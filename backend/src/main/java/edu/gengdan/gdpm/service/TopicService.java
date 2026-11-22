package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.dto.TopicSaveRequest;
import edu.gengdan.gdpm.entity.TopicInfo;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import java.util.List;
import java.util.Map;

/** 持 topic:read 权限的用户查询本人角色可见课题。 教师在申报截止前创建待审核课题；仅本人已退回课题可修改重提。 管理员仅审核待审核课题，更新为已通过或已退回并保留意见。 */
public interface TopicService extends IService<TopicInfo> {
    /** 持 topic:read 权限的用户查询本人角色可见课题。 */
    List<Map<String, Object>> topics();

    /** 教师在申报截止前创建待审核课题；仅本人已退回课题可修改重提。 */
    IdVO save(Long topicId, TopicSaveRequest body);

    /** 管理员仅审核待审核课题，更新为已通过或已退回并保留意见。 */
    MessageVO reviewTopic(long topicId, ReviewRequest body);
}
