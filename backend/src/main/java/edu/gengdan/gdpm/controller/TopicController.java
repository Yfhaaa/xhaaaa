package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.ReviewRequest;
import edu.gengdan.gdpm.dto.TopicSaveRequest;
import edu.gengdan.gdpm.service.TopicService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 持 topic:read 权限的用户查询本人角色可见课题。 教师在申报截止前创建待审核课题；仅本人已退回课题可修改重提。 管理员仅审核待审核课题，更新为已通过或已退回并保留意见。 */
@Tag(name = "课题管理")
@RestController
@RequestMapping("/api")
public class TopicController {

    private final TopicService topics;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public TopicController(TopicService service) {
        this.topics = service;
    }

    /** 持 topic:read 权限的用户查询本人角色可见课题。 */
    @Operation(summary = "课题管理：Get /topics")
    @GetMapping("/topics")
    @RequiresAuthentication
    @RequiresPermissions("topic:read")
    public Result<?> topics(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(topics.topics(), pageQuery);
    }

    /** 教师在申报截止前创建待审核课题；仅本人已退回课题可修改重提。 */
    @Operation(summary = "课题管理：Post /topics")
    @PostMapping("/topics")
    @RequiresAuthentication
    @RequiresPermissions("topic:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<IdVO> topic(@Valid @RequestBody TopicSaveRequest b) {
        return Result.ok(topics.save(null, b));
    }

    /** 教师在申报截止前创建待审核课题；仅本人已退回课题可修改重提。 */
    @Operation(summary = "课题管理：Put /topics/{id}")
    @PutMapping("/topics/{id}")
    @RequiresAuthentication
    @RequiresPermissions("topic:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<IdVO> topic(@PathVariable long id, @Valid @RequestBody TopicSaveRequest b) {
        return Result.ok(topics.save(id, b));
    }

    /** 管理员仅审核待审核课题，更新为已通过或已退回并保留意见。 */
    @Operation(summary = "课题管理：Post /topics/{id}/review")
    @PostMapping("/topics/{id}/review")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<MessageVO> reviewTopic(@PathVariable long id, @Valid @RequestBody ReviewRequest b) {
        return Result.ok(topics.reviewTopic(id, b));
    }
}
