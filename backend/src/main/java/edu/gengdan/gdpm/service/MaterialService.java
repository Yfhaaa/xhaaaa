package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.FileRecord;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** MaterialService 模块业务契约。 */
public interface MaterialService extends IService<FileRecord> {
    /** 校验阶段顺序后保存新材料版本。 */
    Object submit(String stage, Map<String, Object> body);
    /** 审核当前待审核的最新材料。 */
    Object review(String stage, long materialId, Map<String, Object> body);
    /** 查询指定阶段的最新版本。 */
    Map<String, Object> latest(long studentId, String stage);
    /** 判断最新材料是否审核通过。 */
    boolean passed(long studentId, String stage);
    /** 查询范围内全部材料版本。 */
    List<Map<String, Object>> listMaterials(String stage);
}
