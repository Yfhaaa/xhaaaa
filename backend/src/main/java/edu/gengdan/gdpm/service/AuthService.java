package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.SysSession;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** AuthService 模块业务契约。 */
public interface AuthService extends IService<SysSession> {
    /** 验证账号后签发限时随机令牌。 */
    Object login(edu.gengdan.gdpm.dto.LoginRequest request);
    /** 返回当前身份及角色权限。 */
    Object me();
    /** 注销当前令牌。 */
    Object logout(String bearer);
}
