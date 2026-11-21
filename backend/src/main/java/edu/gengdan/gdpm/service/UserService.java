package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.User;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** UserService 模块业务契约。 */
public interface UserService extends IService<User> {
    /** 查询系统用户及角色信息。 */
    Object users();
    /** 维护用户、角色及令牌失效状态。 */
    Object user(Long userId, Map<String, Object> body);
    /** 查询当前身份数据范围内的学生。 */
    List<Map<String, Object>> students();
}
