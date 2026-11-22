package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.MaterialSubmitRequest;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.PageVO;
import edu.gengdan.gdpm.vo.SubmissionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 持 material:read 权限查询范围内各阶段全部历史版本。 */
@Tag(name = "过程材料")
@RestController
@RequestMapping("/api")
public class MaterialController {

    private final MaterialService materials;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public MaterialController(MaterialService service) {
        this.materials = service;
    }

    /** 持 material:read 权限查询范围内各阶段全部历史版本。 */
    @Operation(summary = "过程材料：Get /materials")
    @GetMapping("/materials")
    @RequiresAuthentication
    @RequiresPermissions("material:read")
    public Result<?> materials(
        @RequestParam(required = false) String stage,
        @ModelAttribute PageQuery pageQuery
    ) {
        return PageVO.optional(materials.listMaterials(stage), pageQuery);
    }

    /** 学生按阶段顺序提交本人文件；任务书由指导教师下达，退回重提创建新版本。 */
    @Operation(summary = "过程材料：Post /materials/{stage}")
    @PostMapping("/materials/{stage}")
    @RequiresAuthentication
    @RequiresPermissions("material:write")
    public Result<SubmissionVO> material(@PathVariable String stage, @Valid @RequestBody MaterialSubmitRequest b) {
        return Result.ok(materials.submit(stage, b));
    }

    /** 指导教师仅审核待审核的最新版本，定稿通过还需对应版本查重合格。 */
    @Operation(summary = "过程材料：Post /materials/{stage}/{id}/review")
    @PostMapping("/materials/{stage}/{id}/review")
    @RequiresAuthentication
    @RequiresPermissions("material:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<MessageVO> reviewMaterial(
        @PathVariable String stage,
        @PathVariable long id,
        @Valid @RequestBody ReviewRequest b
    ) {
        return Result.ok(materials.review(stage, id, b));
    }
}
