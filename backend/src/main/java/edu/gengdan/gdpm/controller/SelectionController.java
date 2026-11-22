package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.service.SelectionService;
import edu.gengdan.gdpm.vo.IdVO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 按学生本人或教师指导范围查询双选申请。 学生在选题截止前申请本专业有名额的已通过课题，拒绝重复和多课题确定。 课题教师在事务内确认待审核申请，锁学生与课题保证唯一分配且不超名额。 */
@Tag(name = "双选申请")
@RestController
@RequestMapping("/api")
public class SelectionController {

    private final SelectionService selections;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public SelectionController(SelectionService service) {
        this.selections = service;
    }

    /** 学生在选题截止前申请本专业有名额的已通过课题，拒绝重复和多课题确定。 */
    @Operation(summary = "双选申请：Post /topics/{id}/apply")
    @PostMapping("/topics/{id}/apply")
    @RequiresAuthentication
    @RequiresPermissions("topic:read")
    @RequiresRoles(value = { "STUDENT" })
    public Result<IdVO> apply(@PathVariable long id) {
        return Result.ok(selections.apply(id));
    }

    /** 按学生本人或教师指导范围查询双选申请。 */
    @Operation(summary = "双选申请：Get /selections")
    @GetMapping("/selections")
    @RequiresAuthentication
    @RequiresPermissions("topic:read")
    public Result<?> selections(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(selections.selections(), pageQuery);
    }

    /** 课题教师在事务内确认待审核申请，锁学生与课题保证唯一分配且不超名额。 */
    @Operation(summary = "双选申请：Post /selections/{id}/review")
    @PostMapping("/selections/{id}/review")
    @RequiresAuthentication
    @RequiresPermissions("topic:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<MessageVO> confirm(@PathVariable long id, @Valid @RequestBody ReviewRequest b) {
        return Result.ok(selections.confirm(id, b));
    }
}
