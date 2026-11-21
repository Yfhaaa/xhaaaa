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

/** NotificationService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class NotificationServiceImpl extends DomainSupport<NoticeMapper, Notice> implements NotificationService {

    /** 查询通知列表。 */
    public Object notices() {
        auth.uid();
        return reports.notices();
    }

    /** 发布通知。 */
    @OperLog("发布通知")
    @Transactional
    public Object notice(Map<String, Object> body) {
        admin();
        Notice notice = entity(
            Notice.class,
            "title",
            Params.text(body, "title", 200),
            "content",
            Params.text(body, "content", 10000),
            "publisher_id",
            auth.uid()
        );
        save(notice);
        return Map.of("id", notice.getId());
    }

    /** 删除通知。 */
    @OperLog("删除通知")
    @Transactional
    public Object removeNotice(long noticeId) {
        admin();
        removeById(noticeId);
        return Map.of("message", "已删除");
    }
}
