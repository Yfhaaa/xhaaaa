package edu.gengdan.gdpm.service.impl;

import edu.gengdan.gdpm.common.EntityViews;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.dto.WorkflowUpdateRequest;
import edu.gengdan.gdpm.entity.WorkflowStep;
import edu.gengdan.gdpm.mapper.WorkflowStepMapper;
import edu.gengdan.gdpm.service.WorkflowService;
import edu.gengdan.gdpm.vo.MessageVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
@Service
public class WorkflowServiceImpl
    extends DomainSupport<WorkflowStepMapper, WorkflowStep>
    implements WorkflowService
{

    /** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
    public List<Map<String, Object>> workflow() {
        auth.uid();
        List<Map<String, Object>> result = new ArrayList<>();
        for (WorkflowStep step : lambdaQuery().orderByAsc(WorkflowStep::getSortOrder).list()) {
            result.add(EntityViews.view(step));
        }
        return result;
    }

    /** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
    @OperLog("配置截止时间")
    @Transactional
    public MessageVO workflow(long workflowId, WorkflowUpdateRequest body) {
        validate(body);
        admin();
        WorkflowStep step = required(getById(workflowId));
        step.setDeadline(LocalDateTime.parse(body.getDeadline().replace(" ", "T")));
        if (body.getPlagiarismThreshold() != null) {
            step.setPlagiarismThreshold(
                body.getPlagiarismThreshold()
            );
        }
        updateById(step);
        return new MessageVO("截止时间已更新");
    }
}
