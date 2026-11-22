package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.UserSaveRequest;
import edu.gengdan.gdpm.service.UserService;
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

/** 管理员读取系统用户和角色信息，不返回密码。 学生仅查询本人，教师仅查询指导学生，管理员查询全部学生。 管理员维护用户与角色；禁止破坏自身管理员身份，停用或修改使既有令牌失效。 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService users;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public UserController(UserService service) {
        this.users = service;
    }

    /** 学生仅查询本人，教师仅查询指导学生，管理员查询全部学生。 */
    @Operation(summary = "用户管理：Get /students")
    @GetMapping("/students")
    @RequiresAuthentication
    public Result<?> students(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(users.students(), pageQuery);
    }

    /** 管理员读取系统用户和角色信息，不返回密码。 */
    @Operation(summary = "用户管理：Get /system/users")
    @GetMapping("/system/users")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<?> users(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(users.users(), pageQuery);
    }

    /** 管理员维护用户与角色；禁止破坏自身管理员身份，停用或修改使既有令牌失效。 */
    @Operation(summary = "用户管理：Post /system/users")
    @PostMapping("/system/users")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> user(@Valid @RequestBody UserSaveRequest b) {
        return Result.ok(users.user(null, b));
    }

    /** 管理员维护用户与角色；禁止破坏自身管理员身份，停用或修改使既有令牌失效。 */
    @Operation(summary = "用户管理：Put /system/users/{id}")
    @PutMapping("/system/users/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> user(@PathVariable long id, @Valid @RequestBody UserSaveRequest b) {
        return Result.ok(users.user(id, b));
    }
}
