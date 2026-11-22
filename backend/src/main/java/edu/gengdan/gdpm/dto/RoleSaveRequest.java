package edu.gengdan.gdpm.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 管理员维护角色名称和权限，管理员角色保留系统管理权限。 */
public class RoleSaveRequest {

    @NotBlank(message = "name不能为空且长度不能超过60")
    @Size(max = 60, message = "name不能为空且长度不能超过60")
    private String name = "";

    @NotNull(message = "权限列表格式不正确")
    private List<@NotNull(message = "权限列表格式不正确") Long> permissionIds;

    /** 返回name，供服务端执行权限与业务校验。 */
    public String getName() {
        return name;
    }

    /** 绑定name并去除首尾空白。 */
    public void setName(String value) {
        this.name = value == null ? "" : value.trim();
    }

    /** 返回permissionIds，供服务端执行权限与业务校验。 */
    public List<@NotNull(message = "权限列表格式不正确") Long> getPermissionIds() {
        return permissionIds;
    }

    /** 绑定permissionIds。 */
    public void setPermissionIds(List<@NotNull(message = "权限列表格式不正确") Long> value) {
        this.permissionIds = value;
    }
}
