package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.common.Values;
import edu.gengdan.gdpm.dto.ThesisReviewAssignRequest;
import edu.gengdan.gdpm.dto.ThesisReviewRequest;
import edu.gengdan.gdpm.entity.StudentAssignment;
import edu.gengdan.gdpm.entity.ThesisInfo;
import edu.gengdan.gdpm.entity.ThesisReview;
import edu.gengdan.gdpm.mapper.ThesisReviewMapper;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.service.ReviewService;
import edu.gengdan.gdpm.service.ThesisReviewService;
import edu.gengdan.gdpm.vo.CountVO;
import edu.gengdan.gdpm.vo.IdVO;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 仅指定评阅教师、学生本人及管理员可查询评阅详情。 管理员为定稿通过学生批量或单独分配非指导教师，任一无效项使整批回滚。 指定评阅教师仅一次提交百分制分数、意见与答辩资格，状态从待评阅改为已评阅。 */
@Service
public class ThesisReviewServiceImpl
    extends DomainSupport<ThesisReviewMapper, ThesisReview>
    implements ThesisReviewService
{

    @Autowired
    private MaterialService materials;

    @Autowired
    private ReviewService reviewService;

    /** 仅指定评阅教师、学生本人及管理员可查询评阅详情。 */
    public List<Map<String, Object>> records() {
        auth.permission("material:read");
        List<Map<String, Object>> result = new ArrayList<>();
        for (ThesisReview review : lambdaQuery().orderByDesc(ThesisReview::getId).list()) {
            if (
                !auth.role("ADMIN") &&
                review.getReviewerId() != auth.uid() &&
                review.getStudentId() != auth.uid()
            ) {
                continue;
            }
            Map<String, Object> row = EntityViews.view(review);
            row.put("student_name", required(users.selectById(review.getStudentId())).getName());
            row.put("reviewer_name", required(users.selectById(review.getReviewerId())).getName());
            ThesisInfo thesis = required(theses.selectById(review.getThesisId()));
            row.put("file_id", thesis.getFileId());
            row.put("title", thesis.getTitle());
            result.add(row);
        }
        return result;
    }

    /** 管理员为定稿通过学生批量或单独分配非指导教师，任一无效项使整批回滚。 */
    @OperLog("分配论文评阅")
    @Transactional
    public CountVO assign(ThesisReviewAssignRequest body) {
        validate(body);
        admin();
        List<Long> ids = body.getStudentIds() == null ? List.of(body.getStudentId()) : body.getStudentIds();
        Set<Long> ordered = new TreeSet<>();
        for (long value : ids) {
            ordered.add(value);
        }
        for (long sid : ordered) {
            lockStudent(sid);
            StudentAssignment assignment = required(assignments.selectById(sid));
            require(materials.passed(sid, "final"), "仅可分配定稿通过的学生");
            require(lambdaQuery().eq(ThesisReview::getStudentId, sid).count() == 0, "学生已分配评阅教师");
            List<Long> candidates = new ArrayList<>();
            for (Map<String, Object> teacher : reports.teachers()) {
                long tid = Values.id(teacher.get("id"));
                if (tid != assignment.getTeacherId()) {
                    candidates.add(tid);
                }
            }
            require(!candidates.isEmpty(), "没有可用的非指导教师");
            long reviewer =
                body.getReviewerId() == null
                    ? candidates.get(new SecureRandom().nextInt(candidates.size()))
                    : body.getReviewerId();
            require(candidates.contains(reviewer), "评阅人必须为有效教师且不能是本人指导教师");
            Map<String, Object> finalMaterial = materials.latest(sid, "final");
            ThesisReview review = new ThesisReview();
            review.setStudentId(sid);
            review.setReviewerId(reviewer);
            review.setThesisId(Values.id(finalMaterial.get("id")));
            review.setStatus(BusinessStatus.AWAITING_REVIEW.label());
            save(review);
            messages.send(
                reviewer,
                "新增论文评阅任务",
                "请完成论文评阅并确认是否同意答辩。",
                "/my-reviews",
                null
            );
        }
        return new CountVO(ordered.size());
    }

    /** 指定评阅教师仅一次提交百分制分数、意见与答辩资格，状态从待评阅改为已评阅。 */
    @OperLog("提交论文评阅")
    @Transactional
    public IdVO review(long id, ThesisReviewRequest body) {
        validate(body);
        auth.roles("TEACHER");
        auth.permission("material:write");
        ThesisReview review = required(getById(id));
        lockStudent(review.getStudentId());
        review = required(lambdaQuery().eq(ThesisReview::getId, id).last("FOR UPDATE").one());
        if (review.getReviewerId() != auth.uid()) {
            throw new ApiException(403, "只能评阅分配给自己的论文");
        }
        require(BusinessStatus.AWAITING_REVIEW.label().equals(review.getStatus()), "该论文已经评阅");
        review.setComment(body.getComment());
        review.setScore(body.getScore());
        boolean agree = body.getApproved();
        review.setAgreeDefense(agree ? 1 : 0);
        review.setStatus(BusinessStatus.REVIEWED.label());
        review.setReviewedAt(LocalDateTime.now());
        updateById(review);
        reviewService.record("thesis_review", id, review.getStudentId(), agree, review.getComment());
        messages.send(
            review.getStudentId(),
            "论文评阅完成",
            agree ? "同意进入答辩。" : "未获答辩资格，请联系教师。",
            "/my-project",
            null
        );
        return new IdVO(id);
    }
}
