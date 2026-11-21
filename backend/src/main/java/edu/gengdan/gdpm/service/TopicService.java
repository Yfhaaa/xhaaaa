package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.TopicInfo;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** TopicService 模块业务契约。 */
public interface TopicService extends IService<TopicInfo> {
    /** 查询当前角色可见的课题。 */
    Object topics();
    /** 保存课题并校验退回重提状态。 */
    Object save(Long topicId, Map<String, Object> body);
    /** 审核待审核课题并保存审核留痕。 */
    Object reviewTopic(long topicId, Map<String, Object> body);
}
