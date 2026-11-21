package edu.gengdan.gdpm.service.impl;

import static edu.gengdan.gdpm.common.ApiException.require;
import static edu.gengdan.gdpm.common.Values.*;

import edu.gengdan.gdpm.common.*;
import edu.gengdan.gdpm.entity.*;
import edu.gengdan.gdpm.mapper.*;
import edu.gengdan.gdpm.service.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ReviewService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class ReviewServiceImpl extends DomainSupport<ReviewRecordMapper, ReviewRecord> implements ReviewService {

    /** 查询权限范围内的审核留痕。 */
    public Object reviews() {
        auth.permission("material:read");
        return reports.reviews(auth.scope());
    }

    /** 记录成功操作或审核留痕。 */
    public void record(String type, long businessId, Long studentId, boolean approved, String comment) {
        save(
            entity(
                ReviewRecord.class,
                "business_type",
                type,
                "business_id",
                businessId,
                "student_id",
                studentId,
                "reviewer_id",
                auth.uid(),
                "result",
                BusinessStatus.reviewed(approved),
                "comment",
                comment
            )
        );
    }
}
