package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.RoleSaveRequest;
import edu.gengdan.gdpm.service.RoleService;
import edu.gengdan.gdpm.vo.MessageVO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理员查询内置角色及关联权限编号。 管理员维护角色名称及权限，管理员角色必须保留系统管理权限。 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api")
public class RoleController {

    private final RoleService roles;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public RoleController(RoleService service) {
        this.roles = service;
    }

    /** 管理员查询内置角色及关联权限编号。 */
    @Operation(summary = "角色管理：Get /system/roles")
    @GetMapping("/system/roles")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<?> roles(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(roles.roles(), pageQuery);
    }

    /** 管理员维护角色名称及权限，管理员角色必须保留系统管理权限。 */
    @Operation(summary = "角色管理：Put /system/roles/{id}")
    @PutMapping("/system/roles/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<MessageVO> role(@PathVariable long id, @Valid @RequestBody RoleSaveRequest b) {
        return Result.ok(roles.role(id, b));
    }
}
