package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.LogService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Log 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class LogController {

    private final LogService logs;

    public LogController(LogService service) {
        this.logs = service;
    }

    /** Log 模块接口：/system/logs。 */
    @GetMapping("/system/logs")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> logs(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(logs.logs(), pageQuery));
    }
}
