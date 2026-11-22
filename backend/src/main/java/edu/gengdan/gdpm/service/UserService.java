package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.UserSaveRequest;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.vo.IdVO;
import java.util.List;
import java.util.Map;

/** 管理员读取系统用户和角色信息，不返回密码。 学生仅查询本人，教师仅查询指导学生，管理员查询全部学生。 管理员维护用户与角色；禁止破坏自身管理员身份，停用或修改使既有令牌失效。 */
public interface UserService extends IService<User> {
    /** 管理员读取系统用户和角色信息，不返回密码。 */
    List<Map<String, Object>> users();

    /** 管理员维护用户与角色；禁止破坏自身管理员身份，停用或修改使既有令牌失效。 */
    IdVO user(Long userId, UserSaveRequest body);

    /** 学生仅查询本人，教师仅查询指导学生，管理员查询全部学生。 */
    List<Map<String, Object>> students();
}
