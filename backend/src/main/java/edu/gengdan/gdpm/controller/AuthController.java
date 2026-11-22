package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.LoginRequest;
import edu.gengdan.gdpm.service.AuthService;
import edu.gengdan.gdpm.vo.LoginVO;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 匿名校验启用账号与密码，签发十二小时令牌，仅持久化摘要。 已登录用户读取本人脱敏资料、角色与实时权限。 已登录用户注销当前会话，原令牌立即失效。 */
@Tag(name = "认证登录")
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService auth;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    /** 返回启动工具使用的健康探针。 */
    @Operation(summary = "认证登录：Get /health")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "gdpm");
    }

    /** 匿名校验启用账号与密码，签发十二小时令牌，仅持久化摘要。 */
    @Operation(summary = "认证登录：Post /auth/login")
    @PostMapping("/auth/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest body) {
        return Result.ok(auth.login(body));
    }

    /** 已登录用户读取本人脱敏资料、角色与实时权限。 */
    @RequiresAuthentication
    @Operation(summary = "认证登录：Get /auth/me")
    @GetMapping("/auth/me")
    public Result<UserVO> me() {
        return Result.ok(auth.me());
    }

    /** 已登录用户注销当前会话，原令牌立即失效。 */
    @RequiresAuthentication
    @Operation(summary = "认证登录：Post /auth/logout")
    @PostMapping("/auth/logout")
    public Result<MessageVO> logout(@RequestHeader("Authorization") String bearer) {
        return Result.ok(auth.logout(bearer));
    }
}
