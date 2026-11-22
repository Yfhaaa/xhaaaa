package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.LogService;
import edu.gengdan.gdpm.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 仅具有系统管理权限的管理员查询操作审计。 统一切面在成功操作后记录用户、动作和已脱敏的请求详情。 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api")
public class LogController {

    private final LogService logs;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public LogController(LogService service) {
        this.logs = service;
    }

    /** 仅具有系统管理权限的管理员查询操作审计。 */
    @Operation(summary = "操作日志：Get /system/logs")
    @GetMapping("/system/logs")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<?> logs(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(logs.logs(), pageQuery);
    }
}
