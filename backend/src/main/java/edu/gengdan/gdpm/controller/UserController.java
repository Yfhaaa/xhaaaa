package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.UserService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** User 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService users;

    public UserController(UserService service) {
        this.users = service;
    }

    /** User 模块接口：/students。 */
    @GetMapping("/students")
    @RequiresAuthentication
    public Result<Object> students(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(users.students(), pageQuery));
    }

    /** User 模块接口：/system/users。 */
    @GetMapping("/system/users")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> users(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(users.users(), pageQuery));
    }

    /** User 模块接口：/system/users。 */
    @PostMapping("/system/users")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> user(@RequestBody Map<String, Object> b) {
        return Result.ok(users.user(null, b));
    }

    /** User 模块接口：/system/users/{id}。 */
    @PutMapping("/system/users/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> user(@PathVariable long id, @RequestBody Map<String, Object> b) {
        return Result.ok(users.user(id, b));
    }
}
