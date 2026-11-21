package edu.gengdan.gdpm.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.*;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

/** 从持久化令牌和 RBAC 关联表完成真实认证与授权。 */
@Component
public class TokenRealm extends AuthorizingRealm {

    private final SysSessionMapper sessions;
    private final UserMapper users;
    private final ReportMapper reports;

    public TokenRealm(SysSessionMapper sessions, UserMapper users, ReportMapper reports) {
        this.sessions = sessions;
        this.users = users;
        this.reports = reports;
        setAuthenticationCachingEnabled(false);
        setAuthorizationCachingEnabled(false);
    }

    /** 仅接受 Bearer 令牌认证。 */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof BearerToken;
    }

    /** 验证持久化令牌、有效期与账号状态。 */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        SysSession session = sessions.selectOne(
            new LambdaQueryWrapper<SysSession>()
                .eq(SysSession::getTokenHash, Auth.hash(token.getCredentials().toString()))
                .gt(SysSession::getExpiresAt, LocalDateTime.now())
        );
        if (session == null) {
            throw new IncorrectCredentialsException();
        }
        User user = users.selectById(session.getUserId());
        if (user == null || user.getEnabled() != 1) {
            throw new DisabledAccountException();
        }
        return new SimpleAuthenticationInfo(user.getId(), token.getCredentials(), getName());
    }

    /** 加载当前用户的角色与权限。 */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        long userId = ((Number) principals.getPrimaryPrincipal()).longValue();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(new HashSet<>(reports.roles(userId)));
        info.setStringPermissions(new HashSet<>(reports.permissions(userId)));
        return info;
    }
}
