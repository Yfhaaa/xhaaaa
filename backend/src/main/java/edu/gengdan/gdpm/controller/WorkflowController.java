package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.WorkflowService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Workflow 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class WorkflowController {

    private final WorkflowService workflow;

    public WorkflowController(WorkflowService service) {
        this.workflow = service;
    }

    /** Workflow 模块接口：/workflow。 */
    @GetMapping("/workflow")
    @RequiresAuthentication
    public Result<Object> workflow(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(workflow.workflow(), pageQuery));
    }

    /** Workflow 模块接口：/workflow/{id}。 */
    @PutMapping("/workflow/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> workflow(@PathVariable long id, @RequestBody Map<String, Object> b) {
        return Result.ok(workflow.workflow(id, b));
    }
}
