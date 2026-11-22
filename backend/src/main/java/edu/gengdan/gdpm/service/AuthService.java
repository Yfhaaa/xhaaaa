package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.LoginRequest;
import edu.gengdan.gdpm.entity.SysSession;
import edu.gengdan.gdpm.vo.LoginVO;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.UserVO;

/** 匿名校验启用账号与密码，签发十二小时令牌，仅持久化摘要。 已登录用户读取本人脱敏资料、角色与实时权限。 已登录用户注销当前会话，原令牌立即失效。 */
public interface AuthService extends IService<SysSession> {
    /** 匿名校验启用账号与密码，签发十二小时令牌，仅持久化摘要。 */
    LoginVO login(LoginRequest request);

    /** 已登录用户读取本人脱敏资料、角色与实时权限。 */
    UserVO me();

    /** 已登录用户注销当前会话，原令牌立即失效。 */
    MessageVO logout(String bearer);
}
