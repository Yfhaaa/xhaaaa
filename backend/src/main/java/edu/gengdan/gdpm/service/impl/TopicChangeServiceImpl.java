package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.common.Values;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.dto.TopicChangeRequest;
import edu.gengdan.gdpm.entity.DefenseInfo;
import edu.gengdan.gdpm.entity.MidtermCheck;
import edu.gengdan.gdpm.entity.ProposalReport;
import edu.gengdan.gdpm.entity.ReviewRecord;
import edu.gengdan.gdpm.entity.StudentAssignment;
import edu.gengdan.gdpm.entity.TaskBook;
import edu.gengdan.gdpm.entity.ThesisInfo;
import edu.gengdan.gdpm.entity.TopicChange;
import edu.gengdan.gdpm.entity.TopicInfo;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.mapper.TopicChangeMapper;
import edu.gengdan.gdpm.service.ReviewService;
import edu.gengdan.gdpm.service.TopicChangeService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.StatusVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** 仅变更参与学生、原教师、新教师和管理员可读变更及审批历史。 学生仅为未进入答辩项目申请本专业有名额的新课题，同一学生只允许一条在途申请。 原教师、新教师、管理员依次审批；终审锁定学生和两课题原子迁移关系，拒绝则保留历史。 */
@Service
public class TopicChangeServiceImpl
    extends DomainSupport<TopicChangeMapper, TopicChange>
    implements TopicChangeService
{

    @Autowired
    private ReviewService reviewService;

    /** 仅变更参与学生、原教师、新教师和管理员可读变更及审批历史。 */
    public List<Map<String, Object>> records() {
        auth.permission("topic:read");
        List<Map<String, Object>> result = new ArrayList<>();
        for (TopicChange change : lambdaQuery().orderByDesc(TopicChange::getId).list()) {
            if (
                !auth.role("ADMIN") &&
                change.getStudentId() != auth.uid() &&
                change.getOldTeacherId() != auth.uid() &&
                change.getNewTeacherId() != auth.uid()
            ) {
                continue;
            }
            Map<String, Object> row = EntityViews.view(change);
            row.put("student_name", required(users.selectById(change.getStudentId())).getName());
            row.put("old_title", required(topics.selectById(change.getOldTopicId())).getTitle());
            row.put("new_title", required(topics.selectById(change.getNewTopicId())).getTitle());
            row.put("old_teacher", required(users.selectById(change.getOldTeacherId())).getName());
            row.put("new_teacher", required(users.selectById(change.getNewTeacherId())).getName());
            row.put(
                "history",
                reviews.selectList(
                    query(ReviewRecord.class)
                        .eq(ReviewRecord::getBusinessType, "topic_change")
                        .eq(ReviewRecord::getBusinessId, change.getId())
                )
            );
            result.add(row);
        }
        return result;
    }

    /** 学生仅为未进入答辩项目申请本专业有名额的新课题，同一学生只允许一条在途申请。 */
    @OperLog("申请课题变更")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IdVO apply(TopicChangeRequest body) {
        validate(body);
        auth.roles("STUDENT");
        auth.permission("topic:read");
        User student = lockStudent(auth.uid());
        StudentAssignment assignment = required(assignments.selectById(auth.uid()));
        require(
            defenses.selectCount(query(DefenseInfo.class).eq(DefenseInfo::getStudentId, auth.uid())) == 0,
            "已进入答辩的项目不能变更课题"
        );
        require(
            lambdaQuery()
                .eq(TopicChange::getStudentId, auth.uid())
                .lt(TopicChange::getApprovalStep, 4)
                .ne(TopicChange::getStatus, BusinessStatus.RETURNED.label())
                .count() == 0,
            "已有正在审批的变更申请"
        );
        TopicInfo target = required(topics.selectById(body.getNewTopicId()));
        require(!target.getId().equals(assignment.getTopicId()), "新旧课题不能相同");
        require(BusinessStatus.APPROVED.label().equals(target.getStatus()), "新课题尚未通过审核");
        require(Objects.equals(student.getMajorId(), target.getMajorId()), "只能变更到本专业课题");
        require(
            assignments.selectCount(
                query(StudentAssignment.class).eq(StudentAssignment::getTopicId, target.getId())
            ) < target.getCapacity(),
            "新课题名额已满"
        );
        TopicChange change = new TopicChange();
        change.setStudentId(auth.uid());
        change.setOldTopicId(assignment.getTopicId());
        change.setNewTopicId(target.getId());
        change.setOldTeacherId(assignment.getTeacherId());
        change.setNewTeacherId(target.getTeacherId());
        change.setReason(body.getReason());
        change.setApprovalStep(1);
        change.setStatus(BusinessStatus.AWAITING_OLD_TEACHER.label());
        save(change);
        messages.send(
            assignment.getTeacherId(),
            "课题变更待审批",
            student.getName() + "提交课题变更申请。",
            "/topic-changes",
            null
        );
        return new IdVO(change.getId());
    }

    /** 原教师、新教师、管理员依次审批；终审锁定学生和两课题原子迁移关系，拒绝则保留历史。 */
    @OperLog("审批课题变更")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public StatusVO approve(long id, ReviewRequest body) {
        validate(body);
        auth.roles("TEACHER", "ADMIN");
        auth.permission("topic:write");
        TopicChange change = required(getById(id));
        lockStudent(change.getStudentId());
        change = required(lambdaQuery().eq(TopicChange::getId, id).last("FOR UPDATE").one());
        int step = change.getApprovalStep();
        require(step <= 3 && !BusinessStatus.RETURNED.label().equals(change.getStatus()), "申请已结束");
        if (
            (step == 1 && (!auth.role("TEACHER") || change.getOldTeacherId() != auth.uid())) ||
            (step == 2 && (!auth.role("TEACHER") || change.getNewTeacherId() != auth.uid())) ||
            (step == 3 && !auth.role("ADMIN"))
        ) {
            throw new ApiException(403, "请由当前环节审批人依次审批");
        }
        boolean approved = body.getApproved();
        String comment = body.getComment();
        if (approved && step == 3) {
            // 固定主键顺序锁定两课题，减少交叉变更死锁。
            for (long topicId : new TreeSet<>(List.of(change.getOldTopicId(), change.getNewTopicId()))) {
                required(
                    topics.selectOne(query(TopicInfo.class).eq(TopicInfo::getId, topicId).last("FOR UPDATE"))
                );
            }
            StudentAssignment assignment = required(assignments.selectById(change.getStudentId()));
            require(assignment.getTopicId().equals(change.getOldTopicId()), "原分配已改变，请重新申请");
            TopicInfo target = required(topics.selectById(change.getNewTopicId()));
            require(BusinessStatus.APPROVED.label().equals(target.getStatus()), "新课题未通过审核");
            require(
                assignments.selectCount(
                    query(StudentAssignment.class).eq(StudentAssignment::getTopicId, target.getId())
                ) < target.getCapacity(),
                "新课题名额已满"
            );
            assignment.setTopicId(target.getId());
            assignment.setTeacherId(target.getTeacherId());
            require(
                Objects.equals(
                    required(users.selectById(change.getStudentId())).getMajorId(),
                    target.getMajorId()
                ),
                "学生专业已改变，请重新申请"
            );
            require(
                defenses.selectCount(
                    query(DefenseInfo.class).eq(DefenseInfo::getStudentId, change.getStudentId())
                ) == 0,
                "项目已进入答辩，不能变更"
            );
            assignments.updateById(assignment);
            // 保留原提交和审核留痕，当前审核责任交给新指导教师。
            long sid = change.getStudentId(),
                tid = target.getTeacherId();
            tasks.update(
                null,
                new LambdaUpdateWrapper<TaskBook>()
                    .eq(TaskBook::getStudentId, sid)
                    .set(TaskBook::getTeacherId, tid)
            );
            proposals.update(
                null,
                new LambdaUpdateWrapper<ProposalReport>()
                    .eq(ProposalReport::getStudentId, sid)
                    .set(ProposalReport::getTeacherId, tid)
            );
            midterms.update(
                null,
                new LambdaUpdateWrapper<MidtermCheck>()
                    .eq(MidtermCheck::getStudentId, sid)
                    .set(MidtermCheck::getTeacherId, tid)
            );
            theses.update(
                null,
                new LambdaUpdateWrapper<ThesisInfo>()
                    .eq(ThesisInfo::getStudentId, sid)
                    .set(ThesisInfo::getTeacherId, tid)
            );
        }
        change.setApprovalStep(approved ? step + 1 : step);
        change.setStatus(
            !approved
                ? BusinessStatus.RETURNED.label()
                : List.of(
                    "", "", BusinessStatus.AWAITING_NEW_TEACHER.label(),
                    BusinessStatus.AWAITING_ADMIN.label(), BusinessStatus.APPROVED.label()
                ).get(step + 1)
        );
        updateById(change);
        reviewService.record(
            "topic_change",
            id,
            change.getStudentId(),
            approved,
            "第" + step + "级：" + comment
        );
        messages.send(
            change.getStudentId(),
            "课题变更" + change.getStatus(),
            comment,
            "/topic-changes",
            null
        );
        if (approved && step == 1) {
            messages.send(
                change.getNewTeacherId(),
                "课题变更待审批",
                "原指导教师已同意，请审批。",
                "/topic-changes",
                null
            );
        }
        if (approved && step == 2) {
            for (Map<String, Object> user : reports.users()) {
                if ("ADMIN".equals(user.get("role_code"))) {
                    messages.send(
                        Values.id(user.get("id")),
                        "课题变更待审批",
                        "两位教师已同意，请终审。",
                        "/topic-changes",
                        null
                    );
                }
            }
        }
        return new StatusVO(change.getStatus());
    }
}
