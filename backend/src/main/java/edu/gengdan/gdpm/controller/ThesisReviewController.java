package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.ThesisReviewAssignRequest;
import edu.gengdan.gdpm.dto.ThesisReviewRequest;
import edu.gengdan.gdpm.service.ThesisReviewService;
import edu.gengdan.gdpm.vo.CountVO;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 仅指定评阅教师、学生本人及管理员可查询评阅详情。 管理员为定稿通过学生批量或单独分配非指导教师，任一无效项使整批回滚。 指定评阅教师仅一次提交百分制分数、意见与答辩资格，状态从待评阅改为已评阅。 */
@Tag(name = "论文评阅")
@RestController
@RequestMapping("/api")
@RequiresAuthentication
public class ThesisReviewController {

    private final ThesisReviewService service;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public ThesisReviewController(ThesisReviewService service) {
        this.service = service;
    }

    /** 仅指定评阅教师、学生本人及管理员可查询评阅详情。 */
    @Operation(summary = "论文评阅：Get /thesis-reviews")
    @GetMapping("/thesis-reviews")
    @RequiresPermissions("material:read")
    public Result<?> list(@ModelAttribute PageQuery page) {
        return PageVO.optional(service.records(), page);
    }

    /** 管理员为定稿通过学生批量或单独分配非指导教师，任一无效项使整批回滚。 */
    @Operation(summary = "论文评阅：Post /thesis-reviews/assign")
    @PostMapping("/thesis-reviews/assign")
    @RequiresRoles("ADMIN")
    @RequiresPermissions("system:manage")
    public Result<CountVO> assign(@Valid @RequestBody ThesisReviewAssignRequest body) {
        return Result.ok(service.assign(body));
    }

    /** 指定评阅教师仅一次提交百分制分数、意见与答辩资格，状态从待评阅改为已评阅。 */
    @Operation(summary = "论文评阅：Post /thesis-reviews/{id}/review")
    @PostMapping("/thesis-reviews/{id}/review")
    @RequiresRoles("TEACHER")
    @RequiresPermissions("material:write")
    public Result<IdVO> review(@PathVariable long id, @Valid @RequestBody ThesisReviewRequest body) {
        return Result.ok(service.review(id, body));
    }
}
