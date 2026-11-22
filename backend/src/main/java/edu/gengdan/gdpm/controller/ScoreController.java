package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.ScoreSaveRequest;
import edu.gengdan.gdpm.service.ScoreService;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.PageVO;
import edu.gengdan.gdpm.vo.ScoreSavedVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.Logical;
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

/** 按角色范围查询成绩，学生仅可见本人已发布成绩。 指导教师或管理员锁定学生后录分；引用已完成评阅分，权重和为一，发布归档后禁改。 管理员仅发布未发布成绩，发布后锁定改分并通知学生。 */
@Tag(name = "成绩管理")
@RestController
@RequestMapping("/api")
public class ScoreController {

    private final ScoreService scores;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public ScoreController(ScoreService service) {
        this.scores = service;
    }

    /** 按角色范围查询成绩，学生仅可见本人已发布成绩。 */
    @Operation(summary = "成绩管理：Get /scores")
    @GetMapping("/scores")
    @RequiresAuthentication
    @RequiresPermissions("defense:read")
    public Result<?> scores(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(scores.scores(), pageQuery);
    }

    /** 指导教师或管理员锁定学生后录分；引用已完成评阅分，权重和为一，发布归档后禁改。 */
    @Operation(summary = "成绩管理：Post /scores")
    @PostMapping("/scores")
    @RequiresAuthentication
    @RequiresPermissions("score:write")
    @RequiresRoles(value = { "TEACHER", "ADMIN" }, logical = Logical.OR)
    public Result<ScoreSavedVO> score(@Valid @RequestBody ScoreSaveRequest b) {
        return Result.ok(scores.score(b));
    }

    /** 管理员仅发布未发布成绩，发布后锁定改分并通知学生。 */
    @Operation(summary = "成绩管理：Post /scores/{id}/publish")
    @PostMapping("/scores/{id}/publish")
    @RequiresAuthentication
    @RequiresPermissions("score:write")
    @RequiresRoles(value = { "ADMIN" })
    public Result<MessageVO> publish(@PathVariable long id) {
        return Result.ok(scores.publish(id));
    }
}
