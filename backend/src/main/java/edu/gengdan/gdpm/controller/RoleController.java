package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.RoleService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Role 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class RoleController {

    private final RoleService roles;

    public RoleController(RoleService service) {
        this.roles = service;
    }

    /** Role 模块接口：/system/roles。 */
    @GetMapping("/system/roles")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> roles(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(roles.roles(), pageQuery));
    }

    /** Role 模块接口：/system/roles/{id}。 */
    @PutMapping("/system/roles/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> role(@PathVariable long id, @RequestBody Map<String, Object> b) {
        return Result.ok(roles.role(id, b));
    }
}
