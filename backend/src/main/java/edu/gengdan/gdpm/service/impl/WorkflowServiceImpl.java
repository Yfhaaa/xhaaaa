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

/** WorkflowService 模块实现，校验权限与业务状态后持久化实体。 */
@Service
public class WorkflowServiceImpl extends DomainSupport<WorkflowStepMapper, WorkflowStep> implements WorkflowService {

    /** 查询或更新流程截止时间。 */
    public Object workflow() {
        auth.uid();
        List<Map<String, Object>> result = new ArrayList<>();
        for (WorkflowStep step : lambdaQuery().orderByAsc(WorkflowStep::getSortOrder).list()) {
            result.add(EntityViews.view(step));
        }
        return result;
    }

    /** 查询或更新流程截止时间。 */
    @OperLog("配置截止时间")
    @Transactional
    public Object workflow(long workflowId, Map<String, Object> body) {
        admin();
        WorkflowStep step = required(getById(workflowId));
        step.setDeadline(Params.time(body, "deadline").toLocalDateTime());
        updateById(step);
        return Map.of("message", "截止时间已更新");
    }
}
