package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.*;

import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** AuthService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class AuthServiceImpl extends DomainSupport<SysSessionMapper, SysSession> implements AuthService {

    /** 验证账号后签发限时随机令牌。 */
    @OperLog("登录")
    @Transactional
    public Object login(edu.gengdan.gdpm.dto.LoginRequest request) {
        User user = users.selectOne(query(User.class).eq(User::getUsername, request.getUsername().trim()));
        if (
            user == null ||
            user.getEnabled() != 1 ||
            !new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(
                request.getPassword(),
                user.getPassword()
            )
        ) {
            throw new ApiException(401, "用户名或密码错误，或账号已停用");
        }
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        remove(query(SysSession.class).lt(SysSession::getExpiresAt, LocalDateTime.now()));
        save(
            entity(
                SysSession.class,
                "token_hash",
                edu.gengdan.gdpm.auth.Auth.hash(token),
                "user_id",
                user.getId(),
                "expires_at",
                LocalDateTime.now().plusHours(12)
            )
        );
        return Map.of("token", token, "user", user);
    }

    /** 返回当前身份及角色权限。 */
    public Object me() {
        return auth.me();
    }

    /** 注销当前令牌。 */
    @OperLog("退出登录")
    @Transactional
    public Object logout(String bearer) {
        auth.uid();
        remove(
            query(SysSession.class).eq(SysSession::getTokenHash, edu.gengdan.gdpm.auth.Auth.hash(bearer.substring(7)))
        );
        return Map.of("message", "已退出");
    }
}
