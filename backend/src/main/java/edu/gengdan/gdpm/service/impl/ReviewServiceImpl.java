package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.common.BusinessStatus;
import edu.gengdan.gdpm.entity.ReviewRecord;
import edu.gengdan.gdpm.mapper.ReviewRecordMapper;
import edu.gengdan.gdpm.service.ReviewService;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** 持 material:read 权限读取范围内审核留痕。 业务审核成功后记录结果、审核人和意见，失败事务不保留成功留痕。 */
@Service
public class ReviewServiceImpl
    extends DomainSupport<ReviewRecordMapper, ReviewRecord>
    implements ReviewService
{

    /** 持 material:read 权限读取范围内审核留痕。 */
    public List<Map<String, Object>> reviews() {
        auth.permission("material:read");
        return reports.reviews(auth.scope());
    }

    /** 业务审核成功后记录结果、审核人和意见，失败事务不保留成功留痕。 */
    public void record(String type, long businessId, Long studentId, boolean approved, String comment) {
        ReviewRecord record3 = new ReviewRecord();
        record3.setBusinessType(type);
        record3.setBusinessId(businessId);
        record3.setStudentId(studentId);
        record3.setReviewerId(auth.uid());
        record3.setResult(BusinessStatus.reviewed(approved));
        record3.setComment(comment);
        save(record3);
    }
}
