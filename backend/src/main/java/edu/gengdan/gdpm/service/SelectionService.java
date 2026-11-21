package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.TopicSelection;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** SelectionService 模块业务契约。 */
public interface SelectionService extends IService<TopicSelection> {
    /** 查询身份范围内的双选申请。 */
    Object selections();
    /** 申请课题并检查专业和剩余名额。 */
    Object apply(long topicId);
    /** 确认双选并保护学生唯一分配。 */
    Object confirm(long selectionId, Map<String, Object> body);
}
