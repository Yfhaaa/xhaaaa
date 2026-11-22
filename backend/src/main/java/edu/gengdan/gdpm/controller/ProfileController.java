package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PasswordChangeRequest;
import edu.gengdan.gdpm.dto.ProfileUpdateRequest;
import edu.gengdan.gdpm.service.ProfileService;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 已登录用户仅维护本人姓名和联系方式，禁止更改角色与数据范围。 本人通过旧密码及新密码强度校验后换密，随即使所有既有会话失效。 */
@Tag(name = "个人中心")
@RestController
@RequestMapping("/api")
@RequiresAuthentication
public class ProfileController {

    private final ProfileService service;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public ProfileController(ProfileService service) {
        this.service = service;
    }

    /** 已登录用户仅维护本人姓名和联系方式，禁止更改角色与数据范围。 */
    @Operation(summary = "个人中心：Put /profile")
    @PutMapping("/profile")
    public Result<UserVO> update(@Valid @RequestBody ProfileUpdateRequest body) {
        return Result.ok(service.update(body));
    }

    /** 本人通过旧密码及新密码强度校验后换密，随即使所有既有会话失效。 */
    @Operation(summary = "个人中心：Post /profile/password")
    @PostMapping("/profile/password")
    public Result<MessageVO> password(@Valid @RequestBody PasswordChangeRequest body) {
        service.password(body);
        return Result.ok(new MessageVO("密码已修改，请重新登录"));
    }
}
