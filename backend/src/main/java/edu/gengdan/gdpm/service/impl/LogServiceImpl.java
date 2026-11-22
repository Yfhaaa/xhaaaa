package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.entity.SysLog;
import edu.gengdan.gdpm.mapper.SysLogMapper;
import edu.gengdan.gdpm.service.LogService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** 仅具有系统管理权限的管理员查询操作审计。 统一切面在成功操作后记录用户、动作和已脱敏的请求详情。 */
@Service
public class LogServiceImpl extends DomainSupport<SysLogMapper, SysLog> implements LogService {

    /** 仅具有系统管理权限的管理员查询操作审计。 */
    public List<Map<String, Object>> logs() {
        admin();
        return reports.logs();
    }

    /** 统一切面在成功操作后记录用户、动作和已脱敏的请求详情。 */
    public void record(long userId, String action, String detail) {
        SysLog record2 = new SysLog();
        record2.setUserId(userId);
        record2.setAction(action);
        record2.setDetail(detail);
        save(record2);
    }
}
