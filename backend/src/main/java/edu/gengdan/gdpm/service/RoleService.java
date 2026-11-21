package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.SysRole;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** RoleService 模块业务契约。 */
public interface RoleService extends IService<SysRole> {
    /** 查询角色及其权限列表。 */
    Object roles();
    /** 维护角色权限关联。 */
    Object role(long roleId, Map<String, Object> body);
}
