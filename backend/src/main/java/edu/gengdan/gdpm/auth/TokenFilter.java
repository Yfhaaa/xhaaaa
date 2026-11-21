package edu.gengdan.gdpm.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.gengdan.gdpm.common.Result;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.AccessControlFilter;

/** Shiro 原生过滤链中的无状态 Bearer 认证过滤器。 */
public class TokenFilter extends AccessControlFilter {

    private final ObjectMapper json;

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
