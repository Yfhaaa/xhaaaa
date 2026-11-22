package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;

import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.RoleSaveRequest;
import edu.gengdan.gdpm.entity.SysRole;
import edu.gengdan.gdpm.entity.SysRolePermission;
import edu.gengdan.gdpm.mapper.SysRoleMapper;
import edu.gengdan.gdpm.service.RoleService;
import edu.gengdan.gdpm.vo.MessageVO;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 管理员查询内置角色及关联权限编号。 管理员维护角色名称及权限，管理员角色必须保留系统管理权限。 */
@Service
public class RoleServiceImpl extends DomainSupport<SysRoleMapper, SysRole> implements RoleService {

    /** 管理员查询内置角色及关联权限编号。 */
    public List<Map<String, Object>> roles() {
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

    /** 管理员维护角色名称及权限，管理员角色必须保留系统管理权限。 */
    @OperLog("维护角色权限")
    @Transactional
    public MessageVO role(long roleId, RoleSaveRequest body) {
        validate(body);
        admin();
        SysRole role = required(getById(roleId));
        List<Long> permissionIds = body.getPermissionIds();
        Set<Long> ids = new LinkedHashSet<>();
        for (long permissionId : permissionIds) {
            required(permissions.selectById(permissionId));
            ids.add(permissionId);
        }
        if (roleId == 1) {
            require(ids.contains(11L), "管理员必须保留系统管理权限");
        }
        role.setName(body.getName());
        updateById(role);
        rolePermissions.delete(query(SysRolePermission.class).eq(SysRolePermission::getRoleId, roleId));
        for (long permissionId : ids) {
            SysRolePermission record4 = new SysRolePermission();
            record4.setRoleId(roleId);
            record4.setPermissionId(permissionId);
            rolePermissions.insert(record4);
        }
        return new MessageVO("角色权限已更新");
    }
}
