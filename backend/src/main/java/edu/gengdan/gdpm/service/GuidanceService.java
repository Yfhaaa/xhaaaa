package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.GuidanceRecord;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** GuidanceService 模块业务契约。 */
public interface GuidanceService extends IService<GuidanceRecord> {
    /** 查询或新增指导记录。 */
    Object guidance();
    /** 查询或新增指导记录。 */
    Object guidance(Map<String, Object> body);
}
