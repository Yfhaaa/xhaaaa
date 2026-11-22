package edu.gengdan.gdpm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** 本人仅可修改姓名、邮箱和电话。 */
public class ProfileUpdateRequest {

    @NotBlank(message = "name不能为空且长度不能超过80")
    @Size(max = 80, message = "name不能为空且长度不能超过80")
    private String name = "";

    @Size(max = 120, message = "邮箱格式不正确")
    @Pattern(regexp = "(?:|[^ @]+@[^ @]+[.][^ @]+)", message = "邮箱格式不正确")
    private String email = "";

    @Pattern(regexp = "(?:|[+0-9 -]{6,30})", message = "联系电话格式不正确")
    private String phone = "";

    /** 返回name，供服务端执行权限与业务校验。 */
    public String getName() {
        return name;
    }

    /** 绑定name并去除首尾空白。 */
    public void setName(String value) {
        this.name = value == null ? "" : value.trim();
    }

    /** 返回email，供服务端执行权限与业务校验。 */
    public String getEmail() {
        return email;
    }

    /** 绑定email并去除首尾空白。 */
    public void setEmail(String value) {
        this.email = value == null ? "" : value.trim();
    }

    /** 返回phone，供服务端执行权限与业务校验。 */
    public String getPhone() {
        return phone;
    }

    /** 绑定phone并去除首尾空白。 */
    public void setPhone(String value) {
        this.phone = value == null ? "" : value.trim();
    }
}
