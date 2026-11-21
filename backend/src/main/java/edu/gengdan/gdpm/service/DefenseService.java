package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.DefenseInfo;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** DefenseService 模块业务契约。 */
public interface DefenseService extends IService<DefenseInfo> {
    /** 查询数据范围内的答辩组。 */
    Object groups();
    /** 创建答辩组并验证教师身份。 */
    Object group(Map<String, Object> body);
    /** 查询数据范围内的答辩安排。 */
    Object defenses();
    /** 校验定稿后安排学生答辩。 */
    Object arrange(Map<String, Object> body);
}
