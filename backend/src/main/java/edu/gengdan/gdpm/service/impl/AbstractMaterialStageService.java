package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.Constants;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.MaterialSubmitRequest;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import edu.gengdan.gdpm.entity.FileRecord;
import edu.gengdan.gdpm.entity.PlagiarismCheck;
import edu.gengdan.gdpm.entity.ProcessMaterial;
import edu.gengdan.gdpm.entity.StudentAssignment;
import edu.gengdan.gdpm.entity.WorkflowStep;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.service.MaterialStageService;
import edu.gengdan.gdpm.service.ReviewService;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.SubmissionVO;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/** 持 material:read 权限查询范围内各阶段全部历史版本。 */
public abstract class AbstractMaterialStageService<M extends BaseMapper<T>, T extends ProcessMaterial>
    extends DomainSupport<M, T>
    implements MaterialStageService<T>
{

    @Autowired
    @Lazy
    private MaterialService materialService;

    @Autowired
    private ReviewService reviewService;

    /** 构造当前材料表的学生与阶段查询条件。 */
    protected abstract LambdaQueryWrapper<T> stageQuery(long studentId, String stage);

    /** 创建当前阶段对应的材料实体。 */
    protected abstract T newMaterial();

    /** 拒绝与当前模块不匹配的材料阶段。 */
    protected abstract void validateStage(String stage);

    /** 先校验学生数据范围，再读取指定阶段最新材料，尚未提交时返回空。 */
    public T latest(long studentId, String stage) {
        validateStage(stage);
        return getOne(stageQuery(studentId, stage).last("LIMIT 1"));
    }

    /** 学生按阶段顺序提交本人文件；任务书由指导教师下达，退回重提创建新版本。 */
    @OperLog("提交材料")
    @Transactional
    public SubmissionVO submit(String stage, MaterialSubmitRequest body) {
        validate(body);
        validateStage(stage);
        auth.permission("material:write");
        auth.deadline(stage);
        long studentId;
        if (stage.equals("task")) {
            auth.roles("TEACHER");
            validate(body, MaterialSubmitRequest.Task.class);
            studentId = body.getStudentId();
            auth.student(studentId);
        } else {
            auth.roles("STUDENT");
            studentId = auth.uid();
        }
        lockStudent(studentId);
        StudentAssignment assignment = required(assignments.selectById(studentId));
        if (auth.role("TEACHER") && assignment.getTeacherId() != auth.uid()) {
            throw new ApiException(403, "只能下达给本人指导的学生");
        }
        require(
            archives.selectCount(query(ArchiveRecord.class).eq(ArchiveRecord::getStudentId, studentId)) == 0,
            "该项目已归档，不能再变更材料"
        );
        T previous = latest(studentId, stage);
        require(
            previous == null || BusinessStatus.RETURNED.label().equals(previous.getStatus()),
            "该阶段已提交，请等待审核；通过后不能重复提交"
        );
        int index = Constants.STAGES.indexOf(stage);
        if (index > 0) {
            require(
                materialService.passed(studentId, Constants.STAGES.get(index - 1)),
                "请先完成上一阶段材料并通过审核"
            );
        }
        long fileId = body.getFileId();
        FileRecord file = required(files.selectById(fileId));
        if (file.getOwnerId() != auth.uid()) {
            throw new ApiException(403, "必须使用本人上传的文件");
        }
        int version = previous == null ? 1 : previous.getVersion() + 1;
        T material = newMaterial();
        material.setStudentId(studentId);
        material.setTeacherId(assignment.getTeacherId());
        material.setStage(stage);
        material.setTitle(body.getTitle());
        material.setContent(body.getContent());
        material.setFileId(fileId);
        material.setVersion(version);
        material.setStatus(
            stage.equals("task") ? BusinessStatus.APPROVED.label() : BusinessStatus.PENDING.label()
        );
        material.setRevisionCount(version - 1);
        if (stage.equals("task")) {
            material.setReviewerId(auth.uid());
            material.setReviewComment("教师下达任务书");
            material.setReviewedAt(LocalDateTime.now());
        }
        save(material);
        messages.send(
            stage.equals("task") ? studentId : assignment.getTeacherId(),
            "新材料已提交",
            material.getTitle(),
            stage.equals("task") ? "/my-project" : "/pending-reviews",
            null
        );
        if (stage.equals("task")) {
            reviewService.record(stage, material.getId(), studentId, true, "教师下达任务书");
        }
        return new SubmissionVO(material.getId(), version);
    }

    /** 指导教师仅审核待审核的最新版本，定稿通过还需对应版本查重合格。 */
    @OperLog("审核材料")
    @Transactional
    public MessageVO review(String stage, long materialId, ReviewRequest body) {
        validate(body);
        validateStage(stage);
        auth.roles("TEACHER");
        auth.permission("material:write");
        require(!stage.equals("task"), "任务书由教师直接下达");
        T material = required(getById(materialId));
        require(stage.equals(material.getStage()), "材料阶段不匹配");
        long studentId = material.getStudentId();
        auth.student(studentId);
        lockStudent(studentId);
        material = required(getById(materialId));
        if (material.getTeacherId() != auth.uid()) {
            throw new ApiException(403, "只能审核本人指导学生的材料");
        }
        require(BusinessStatus.PENDING.label().equals(material.getStatus()), "仅待审核材料可以审核");
        require(latest(studentId, stage).getId() == materialId, "只能审核最新版本");
        boolean approved = body.getApproved();
        String comment = body.getComment();
        if (approved && stage.equals("final")) {
            PlagiarismCheck check = checks.selectOne(
                query(PlagiarismCheck.class)
                    .eq(PlagiarismCheck::getThesisId, materialId)
                    .orderByDesc(PlagiarismCheck::getId)
                    .last("LIMIT 1")
            );
            require(check != null, "请先为当前定稿版本登记查重报告");
            WorkflowStep step = required(
                workflowSteps.selectOne(query(WorkflowStep.class).eq(WorkflowStep::getCode, "final"))
            );
            require(
                check.getRate().compareTo(step.getPlagiarismThreshold()) <= 0,
                "重复率超过节点阈值，不能通过定稿"
            );
        }
        material.setStatus(BusinessStatus.reviewed(approved));
        material.setReviewerId(auth.uid());
        material.setReviewComment(comment);
        material.setReviewedAt(LocalDateTime.now());
        updateById(material);
        reviewService.record(stage, materialId, studentId, approved, comment);
        messages.send(studentId, "材料" + BusinessStatus.reviewed(approved), comment, "/my-project", null);
        return new MessageVO("审核成功");
    }
}
