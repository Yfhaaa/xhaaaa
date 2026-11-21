package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.*;

import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RoleService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class RoleServiceImpl extends DomainSupport<SysRoleMapper, SysRole> implements RoleService {

    /** 查询角色及其权限列表。 */
    public Object roles() {
        admin();
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysRole role : lambdaQuery().orderByAsc(SysRole::getId).list()) {
            Map<String, Object> view = EntityViews.view(role);
            List<Long> ids = new ArrayList<>();
            for (SysRolePermission permission : rolePermissions.selectList(
                query(SysRolePermission.class).eq(SysRolePermission::getRoleId, role.getId())
            )) {
                ids.add(permission.getPermissionId());
            }
            view.put("permission_ids", ids);
            result.add(view);
        }
        return result;
    }

    /** 维护角色权限关联。 */
    @OperLog("维护角色权限")
    @Transactional
    public Object role(long roleId, Map<String, Object> body) {
        admin();
        SysRole role = required(getById(roleId));
        Object raw = body.get("permissionIds");
        require(raw instanceof List, "权限列表格式不正确");
        Set<Long> ids = new LinkedHashSet<>();
        for (Object value : (List<?>) raw) {
            long permissionId = id(value);
            required(permissions.selectById(permissionId));
            ids.add(permissionId);
        }
        if (roleId == 1) {
            require(ids.contains(11L), "管理员必须保留系统管理权限");
        }
        role.setName(Params.text(body, "name", 60));
        updateById(role);
        rolePermissions.delete(query(SysRolePermission.class).eq(SysRolePermission::getRoleId, roleId));
        for (long permissionId : ids) {
            rolePermissions.insert(entity(SysRolePermission.class, "role_id", roleId, "permission_id", permissionId));
        }
        return Map.of("message", "角色权限已更新");
    }
}
