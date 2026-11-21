package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.SysPermission;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** PermissionService 模块业务契约。 */
public interface PermissionService extends IService<SysPermission> {
    /** 查询权限目录。 */
    Object permissions();
    /** 维护权限名称与编码。 */
    Object permission(Long permissionId, Map<String, Object> body);
}
