package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.SysLog;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** LogService 模块业务契约。 */
public interface LogService extends IService<SysLog> {
    /** 查询管理员可见的操作审计日志。 */
    Object logs();
    /** 记录成功操作或审核留痕。 */
    void record(long userId, String action, String detail);
}
