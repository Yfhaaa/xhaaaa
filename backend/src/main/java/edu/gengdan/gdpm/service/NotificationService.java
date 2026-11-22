package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.NoticeSaveRequest;
import edu.gengdan.gdpm.entity.Notice;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import java.util.List;
import java.util.Map;

/** 所有登录用户查询通知公告。 管理员发布标题和正文均完整的通知，成功后记录操作审计。 管理员删除通知，成功后记录操作审计。 */
public interface NotificationService extends IService<Notice> {
    /** 所有登录用户查询通知公告。 */
    List<Map<String, Object>> notices();

    /** 管理员发布标题和正文均完整的通知，成功后记录操作审计。 */
    IdVO notice(NoticeSaveRequest body);

    /** 管理员删除通知，成功后记录操作审计。 */
    MessageVO removeNotice(long noticeId);
}
