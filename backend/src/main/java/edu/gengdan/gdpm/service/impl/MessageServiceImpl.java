package edu.gengdan.gdpm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import edu.gengdan.gdpm.auth.Auth;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.common.Values;
import edu.gengdan.gdpm.dto.DataScope;
import edu.gengdan.gdpm.entity.SysMessage;
import edu.gengdan.gdpm.entity.WorkflowStep;
import edu.gengdan.gdpm.mapper.ReportMapper;
import edu.gengdan.gdpm.mapper.SysMessageMapper;
import edu.gengdan.gdpm.mapper.WorkflowStepMapper;
import edu.gengdan.gdpm.service.MessageService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 已登录用户仅查询本人消息，可选择仅未读并在范围过滤后分页。 用户只能标记本人消息，编号为空时一键标记本人全部消息。 业务操作向指定收件人写入私信，事件键保证截止提醒不重复。
 * 每天北京时间八点提醒未来三天截止且未完成的节点，不依赖登录上下文。
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private SysMessageMapper mapper;

    @Autowired
    private WorkflowStepMapper steps;

    @Autowired
    private ReportMapper reports;

    @Autowired
    private Auth auth;

    /** 业务操作向指定收件人写入私信，事件键保证截止提醒不重复。 */
    public void send(long userId, String title, String content, String path, String eventKey) {
        if (
            eventKey != null &&
            mapper.selectCount(new LambdaQueryWrapper<SysMessage>().eq(SysMessage::getEventKey, eventKey)) > 0
        ) {
            return;
        }
        SysMessage message = new SysMessage();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setPath(path);
        message.setEventKey(eventKey);
        message.setReadFlag(0);
        mapper.insert(message);
    }

    /** 已登录用户仅查询本人消息，可选择仅未读并在范围过滤后分页。 */
    public List<Map<String, Object>> list(boolean unread) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMessage message : mapper.selectList(
            new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getUserId, auth.uid())
                .eq(unread, SysMessage::getReadFlag, 0)
                .orderByDesc(SysMessage::getId)
        )) {
            result.add(EntityViews.view(message));
        }
        return result;
    }

    /** 用户只能标记本人消息，编号为空时一键标记本人全部消息。 */
    @OperLog("消息标记已读")
    public void read(Long id) {
        if (id != null) {
            SysMessage message = mapper.selectById(id);
            if (message == null || message.getUserId() != auth.uid()) {
                throw new ApiException(403, "不能操作其他用户消息");
            }
        }
        mapper.update(
            null,
            new LambdaUpdateWrapper<SysMessage>()
                .eq(SysMessage::getUserId, auth.uid())
                .eq(id != null, SysMessage::getId, id)
                .set(SysMessage::getReadFlag, 1)
        );
    }

    /** 每天北京时间八点提醒未来三天截止且未完成的节点，不依赖登录上下文。 */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Shanghai")
    @Transactional
    public void reminders() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        List<Map<String, Object>> students = reports.students(
            new DataScope(true, false, 0)
        );
        for (WorkflowStep step : steps.selectList(null)) {
            long days = ChronoUnit.DAYS.between(today, step.getDeadline().toLocalDate());
            if (days < 0 || days > 3) {
                continue;
            }
            for (Map<String, Object> student : students) {
                long sid = Values.id(student.get("student_id"));
                // 九节点明细由聚合查询提供；提醒只面向确有未完成事项的学生。
                Map<String, Object> state = reports.progressState(sid);
                if (state != null && ((Number) state.getOrDefault(step.getCode(), 0)).intValue() > 0) {
                    continue;
                }
                send(
                    sid,
                    "截止提醒：" + step.getName(),
                    "请于 " + step.getDeadline() + " 前完成该节点。",
                    "/my-project",
                    "deadline:" + sid + ":" + step.getCode() + ":" + step.getDeadline()
                );
            }
        }
    }
}
