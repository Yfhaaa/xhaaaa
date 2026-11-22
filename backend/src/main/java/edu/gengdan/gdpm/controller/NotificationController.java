package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.NoticeSaveRequest;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.NotificationService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 所有登录用户查询通知公告。 管理员发布标题和正文均完整的通知，成功后记录操作审计。 管理员删除通知，成功后记录操作审计。 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notifications;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public NotificationController(NotificationService service) {
        this.notifications = service;
    }

    /** 所有登录用户查询通知公告。 */
    @Operation(summary = "通知公告：Get /notices")
    @GetMapping("/notices")
    @RequiresAuthentication
    public Result<?> notices(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(notifications.notices(), pageQuery);
    }

    /** 管理员发布标题和正文均完整的通知，成功后记录操作审计。 */
    @Operation(summary = "通知公告：Post /notices")
    @PostMapping("/notices")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> notice(@Valid @RequestBody NoticeSaveRequest b) {
        return Result.ok(notifications.notice(b));
    }

    /** 管理员删除通知，成功后记录操作审计。 */
    @Operation(summary = "通知公告：Delete /notices/{id}")
    @DeleteMapping("/notices/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<MessageVO> removeNotice(@PathVariable long id) {
        return Result.ok(notifications.removeNotice(id));
    }
}
