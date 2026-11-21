package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.*;

import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** SelectionService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class SelectionServiceImpl
    extends DomainSupport<TopicSelectionMapper, TopicSelection>
    implements SelectionService
{

    @Autowired
    private ReviewService reviewService;

    /** 查询身份范围内的双选申请。 */
    public Object selections() {
        auth.permission("topic:read");
        return reports.selections(auth.scope());
    }

    /** 申请课题并检查专业和剩余名额。 */
    @OperLog("申请选题")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Object apply(long topicId) {
        auth.roles("STUDENT");
        auth.permission("topic:read");
        auth.deadline("selection");
        User student = lockStudent(auth.uid());
        require(assignments.selectById(auth.uid()) == null, "每名学生只能确定一个课题");
        TopicInfo topic = required(
            topics.selectOne(query(TopicInfo.class).eq(TopicInfo::getId, topicId).last("FOR UPDATE"))
        );
        require(BusinessStatus.APPROVED.label().equals(topic.getStatus()), "课题尚未审核通过");
        require(Objects.equals(student.getMajorId(), topic.getMajorId()), "请选择本专业课题");
        require(
            assignments.selectCount(query(StudentAssignment.class).eq(StudentAssignment::getTopicId, topicId)) <
                topic.getCapacity(),
            "课题名额已满"
        );
        TopicSelection selection = lambdaQuery()
            .eq(TopicSelection::getStudentId, auth.uid())
            .eq(TopicSelection::getTopicId, topicId)
            .one();
        if (selection == null) {
            selection = entity(
                TopicSelection.class,
                "topic_id",
                topicId,
                "student_id",
                auth.uid(),
                "status",
                BusinessStatus.PENDING.label()
            );
            save(selection);
        } else {
            require(BusinessStatus.RETURNED.label().equals(selection.getStatus()), "请勿重复申请");
            lambdaUpdate()
                .eq(TopicSelection::getId, selection.getId())
                .set(TopicSelection::getStatus, BusinessStatus.PENDING.label())
                .set(TopicSelection::getSubmittedAt, LocalDateTime.now())
                .set(TopicSelection::getReviewerId, null)
                .set(TopicSelection::getReviewComment, null)
                .set(TopicSelection::getRevisionCount, selection.getRevisionCount() + 1)
                .update();
        }
        return Map.of("id", selection.getId());
    }

    /**
     * 锁学生、课题和申请，事务内检查唯一分配与并发名额。
     * 使用 READ_COMMITTED，确保等待课题锁后可见其他确认事务刚提交的分配记录。
     */
    @OperLog("确认双选")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Object confirm(long selectionId, Map<String, Object> body) {
        auth.roles("TEACHER");
        auth.permission("topic:write");
        boolean approved = Params.approved(body);
        String comment = Params.text(body, "comment", 2000);
        TopicSelection selection = required(getById(selectionId));
        TopicInfo owner = required(topics.selectById(selection.getTopicId()));
        if (owner.getTeacherId() != auth.uid()) {
            throw new ApiException(403, "只能处理本人课题的申请");
        }
        long studentId = selection.getStudentId(),
            topicId = selection.getTopicId();
        lockStudent(studentId);
        TopicInfo topic = required(
            topics.selectOne(query(TopicInfo.class).eq(TopicInfo::getId, topicId).last("FOR UPDATE"))
        );
        selection = required(lambdaQuery().eq(TopicSelection::getId, selectionId).last("FOR UPDATE").one());
        require(BusinessStatus.PENDING.label().equals(selection.getStatus()), "该申请当前不在待审核状态");
        if (approved) {
            require(BusinessStatus.APPROVED.label().equals(topic.getStatus()), "课题未通过审核");
            require(assignments.selectById(studentId) == null, "该学生已经确定课题");
            require(
                assignments.selectCount(query(StudentAssignment.class).eq(StudentAssignment::getTopicId, topicId)) <
                    topic.getCapacity(),
                "课题名额已满"
            );
            assignments.insert(
                entity(StudentAssignment.class, "student_id", studentId, "topic_id", topicId, "teacher_id", auth.uid())
            );
            lambdaUpdate()
                .eq(TopicSelection::getStudentId, studentId)
                .eq(TopicSelection::getStatus, BusinessStatus.PENDING.label())
                .ne(TopicSelection::getId, selectionId)
                .set(TopicSelection::getStatus, BusinessStatus.RETURNED.label())
                .set(TopicSelection::getReviewComment, "已确认其他课题")
                .set(TopicSelection::getReviewerId, auth.uid())
                .update();
        }
        selection.setStatus(BusinessStatus.reviewed(approved));
        selection.setReviewerId(auth.uid());
        selection.setReviewComment(comment);
        updateById(selection);
        reviewService.record("selection", selectionId, studentId, approved, comment);
        return Map.of("message", "处理成功");
    }
}
