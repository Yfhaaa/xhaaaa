package edu.gengdan.gdpm.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.gengdan.gdpm.entity.User;
import java.util.List;

/** 当前用户的脱敏资料和角色权限，维持个人资料接口的下划线字段。 */
public class UserVO {
    public final Long id;
    public final String username;
    public final String name;
    @JsonProperty("major_id")
    public final Long majorId;
    @JsonProperty("class_id")
    public final Long classId;
    public final String cohort;
    public final Integer enabled;
    public final String email;
    public final String phone;
    public final List<String> roles;
    public final List<String> permissions;

    /** 仅绑定本人已鉴权的身份和权限，禁止传入密码字段。 */
    public UserVO(User user, List<String> roles, List<String> permissions) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.majorId = user.getMajorId();
        this.classId = user.getClassId();
        this.cohort = user.getCohort();
        this.enabled = user.getEnabled();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.roles = roles;
        this.permissions = permissions;
    }
}
