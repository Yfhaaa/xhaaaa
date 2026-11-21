package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** sys_role_permission 表实体。
 * 关联表保留数据库复合主键；操作必须同时指定两个字段的条件，禁止按单一主键修改。
 */
@TableName("sys_role_permission")
public class SysRolePermission {

    /** 角色编号。 */
    @TableId(value = "role_id", type = IdType.INPUT)
    private Long roleId;

    /** 权限编号。 */
    private Long permissionId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }
}
