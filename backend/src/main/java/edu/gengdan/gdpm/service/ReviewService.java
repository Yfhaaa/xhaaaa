package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.ReviewRecord;
import java.util.List;
import java.util.Map;

/** 持 material:read 权限读取范围内审核留痕。 业务审核成功后记录结果、审核人和意见，失败事务不保留成功留痕。 */
public interface ReviewService extends IService<ReviewRecord> {
    /** 持 material:read 权限读取范围内审核留痕。 */
    List<Map<String, Object>> reviews();

    /** 业务审核成功后记录结果、审核人和意见，失败事务不保留成功留痕。 */
    void record(String type, long businessId, Long studentId, boolean approved, String comment);
}
