package edu.gengdan.gdpm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 本人换密必须提交旧密码，新密码校验通过后注销全部会话。 */
public class PasswordChangeRequest {

    @NotBlank(message = "oldPassword不能为空且长度不能超过72")
    @Size(max = 72, message = "oldPassword不能为空且长度不能超过72")
    private String oldPassword = "";

    @NotBlank(message = "newPassword不能为空且长度不能超过32")
    @Size(max = 32, message = "newPassword不能为空且长度不能超过32")
    private String newPassword = "";

    /** 返回oldPassword，供服务端执行权限与业务校验。 */
    public String getOldPassword() {
        return oldPassword;
    }

    /** 绑定oldPassword并去除首尾空白。 */
    public void setOldPassword(String value) {
        this.oldPassword = value == null ? "" : value.trim();
    }

    /** 返回newPassword，供服务端执行权限与业务校验。 */
    public String getNewPassword() {
        return newPassword;
    }

    /** 绑定newPassword并去除首尾空白。 */
    public void setNewPassword(String value) {
        this.newPassword = value == null ? "" : value.trim();
    }
}
