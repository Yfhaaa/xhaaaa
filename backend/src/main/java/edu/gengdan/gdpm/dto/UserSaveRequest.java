package edu.gengdan.gdpm.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** 管理员维护用户；创建时要求账号密码，修改时可省略。 */
public class UserSaveRequest {

    @NotBlank(message = "username不能为空且长度不能超过60", groups = Create.class)
    @Size(max = 60, message = "username不能为空且长度不能超过60", groups = Create.class)
    @Pattern(regexp = "[A-Za-z0-9_]{3,60}", message = "用户名请使用3至60位字母数字下划线", groups = Create.class)
    private String username = "";

    @NotBlank(message = "name不能为空且长度不能超过80")
    @Size(max = 80, message = "name不能为空且长度不能超过80")
    private String name = "";

    @NotBlank(message = "新用户必须设置密码", groups = Create.class)
    private String password = "";

    @NotNull(message = "roleId必须为正整数")
    @Min(value = 1, message = "请选择系统内置的三种角色")
    @Max(value = 3, message = "请选择系统内置的三种角色")
    private Long roleId;

    @NotNull(message = "majorId必须为正整数")
    @Min(value = 1, message = "majorId必须为正整数")
    private Long majorId;

    @NotNull(message = "classId必须为正整数")
    @Min(value = 1, message = "classId必须为正整数")
    private Long classId;

    @NotBlank(message = "cohort不能为空且长度不能超过20")
    @Size(max = 20, message = "cohort不能为空且长度不能超过20")
    private String cohort = "";

    @Min(value = 0, message = "账号状态不合法")
    @Max(value = 1, message = "账号状态不合法")
    private Integer enabled = 1;

    /** 返回username，供服务端执行权限与业务校验。 */
    public String getUsername() {
        return username;
    }

    /** 绑定username并去除首尾空白。 */
    public void setUsername(String value) {
        this.username = value == null ? "" : value.trim();
    }

    /** 返回name，供服务端执行权限与业务校验。 */
    public String getName() {
        return name;
    }

    /** 绑定name并去除首尾空白。 */
    public void setName(String value) {
        this.name = value == null ? "" : value.trim();
    }

    /** 返回password，供服务端执行权限与业务校验。 */
    public String getPassword() {
        return password;
    }

    /** 绑定password并去除首尾空白。 */
    public void setPassword(String value) {
        this.password = value == null ? "" : value.trim();
    }

    /** 返回roleId，供服务端执行权限与业务校验。 */
    public Long getRoleId() {
        return roleId;
    }

    /** 绑定roleId。 */
    public void setRoleId(Long value) {
        this.roleId = value;
    }

    /** 返回majorId，供服务端执行权限与业务校验。 */
    public Long getMajorId() {
        return majorId;
    }

    /** 绑定majorId。 */
    public void setMajorId(Long value) {
        this.majorId = value;
    }

    /** 返回classId，供服务端执行权限与业务校验。 */
    public Long getClassId() {
        return classId;
    }

    /** 绑定classId。 */
    public void setClassId(Long value) {
        this.classId = value;
    }

    /** 返回cohort，供服务端执行权限与业务校验。 */
    public String getCohort() {
        return cohort;
    }

    /** 绑定cohort并去除首尾空白。 */
    public void setCohort(String value) {
        this.cohort = value == null ? "" : value.trim();
    }

    /** 返回enabled，供服务端执行权限与业务校验。 */
    public Integer getEnabled() {
        return enabled;
    }

    /** 绑定enabled。 */
    public void setEnabled(Integer value) {
        this.enabled = value;
    }

    /** 新建时才要求不可变的身份字段。 */
    public interface Create {}
}
