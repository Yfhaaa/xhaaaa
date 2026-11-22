package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.PermissionSaveRequest;
import edu.gengdan.gdpm.entity.SysPermission;
import edu.gengdan.gdpm.mapper.SysPermissionMapper;
import edu.gengdan.gdpm.service.PermissionService;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 管理员读取权限目录，供角色配置使用。 管理员创建资源:操作编码或修改权限名称，已有编码不允许变更。 */
@Service
public class PermissionServiceImpl
    extends DomainSupport<SysPermissionMapper, SysPermission>
    implements PermissionService
{

    /** 管理员读取权限目录，供角色配置使用。 */
    public List<SysPermission> permissions() {
        admin();
        return lambdaQuery().orderByAsc(SysPermission::getId).list();
    }

    /** 管理员创建资源:操作编码或修改权限名称，已有编码不允许变更。 */
    @OperLog("维护权限")
    @Transactional
    public IdVO permission(Long permissionId, PermissionSaveRequest body) {
        validate(body);
        admin();
        SysPermission permission =
            permissionId == null ? new SysPermission() : required(getById(permissionId));
        permission.setName(body.getName());
        if (permissionId == null) {
            validate(body, PermissionSaveRequest.Create.class);
            String code = body.getCode();
            permission.setCode(code);
        }
        saveOrUpdate(permission);
        return new IdVO(permission.getId());
    }
}
