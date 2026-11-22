package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.common.Values;
import edu.gengdan.gdpm.dto.PlagiarismRegisterRequest;
import edu.gengdan.gdpm.entity.FileRecord;
import edu.gengdan.gdpm.entity.PlagiarismCheck;
import edu.gengdan.gdpm.entity.ThesisInfo;
import edu.gengdan.gdpm.entity.WorkflowStep;
import edu.gengdan.gdpm.mapper.PlagiarismCheckMapper;
import edu.gengdan.gdpm.service.PlagiarismService;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** 持 material:read 权限查询授权学生的查重记录及对应定稿版本。 指导教师或管理员为尚未通过的最新定稿登记本人报告，新版本不能复用旧结论。 */
@Service
public class PlagiarismServiceImpl
    extends DomainSupport<PlagiarismCheckMapper, PlagiarismCheck>
    implements PlagiarismService
{

    /** 持 material:read 权限查询授权学生的查重记录及对应定稿版本。 */
    public List<Map<String, Object>> records() {
        auth.permission("material:read");
        Set<Long> allowed = new HashSet<>();
        for (Map<String, Object> row : reports.students(auth.scope())) {
            allowed.add(Values.id(row.get("student_id")));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (PlagiarismCheck check : lambdaQuery().orderByDesc(PlagiarismCheck::getId).list()) {
            if (!allowed.contains(check.getStudentId())) {
                continue;
            }
            Map<String, Object> row = EntityViews.view(check);
            row.put("student_name", required(users.selectById(check.getStudentId())).getName());
            row.put("registrar_name", required(users.selectById(check.getRegistrarId())).getName());
            row.put("version", required(theses.selectById(check.getThesisId())).getVersion());
            row.put(
                "threshold",
                required(
                    workflowSteps.selectOne(query(WorkflowStep.class).eq(WorkflowStep::getCode, "final"))
                ).getPlagiarismThreshold()
            );
            result.add(row);
        }
        return result;
    }

    /** 指导教师或管理员为尚未通过的最新定稿登记本人报告，新版本不能复用旧结论。 */
    @OperLog("登记查重报告")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IdVO register(PlagiarismRegisterRequest body) {
        validate(body);
        auth.roles("ADMIN", "TEACHER");
        auth.permission("material:write");
        long sid = body.getStudentId();
        auth.student(sid);
        lockStudent(sid);
        ThesisInfo thesis = required(
            theses.selectOne(
                query(ThesisInfo.class)
                    .eq(ThesisInfo::getStudentId, sid)
                    .eq(ThesisInfo::getStage, "final")
                    .orderByDesc(ThesisInfo::getVersion)
                    .last("LIMIT 1")
            )
        );
        require(!BusinessStatus.APPROVED.label().equals(thesis.getStatus()), "定稿已审核通过，不可追加改变准入结果");
        FileRecord file = required(files.selectById(body.getFileId()));
        if (file.getOwnerId() != auth.uid()) {
            throw new ApiException(403, "报告必须为本人上传的文件");
        }
        PlagiarismCheck check = new PlagiarismCheck();
        check.setStudentId(sid);
        check.setThesisId(thesis.getId());
        check.setPlatform(body.getPlatform());
        check.setRate(body.getRate());
        check.setFileId(file.getId());
        check.setRegistrarId(auth.uid());
        save(check);
        messages.send(sid, "查重结果已登记", "重复率 " + check.getRate() + "%", "/plagiarism", null);
        return new IdVO(check.getId());
    }
}
