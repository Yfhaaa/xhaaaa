package edu.gengdan.gdpm.vo;

import edu.gengdan.gdpm.entity.User;

/** 匿名验证账号后签发的令牌与脱敏用户信息。 */
public class LoginVO {

    public final String token;

    public final User user;

    /** 根据已通过业务校验的结果构造响应，保持原有 JSON 字段。 */
    public LoginVO(
        String token,
        User user
    ) {
        this.token = token;
        this.user = user;
    }
}
