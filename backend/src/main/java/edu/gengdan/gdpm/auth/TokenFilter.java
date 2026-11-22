package edu.gengdan.gdpm.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.gengdan.gdpm.common.Result;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.AccessControlFilter;

/** Shiro 原生过滤链中的无状态 Bearer 认证过滤器。 */
public class TokenFilter extends AccessControlFilter {

    private final ObjectMapper json;

    /** 装配已配置的依赖或已验证的数据，供所属业务使用。 */
    public TokenFilter(ObjectMapper json) {
        this.json = json;
    }

    /** 强制每个受保护请求重新验证令牌。 */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return false;
    }

    /** 认证当前请求或返回统一的未登录响应。 */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        String bearer = ((HttpServletRequest) request).getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            try {
                getSubject(request, response).login(new BearerToken(bearer.substring(7)));
                return true;
            } catch (AuthenticationException ignored) {}
        }
        HttpServletResponse http = (HttpServletResponse) response;
        http.setStatus(401);
        http.setContentType("application/json;charset=UTF-8");
        json.writeValue(http.getWriter(), Result.error(401, "登录已失效，请重新登录"));
        return false;
    }
}
