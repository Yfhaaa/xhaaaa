package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.RoleSaveRequest;
import edu.gengdan.gdpm.entity.SysRole;
import edu.gengdan.gdpm.vo.MessageVO;
import java.util.List;
import java.util.Map;

/** 管理员查询内置角色及关联权限编号。 管理员维护角色名称及权限，管理员角色必须保留系统管理权限。 */
public interface RoleService extends IService<SysRole> {
    /** 管理员查询内置角色及关联权限编号。 */
    List<Map<String, Object>> roles();

    /** 管理员维护角色名称及权限，管理员角色必须保留系统管理权限。 */
    MessageVO role(long roleId, RoleSaveRequest body);
}
