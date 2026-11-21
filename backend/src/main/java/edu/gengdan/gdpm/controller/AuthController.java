package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.LoginRequest;
import edu.gengdan.gdpm.service.AuthService;
import java.util.Map;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.*;

/** 身份接口，健康探针保留启动工具所需的稳定协议。 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    /** 返回启动工具使用的健康探针。 */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "gdpm");
    }

    /** 验证账号后签发限时随机令牌。 */
    @PostMapping("/auth/login")
    public Result<Object> login(@Valid @RequestBody LoginRequest body) {
        return Result.ok(auth.login(body));
    }

    /** 返回当前身份及角色权限。 */
    @RequiresAuthentication
    @GetMapping("/auth/me")
    public Result<Object> me() {
        return Result.ok(auth.me());
    }

    /** 注销当前令牌。 */
    @RequiresAuthentication
    @PostMapping("/auth/logout")
    public Result<Object> logout(@RequestHeader("Authorization") String bearer) {
        return Result.ok(auth.logout(bearer));
    }
}
