package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.entity.StudentAssignment;
import edu.gengdan.gdpm.entity.TopicInfo;
import edu.gengdan.gdpm.entity.TopicSelection;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.mapper.TopicSelectionMapper;
import edu.gengdan.gdpm.service.ReviewService;
import edu.gengdan.gdpm.service.SelectionService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** 按学生本人或教师指导范围查询双选申请。 学生在选题截止前申请本专业有名额的已通过课题，拒绝重复和多课题确定。 课题教师在事务内确认待审核申请，锁学生与课题保证唯一分配且不超名额。 */
@Service
public class SelectionServiceImpl
    extends DomainSupport<TopicSelectionMapper, TopicSelection>
    implements SelectionService
{

    @Autowired
    private ReviewService reviewService;

    /** 按学生本人或教师指导范围查询双选申请。 */
    public List<Map<String, Object>> selections() {
        auth.permission("topic:read");
        return reports.selections(auth.scope());
    }

    /** 学生在选题截止前申请本专业有名额的已通过课题，拒绝重复和多课题确定。 */
    @OperLog("申请选题")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IdVO apply(long topicId) {
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
            assignments.selectCount(
                query(StudentAssignment.class).eq(StudentAssignment::getTopicId, topicId)
            ) < topic.getCapacity(),
            "课题名额已满"
        );
        TopicSelection selection = lambdaQuery()
            .eq(TopicSelection::getStudentId, auth.uid())
            .eq(TopicSelection::getTopicId, topicId)
            .one();
        if (selection == null) {
            selection = new TopicSelection();
            selection.setTopicId(topicId);
            selection.setStudentId(auth.uid());
            selection.setStatus(BusinessStatus.PENDING.label());
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
        messages.send(
            topic.getTeacherId(),
            "新增双选申请",
            student.getName() + "申请您的课题。",
            "/selections",
            null
        );
        return new IdVO(selection.getId());
    }

    /** 课题教师在事务内确认待审核申请，锁学生与课题保证唯一分配且不超名额。 */
    @OperLog("确认双选")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public MessageVO confirm(long selectionId, ReviewRequest body) {
        validate(body);
        auth.roles("TEACHER");
        auth.permission("topic:write");
        boolean approved = body.getApproved();
        String comment = body.getComment();
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
                assignments.selectCount(
                    query(StudentAssignment.class).eq(StudentAssignment::getTopicId, topicId)
                ) < topic.getCapacity(),
                "课题名额已满"
            );
            StudentAssignment record5 = new StudentAssignment();
            record5.setStudentId(studentId);
            record5.setTopicId(topicId);
            record5.setTeacherId(auth.uid());
            assignments.insert(record5);
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
        messages.send(
            studentId,
            "选题结果：" + BusinessStatus.reviewed(approved),
            comment,
            "/selections",
            null
        );
        return new MessageVO("处理成功");
    }
}
