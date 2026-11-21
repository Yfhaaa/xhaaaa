package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.ScoreInfo;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** ScoreService 模块业务契约。 */
public interface ScoreService extends IService<ScoreInfo> {
    /** 查询范围内可见成绩并隐藏未发布成绩。 */
    Object scores();
    /** 计算综合成绩并校验分数和权重。 */
    Object score(Map<String, Object> body);
    /** 发布成绩并锁定后续修改。 */
    Object publish(long scoreId);
}
