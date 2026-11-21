package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.NotificationService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Notification 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService service) {
        this.notifications = service;
    }

    /** Notification 模块接口：/notices。 */
    @GetMapping("/notices")
    @RequiresAuthentication
    public Result<Object> notices(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(notifications.notices(), pageQuery));
    }

    /** Notification 模块接口：/notices。 */
    @PostMapping("/notices")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> notice(@RequestBody Map<String, Object> b) {
        return Result.ok(notifications.notice(b));
    }

    /** Notification 模块接口：/notices/{id}。 */
    @DeleteMapping("/notices/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> removeNotice(@PathVariable long id) {
        return Result.ok(notifications.removeNotice(id));
    }
}
