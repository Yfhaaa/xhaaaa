package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.auth.Auth;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.LoginRequest;
import edu.gengdan.gdpm.entity.SysSession;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.mapper.SysSessionMapper;
import edu.gengdan.gdpm.service.AuthService;
import edu.gengdan.gdpm.vo.LoginVO;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.UserVO;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 匿名校验启用账号与密码，签发十二小时令牌，仅持久化摘要。 已登录用户读取本人脱敏资料、角色与实时权限。 已登录用户注销当前会话，原令牌立即失效。 */
@Service
public class AuthServiceImpl extends DomainSupport<SysSessionMapper, SysSession> implements AuthService {

    /** 匿名校验启用账号与密码，签发十二小时令牌，仅持久化摘要。 */
    @OperLog("登录")
    @Transactional
    public LoginVO login(LoginRequest request) {
        User user = users.selectOne(query(User.class).eq(User::getUsername, request.getUsername().trim()));
        if (
            user == null ||
            user.getEnabled() != 1 ||
            !new BCryptPasswordEncoder().matches(
                request.getPassword(),
                user.getPassword()
            )
        ) {
            throw new ApiException(401, "用户名或密码错误，或账号已停用");
        }
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        remove(query(SysSession.class).lt(SysSession::getExpiresAt, LocalDateTime.now()));
        SysSession record1 = new SysSession();
        record1.setTokenHash(Auth.hash(token));
        record1.setUserId(user.getId());
        record1.setExpiresAt(LocalDateTime.now().plusHours(12));
        save(record1);
        return new LoginVO(token, user);
    }

    /** 已登录用户读取本人脱敏资料、角色与实时权限。 */
    public UserVO me() {
        return auth.me();
    }

    /** 已登录用户注销当前会话，原令牌立即失效。 */
    @OperLog("退出登录")
    @Transactional
    public MessageVO logout(String bearer) {
        auth.uid();
        remove(
            query(SysSession.class).eq(
                SysSession::getTokenHash,
                Auth.hash(bearer.substring(7))
            )
        );
        return new MessageVO("已退出");
    }
}
