package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.dto.WorkflowUpdateRequest;
import edu.gengdan.gdpm.entity.WorkflowStep;
import edu.gengdan.gdpm.vo.MessageVO;
import java.util.List;
import java.util.Map;

/** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
public interface WorkflowService extends IService<WorkflowStep> {
    /** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
    List<Map<String, Object>> workflow();

    /** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
    MessageVO workflow(long workflowId, WorkflowUpdateRequest body);
}
