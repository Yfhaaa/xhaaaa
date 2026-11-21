package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.Major;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** BaseDataService 模块业务契约。 */
public interface BaseDataService extends IService<Major> {
    /** 查询页面所需基础下拉数据。 */
    Object reference();
    /** 维护专业、班级或届次。 */
    Object base(String type, Long baseId, Map<String, Object> body);
    /** 校验引用后删除基础数据。 */
    Object removeBase(String type, long baseId);
}
