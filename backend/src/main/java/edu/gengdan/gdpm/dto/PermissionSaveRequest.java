package edu.gengdan.gdpm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** 管理员维护权限；创建时必须提供资源:操作编码。 */
public class PermissionSaveRequest {

    @NotBlank(message = "name不能为空且长度不能超过80")
    @Size(max = 80, message = "name不能为空且长度不能超过80")
    private String name = "";

    @NotBlank(message = "code不能为空且长度不能超过80", groups = Create.class)
    @Size(max = 80, message = "code不能为空且长度不能超过80", groups = Create.class)
    @Pattern(regexp = "[a-z]+:[a-z]+", message = "权限标识格式为资源:操作", groups = Create.class)
    private String code = "";

    /** 返回name，供服务端执行权限与业务校验。 */
    public String getName() {
        return name;
    }

    /** 绑定name并去除首尾空白。 */
    public void setName(String value) {
        this.name = value == null ? "" : value.trim();
    }

    /** 返回code，供服务端执行权限与业务校验。 */
    public String getCode() {
        return code;
    }

    /** 绑定code并去除首尾空白。 */
    public void setCode(String value) {
        this.code = value == null ? "" : value.trim();
    }

    /** 新建时才要求不可变的身份字段。 */
    public interface Create {}
}
