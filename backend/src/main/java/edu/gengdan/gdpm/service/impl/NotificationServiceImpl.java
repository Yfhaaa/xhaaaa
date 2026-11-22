package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.NoticeSaveRequest;
import edu.gengdan.gdpm.entity.Notice;
import edu.gengdan.gdpm.mapper.NoticeMapper;
import edu.gengdan.gdpm.service.NotificationService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 所有登录用户查询通知公告。 管理员发布标题和正文均完整的通知，成功后记录操作审计。 管理员删除通知，成功后记录操作审计。 */
@Service
public class NotificationServiceImpl
    extends DomainSupport<NoticeMapper, Notice>
    implements NotificationService
{

    /** 所有登录用户查询通知公告。 */
    public List<Map<String, Object>> notices() {
        auth.uid();
        return reports.notices();
    }

    /** 管理员发布标题和正文均完整的通知，成功后记录操作审计。 */
    @OperLog("发布通知")
    @Transactional
    public IdVO notice(NoticeSaveRequest body) {
        validate(body);
        admin();
        Notice notice = new Notice();
        notice.setTitle(body.getTitle());
        notice.setContent(body.getContent());
        notice.setPublisherId(auth.uid());
        save(notice);
        return new IdVO(notice.getId());
    }

    /** 管理员删除通知，成功后记录操作审计。 */
    @OperLog("删除通知")
    @Transactional
    public MessageVO removeNotice(long noticeId) {
        admin();
        removeById(noticeId);
        return new MessageVO("已删除");
    }
}
