package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.PasswordChangeRequest;
import edu.gengdan.gdpm.dto.ProfileUpdateRequest;
import edu.gengdan.gdpm.entity.SysSession;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.mapper.UserMapper;
import edu.gengdan.gdpm.service.ProfileService;
import edu.gengdan.gdpm.vo.UserVO;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 已登录用户仅维护本人姓名和联系方式，禁止更改角色与数据范围。 本人通过旧密码及新密码强度校验后换密，随即使所有既有会话失效。 */
@Service
public class ProfileServiceImpl extends DomainSupport<UserMapper, User> implements ProfileService {

    /** 已登录用户仅维护本人姓名和联系方式，禁止更改角色与数据范围。 */
    @OperLog("更新个人资料")
    @Transactional
    public UserVO update(ProfileUpdateRequest body) {
        validate(body);
        User user = lockStudent(auth.uid());
        user.setName(body.getName());
        String email = body.getEmail(),
            phone = body.getPhone();
        user.setEmail(email);
        user.setPhone(phone);
        updateById(user);
        return auth.me();
    }

    /** 本人通过旧密码及新密码强度校验后换密，随即使所有既有会话失效。 */
    @OperLog("修改个人密码")
    @Transactional
    public void password(PasswordChangeRequest body) {
        validate(body);
        User user = lockStudent(auth.uid());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String old = body.getOldPassword(),
            next = body.getNewPassword();
        require(encoder.matches(old, user.getPassword()), "旧密码不正确");
        require(
            next.matches("(?=.*[A-Za-z])(?=.*[0-9]).{8,32}") &&
                next.getBytes(StandardCharsets.UTF_8).length <= 72,
            "新密码需8至32位，包含字母和数字"
        );
        require(!old.equals(next), "新密码不能与旧密码相同");
        user.setPassword(encoder.encode(next));
        updateById(user);
        sessions.delete(query(SysSession.class).eq(SysSession::getUserId, auth.uid()));
    }
}
