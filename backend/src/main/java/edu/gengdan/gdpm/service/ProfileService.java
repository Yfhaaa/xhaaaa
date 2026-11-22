package edu.gengdan.gdpm.service;

import edu.gengdan.gdpm.dto.PasswordChangeRequest;
import edu.gengdan.gdpm.dto.ProfileUpdateRequest;
import edu.gengdan.gdpm.vo.UserVO;

/** 已登录用户仅维护本人姓名和联系方式，禁止更改角色与数据范围。 本人通过旧密码及新密码强度校验后换密，随即使所有既有会话失效。 */
public interface ProfileService {
    /** 已登录用户仅维护本人姓名和联系方式，禁止更改角色与数据范围。 */
    UserVO update(ProfileUpdateRequest body);

    /** 本人通过旧密码及新密码强度校验后换密，随即使所有既有会话失效。 */
    void password(PasswordChangeRequest body);
}
