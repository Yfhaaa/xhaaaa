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

/** LogService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class LogServiceImpl extends DomainSupport<SysLogMapper, SysLog> implements LogService {

    /** 查询管理员可见的操作审计日志。 */
    public Object logs() {
        admin();
        return reports.logs();
    }

    /** 记录成功操作或审核留痕。 */
    public void record(long userId, String action, String detail) {
        save(entity(SysLog.class, "user_id", userId, "action", action, "detail", detail));
    }
}
