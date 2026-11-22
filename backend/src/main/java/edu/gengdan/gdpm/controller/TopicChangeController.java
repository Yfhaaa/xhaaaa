package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.dto.TopicChangeRequest;
import edu.gengdan.gdpm.service.TopicChangeService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.PageVO;
import edu.gengdan.gdpm.vo.StatusVO;
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

/** 仅变更参与学生、原教师、新教师和管理员可读变更及审批历史。 学生仅为未进入答辩项目申请本专业有名额的新课题，同一学生只允许一条在途申请。 原教师、新教师、管理员依次审批；终审锁定学生和两课题原子迁移关系，拒绝则保留历史。 */
@Tag(name = "课题变更")
@RestController
@RequestMapping("/api")
@RequiresAuthentication
public class TopicChangeController {

    private final TopicChangeService service;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public TopicChangeController(TopicChangeService service) {
        this.service = service;
    }

    /** 仅变更参与学生、原教师、新教师和管理员可读变更及审批历史。 */
    @Operation(summary = "课题变更：Get /topic-changes")
    @GetMapping("/topic-changes")
    @RequiresPermissions("topic:read")
    public Result<?> list(@ModelAttribute PageQuery page) {
        return PageVO.optional(service.records(), page);
    }

    /** 学生仅为未进入答辩项目申请本专业有名额的新课题，同一学生只允许一条在途申请。 */
    @Operation(summary = "课题变更：Post /topic-changes")
    @PostMapping("/topic-changes")
    @RequiresRoles("STUDENT")
    @RequiresPermissions("topic:read")
    public Result<IdVO> apply(@Valid @RequestBody TopicChangeRequest body) {
        return Result.ok(service.apply(body));
    }

    /** 原教师、新教师、管理员依次审批；终审锁定学生和两课题原子迁移关系，拒绝则保留历史。 */
    @Operation(summary = "课题变更：Post /topic-changes/{id}/approve")
    @PostMapping("/topic-changes/{id}/approve")
    @RequiresPermissions("topic:write")
    public Result<StatusVO> approve(@PathVariable long id, @Valid @RequestBody ReviewRequest body) {
        return Result.ok(service.approve(id, body));
    }
}
