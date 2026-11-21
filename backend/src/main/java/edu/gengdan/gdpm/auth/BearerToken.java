package edu.gengdan.gdpm.auth;

import org.apache.shiro.authc.AuthenticationToken;

/** 仅在当前请求中使用的随机令牌。 */
public class BearerToken implements AuthenticationToken {

    private final String value;

    public BearerToken(String value) {
        this.value = value;
    }

    public Object getPrincipal() {
        return value;
    }

    public Object getCredentials() {
        return value;
    }
}
