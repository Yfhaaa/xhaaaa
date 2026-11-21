package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.ReviewRecord;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** ReviewService 模块业务契约。 */
public interface ReviewService extends IService<ReviewRecord> {
    /** 查询权限范围内的审核留痕。 */
    Object reviews();
    /** 记录成功操作或审核留痕。 */
    void record(String type, long businessId, Long studentId, boolean approved, String comment);
}
