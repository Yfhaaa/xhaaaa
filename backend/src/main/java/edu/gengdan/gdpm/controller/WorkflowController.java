package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.WorkflowUpdateRequest;
import edu.gengdan.gdpm.service.WorkflowService;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
@Tag(name = "流程节点")
@RestController
@RequestMapping("/api")
public class WorkflowController {

    private final WorkflowService workflow;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public WorkflowController(WorkflowService service) {
        this.workflow = service;
    }

    /** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
    @Operation(summary = "流程节点：Get /workflow")
    @GetMapping("/workflow")
    @RequiresAuthentication
    public Result<?> workflow(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(workflow.workflow(), pageQuery);
    }

    /** 登录用户可读流程节点；仅管理员可修改截止时间和可选重复率阈值。 */
    @Operation(summary = "流程节点：Put /workflow/{id}")
    @PutMapping("/workflow/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<MessageVO> workflow(@PathVariable long id, @Valid @RequestBody WorkflowUpdateRequest b) {
        return Result.ok(workflow.workflow(id, b));
    }
}
