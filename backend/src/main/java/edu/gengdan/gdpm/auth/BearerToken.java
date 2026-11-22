package edu.gengdan.gdpm.auth;

import org.apache.shiro.authc.AuthenticationToken;

/** 仅在当前请求中使用的随机令牌。 */
public class BearerToken implements AuthenticationToken {

    private final String value;

    /** 装配已配置的依赖或已验证的数据，供所属业务使用。 */
    public BearerToken(String value) {
        this.value = value;
    }

    /** 读取Principal字段。 */
    public Object getPrincipal() {
        return value;
    }

    /** 读取Credentials字段。 */
    public Object getCredentials() {
        return value;
    }
}
