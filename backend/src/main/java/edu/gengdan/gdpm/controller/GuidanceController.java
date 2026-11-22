package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.GuidanceSaveRequest;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.GuidanceService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
@Tag(name = "指导记录")
@RestController
@RequestMapping("/api")
public class GuidanceController {

    private final GuidanceService guidance;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public GuidanceController(GuidanceService service) {
        this.guidance = service;
    }

    /** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
    @Operation(summary = "指导记录：Get /guidance")
    @GetMapping("/guidance")
    @RequiresAuthentication
    @RequiresPermissions("guidance:read")
    public Result<?> guidance(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(guidance.guidance(), pageQuery);
    }

    /** 指导教师仅可为本人指导学生新增记录；查询按当前身份隔离。 */
    @Operation(summary = "指导记录：Post /guidance")
    @PostMapping("/guidance")
    @RequiresAuthentication
    @RequiresPermissions("guidance:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<IdVO> guidance(@Valid @RequestBody GuidanceSaveRequest b) {
        return Result.ok(guidance.guidance(b));
    }
}
