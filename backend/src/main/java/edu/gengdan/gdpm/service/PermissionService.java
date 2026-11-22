package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.PermissionSaveRequest;
import edu.gengdan.gdpm.entity.SysPermission;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.List;

/** 管理员读取权限目录，供角色配置使用。 管理员创建资源:操作编码或修改权限名称，已有编码不允许变更。 */
public interface PermissionService extends IService<SysPermission> {
    /** 管理员读取权限目录，供角色配置使用。 */
    List<SysPermission> permissions();

    /** 管理员创建资源:操作编码或修改权限名称，已有编码不允许变更。 */
    IdVO permission(Long permissionId, PermissionSaveRequest body);
}
