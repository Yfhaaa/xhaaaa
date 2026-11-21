package edu.gengdan.gdpm.entity;

import com.baomidou.mybatisplus.annotation.*;

/** sys_user_role 表实体。
 * 关联表保留数据库复合主键；操作必须同时指定两个字段的条件，禁止按单一主键修改。
 */
@TableName("sys_user_role")
public class SysUserRole {

    /** 用户编号。 */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    /** 角色编号。 */
    private Long roleId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
