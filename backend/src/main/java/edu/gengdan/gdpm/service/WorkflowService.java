package edu.gengdan.gdpm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import edu.gengdan.gdpm.entity.WorkflowStep;
import java.util.*;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/** WorkflowService 模块业务契约。 */
public interface WorkflowService extends IService<WorkflowStep> {
    /** 查询或更新流程截止时间。 */
    Object workflow();
    /** 查询或更新流程截止时间。 */
    Object workflow(long workflowId, Map<String, Object> body);
}
