package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.ArchiveRecord;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** StatisticsService 模块业务契约。 */
public interface StatisticsService extends IService<ArchiveRecord> {
    /** 计算九个业务节点的完成进度。 */
    List<Map<String, Object>> progress();
    /** 生成当前角色的待办、进度和提醒。 */
    Object dashboard();
    /** 按允许维度与筛选条件统计学生进度。 */
    Object statistics(Map<String, String> filters);
}
