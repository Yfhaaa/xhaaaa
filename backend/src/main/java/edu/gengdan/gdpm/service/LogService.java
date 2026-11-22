package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.SysLog;
import java.util.List;
import java.util.Map;

/** 仅具有系统管理权限的管理员查询操作审计。 统一切面在成功操作后记录用户、动作和已脱敏的请求详情。 */
public interface LogService extends IService<SysLog> {
    /** 仅具有系统管理权限的管理员查询操作审计。 */
    List<Map<String, Object>> logs();

    /** 统一切面在成功操作后记录用户、动作和已脱敏的请求详情。 */
    void record(long userId, String action, String detail);
}
