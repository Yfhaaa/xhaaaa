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

/** PermissionService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class PermissionServiceImpl
    extends DomainSupport<SysPermissionMapper, SysPermission>
    implements PermissionService
{

    /** 查询权限目录。 */
    public Object permissions() {
        admin();
        return lambdaQuery().orderByAsc(SysPermission::getId).list();
    }

    /** 维护权限名称与编码。 */
    @OperLog("维护权限")
    @Transactional
    public Object permission(Long permissionId, Map<String, Object> body) {
        admin();
        SysPermission permission = permissionId == null ? new SysPermission() : required(getById(permissionId));
        permission.setName(Params.text(body, "name", 80));
        if (permissionId == null) {
            String code = Params.text(body, "code", 80);
            require(code.matches("[a-z]+:[a-z]+"), "权限标识格式为资源:操作");
            permission.setCode(code);
        }
        saveOrUpdate(permission);
        return Map.of("id", permission.getId());
    }
}
