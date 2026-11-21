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
import org.springframework.transaction.annotation.Transactional;

/** TopicService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class TopicServiceImpl extends DomainSupport<TopicInfoMapper, TopicInfo> implements TopicService {

    @Autowired
    private ReviewService reviewService;

    /** 查询当前角色可见的课题。 */
    public Object topics() {
        auth.permission("topic:read");
        return reports.topics(auth.scope());
    }

    /** 保存课题并校验退回重提状态。 */
    @OperLog("申报课题")
    @Transactional
    public Object save(Long topicId, Map<String, Object> body) {
        auth.roles("TEACHER");
        auth.permission("topic:write");
        auth.deadline("topic");
        long majorId = Params.number(body, "majorId"),
            capacity = Params.number(body, "capacity");
        require(capacity <= 20, "每个课题最多20人");
        required(majors.selectById(majorId));
        String title = Params.text(body, "title", 200),
            description = Params.text(body, "description", 10000);
        if (topicId == null) {
            TopicInfo topic = entity(
                TopicInfo.class,
                "title",
                title,
                "description",
                description,
                "teacher_id",
                auth.uid(),
                "major_id",
                majorId,
                "capacity",
                (int) capacity,
                "status",
                BusinessStatus.PENDING.label()
            );
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
        return Map.of("id", topicId);
    }

    /** 审核待审核课题并保存审核留痕。 */
    @OperLog("审核课题")
    @Transactional
    public Object reviewTopic(long topicId, Map<String, Object> body) {
        auth.roles("ADMIN");
        auth.permission("system:manage");
        boolean approved = Params.approved(body);
        String comment = Params.text(body, "comment", 2000);
        TopicInfo topic = required(lambdaQuery().eq(TopicInfo::getId, topicId).last("FOR UPDATE").one());
        require(BusinessStatus.PENDING.label().equals(topic.getStatus()), "该课题当前不在待审核状态");
        topic.setStatus(BusinessStatus.reviewed(approved));
        topic.setReviewerId(auth.uid());
        topic.setReviewComment(comment);
        updateById(topic);
        reviewService.record("topic", topicId, null, approved, comment);
        return Map.of("message", "审核成功");
    }
}
