package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.common.Values;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.dto.TopicSaveRequest;
import edu.gengdan.gdpm.entity.TopicInfo;
import edu.gengdan.gdpm.mapper.TopicInfoMapper;
import edu.gengdan.gdpm.service.ReviewService;
import edu.gengdan.gdpm.service.TopicService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 持 topic:read 权限的用户查询本人角色可见课题。 教师在申报截止前创建待审核课题；仅本人已退回课题可修改重提。 管理员仅审核待审核课题，更新为已通过或已退回并保留意见。 */
@Service
public class TopicServiceImpl extends DomainSupport<TopicInfoMapper, TopicInfo> implements TopicService {

    @Autowired
    private ReviewService reviewService;

    /** 持 topic:read 权限的用户查询本人角色可见课题。 */
    public List<Map<String, Object>> topics() {
        auth.permission("topic:read");
        return reports.topics(auth.scope());
    }

    /** 教师在申报截止前创建待审核课题；仅本人已退回课题可修改重提。 */
    @OperLog("申报课题")
    @Transactional
    public IdVO save(Long topicId, TopicSaveRequest body) {
        validate(body);
        auth.roles("TEACHER");
        auth.permission("topic:write");
        auth.deadline("topic");
        long majorId = body.getMajorId(),
            capacity = body.getCapacity();
        required(majors.selectById(majorId));
        String title = body.getTitle(),
            description = body.getDescription();
        if (topicId == null) {
            TopicInfo topic = new TopicInfo();
            topic.setTitle(title);
            topic.setDescription(description);
            topic.setTeacherId(auth.uid());
            topic.setMajorId(majorId);
            topic.setCapacity((int) capacity);
            topic.setStatus(BusinessStatus.PENDING.label());
            save(topic);
            topicId = topic.getId();
        } else {
            TopicInfo old = required(lambdaQuery().eq(TopicInfo::getId, topicId).last("FOR UPDATE").one());
            if (old.getTeacherId() != auth.uid()) {
                throw new ApiException(403, "不能修改其他教师的课题");
            }
            require(BusinessStatus.RETURNED.label().equals(old.getStatus()), "仅退回课题可以修改并重新提交");
            lambdaUpdate()
                .eq(TopicInfo::getId, topicId)
                .set(TopicInfo::getTitle, title)
                .set(TopicInfo::getDescription, description)
                .set(TopicInfo::getMajorId, majorId)
                .set(TopicInfo::getCapacity, (int) capacity)
                .set(TopicInfo::getStatus, BusinessStatus.PENDING.label())
                .set(TopicInfo::getSubmittedAt, LocalDateTime.now())
                .set(TopicInfo::getReviewerId, null)
                .set(TopicInfo::getReviewComment, null)
                .set(TopicInfo::getRevisionCount, old.getRevisionCount() + 1)
                .update();
        }
        for (Map<String, Object> user : reports.users()) {
            if ("ADMIN".equals(user.get("role_code"))) {
                messages.send(Values.id(user.get("id")), "新课题待审核", title, "/topics/audit", null);
            }
        }
        return new IdVO(topicId);
    }

    /** 管理员仅审核待审核课题，更新为已通过或已退回并保留意见。 */
    @OperLog("审核课题")
    @Transactional
    public MessageVO reviewTopic(long topicId, ReviewRequest body) {
        validate(body);
        auth.roles("ADMIN");
        auth.permission("system:manage");
        boolean approved = body.getApproved();
        String comment = body.getComment();
        TopicInfo topic = required(lambdaQuery().eq(TopicInfo::getId, topicId).last("FOR UPDATE").one());
        require(BusinessStatus.PENDING.label().equals(topic.getStatus()), "该课题当前不在待审核状态");
        topic.setStatus(BusinessStatus.reviewed(approved));
        topic.setReviewerId(auth.uid());
        topic.setReviewComment(comment);
        updateById(topic);
        reviewService.record("topic", topicId, null, approved, comment);
        messages.send(
            topic.getTeacherId(),
            "课题" + BusinessStatus.reviewed(approved),
            comment,
            "/topics/declaration",
            null
        );
        return new MessageVO("审核成功");
    }
}
