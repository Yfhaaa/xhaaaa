package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.PermissionSaveRequest;
import edu.gengdan.gdpm.service.PermissionService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理员读取权限目录，供角色配置使用。 管理员创建资源:操作编码或修改权限名称，已有编码不允许变更。 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/api")
public class PermissionController {

    private final PermissionService permissions;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public PermissionController(PermissionService service) {
        this.permissions = service;
    }

    /** 管理员读取权限目录，供角色配置使用。 */
    @Operation(summary = "权限管理：Get /system/permissions")
    @GetMapping("/system/permissions")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<?> permissions(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(permissions.permissions(), pageQuery);
    }

    /** 管理员创建资源:操作编码或修改权限名称，已有编码不允许变更。 */
    @Operation(summary = "权限管理：Post /system/permissions")
    @PostMapping("/system/permissions")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> permission(@Valid @RequestBody PermissionSaveRequest b) {
        return Result.ok(permissions.permission(null, b));
    }

    /** 管理员创建资源:操作编码或修改权限名称，已有编码不允许变更。 */
    @Operation(summary = "权限管理：Put /system/permissions/{id}")
    @PutMapping("/system/permissions/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> permission(@PathVariable long id, @Valid @RequestBody PermissionSaveRequest b) {
        return Result.ok(permissions.permission(id, b));
    }
}
