package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.Notice;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** NotificationService 模块业务契约。 */
public interface NotificationService extends IService<Notice> {
    /** 查询通知列表。 */
    Object notices();
    /** 发布通知。 */
    Object notice(Map<String, Object> body);
    /** 删除通知。 */
    Object removeNotice(long noticeId);
}
