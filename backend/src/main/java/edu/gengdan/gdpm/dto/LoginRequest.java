package edu.gengdan.gdpm.dto;

import javax.validation.constraints.NotBlank;

/** 登录请求，密码不写入操作日志。 */
public class LoginRequest {

    @NotBlank(message = "请求参数格式不正确")
    private String username;

    @NotBlank(message = "请求参数格式不正确")
    private String password;

    /** 读取登录账号。 */
    public String getUsername() {
        return username;
    }

    /** 设置登录账号。 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 读取密码哈希。密码相关值不得进入响应或操作日志。 */
    public String getPassword() {
        return password;
    }

    /** 设置密码哈希。密码相关值不得进入响应或操作日志。 */
    public void setPassword(String password) {
        this.password = password;
    }
}
