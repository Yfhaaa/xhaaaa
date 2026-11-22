package edu.gengdan.gdpm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 管理员发布所有登录用户可见的通知。 */
public class NoticeSaveRequest {

    @NotBlank(message = "title不能为空且长度不能超过200")
    @Size(max = 200, message = "title不能为空且长度不能超过200")
    private String title = "";

    @NotBlank(message = "content不能为空且长度不能超过10000")
    @Size(max = 10000, message = "content不能为空且长度不能超过10000")
    private String content = "";

    /** 返回title，供服务端执行权限与业务校验。 */
    public String getTitle() {
        return title;
    }

    /** 绑定title并去除首尾空白。 */
    public void setTitle(String value) {
        this.title = value == null ? "" : value.trim();
    }

    /** 返回content，供服务端执行权限与业务校验。 */
    public String getContent() {
        return content;
    }

    /** 绑定content并去除首尾空白。 */
    public void setContent(String value) {
        this.content = value == null ? "" : value.trim();
    }
}
