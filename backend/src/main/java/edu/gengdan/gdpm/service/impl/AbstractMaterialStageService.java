package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.str;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.service.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/** 共用阶段事务，具体模块通过类型化 Lambda 条件选择其业务表。 */
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

    /** 查询指定阶段的最新版本。 */
    public T latest(long studentId, String stage) {
        validateStage(stage);
        return getOne(stageQuery(studentId, stage).last("LIMIT 1"));
    }

    /** 学生行锁保护版本号和归档状态，退回后创建新版本。 */
    @OperLog("提交材料")
    @Transactional
    public Object submit(String stage, Map<String, Object> body) {
        validateStage(stage);
        auth.permission("material:write");
        auth.deadline(stage);
        long studentId;
        if (stage.equals("task")) {
            auth.roles("TEACHER");
            studentId = Params.number(body, "studentId");
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
        long fileId = Params.number(body, "fileId");
        FileRecord file = required(files.selectById(fileId));
        if (file.getOwnerId() != auth.uid()) {
            throw new ApiException(403, "必须使用本人上传的文件");
        }
        int version = previous == null ? 1 : previous.getVersion() + 1;
        T material = newMaterial();
        material.setStudentId(studentId);
        material.setTeacherId(assignment.getTeacherId());
        material.setStage(stage);
        material.setTitle(Params.text(body, "title", 200));
        material.setContent(str(body, "content"));
        material.setFileId(fileId);
        material.setVersion(version);
        material.setStatus(stage.equals("task") ? BusinessStatus.APPROVED.label() : BusinessStatus.PENDING.label());
        material.setRevisionCount(version - 1);
        if (stage.equals("task")) {
            material.setReviewerId(auth.uid());
            material.setReviewComment("教师下达任务书");
            material.setReviewedAt(LocalDateTime.now());
        }
        save(material);
        if (stage.equals("task")) {
            reviewService.record(stage, material.getId(), studentId, true, "教师下达任务书");
        }
        return Map.of("id", material.getId(), "version", version);
    }

    /** 只允许指导教师审核当前待审核的最新版本。 */
    @OperLog("审核材料")
    @Transactional
    public Object review(String stage, long materialId, Map<String, Object> body) {
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
        boolean approved = Params.approved(body);
        String comment = Params.text(body, "comment", 2000);
        material.setStatus(BusinessStatus.reviewed(approved));
        material.setReviewerId(auth.uid());
        material.setReviewComment(comment);
        material.setReviewedAt(LocalDateTime.now());
        updateById(material);
        reviewService.record(stage, materialId, studentId, approved, comment);
        return Map.of("message", "审核成功");
    }
}
