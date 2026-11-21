package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.PermissionService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Permission 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class PermissionController {

    private final PermissionService permissions;

    public PermissionController(PermissionService service) {
        this.permissions = service;
    }

    /** Permission 模块接口：/system/permissions。 */
    @GetMapping("/system/permissions")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> permissions(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(permissions.permissions(), pageQuery));
    }

    /** Permission 模块接口：/system/permissions。 */
    @PostMapping("/system/permissions")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> permission(@RequestBody Map<String, Object> b) {
        return Result.ok(permissions.permission(null, b));
    }

    /** Permission 模块接口：/system/permissions/{id}。 */
    @PutMapping("/system/permissions/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> permission(@PathVariable long id, @RequestBody Map<String, Object> b) {
        return Result.ok(permissions.permission(id, b));
    }
}
