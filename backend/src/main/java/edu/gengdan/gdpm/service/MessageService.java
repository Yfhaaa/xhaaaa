package edu.gengdan.gdpm.service;

import java.util.List;
import java.util.Map;

/**
 * 已登录用户仅查询本人消息，可选择仅未读并在范围过滤后分页。 用户只能标记本人消息，编号为空时一键标记本人全部消息。 业务操作向指定收件人写入私信，事件键保证截止提醒不重复。
 * 每天北京时间八点提醒未来三天截止且未完成的节点，不依赖登录上下文。
 */
public interface MessageService {
    /** 业务操作向指定收件人写入私信，事件键保证截止提醒不重复。 */
    void send(long userId, String title, String content, String path, String eventKey);

    /** 已登录用户仅查询本人消息，可选择仅未读并在范围过滤后分页。 */
    List<Map<String, Object>> list(boolean unread);

    /** 用户只能标记本人消息，编号为空时一键标记本人全部消息。 */
    void read(Long id);

    /** 每天北京时间八点提醒未来三天截止且未完成的节点，不依赖登录上下文。 */
    void reminders();
}
