package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.MessageService;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 已登录用户仅查询本人消息，可选择仅未读并在范围过滤后分页。 用户只能标记本人消息，编号为空时一键标记本人全部消息。 业务操作向指定收件人写入私信，事件键保证截止提醒不重复。
 * 每天北京时间八点提醒未来三天截止且未完成的节点，不依赖登录上下文。
 */
@Tag(name = "个人消息")
@RestController
@RequestMapping("/api")
@RequiresAuthentication
public class MessageController {

    private final MessageService service;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public MessageController(MessageService service) {
        this.service = service;
    }

    /** 已登录用户仅查询本人消息，可选择仅未读并在范围过滤后分页。 */
    @Operation(summary = "个人消息：Get /messages")
    @GetMapping("/messages")
    public Result<?> list(
        @RequestParam(defaultValue = "false") boolean unread,
        @ModelAttribute PageQuery page
    ) {
        return PageVO.optional(service.list(unread), page);
    }

    /** 用户只能标记本人消息，编号为空时一键标记本人全部消息。 */
    @Operation(summary = "个人消息：Post /messages/read-all")
    @PostMapping("/messages/read-all")
    public Result<MessageVO> allRead() {
        service.read(null);
        return Result.ok(new MessageVO("已全部标记已读"));
    }

    /** 用户只能标记本人消息，编号为空时一键标记本人全部消息。 */
    @Operation(summary = "个人消息：Post /messages/{id}/read")
    @PostMapping("/messages/{id}/read")
    public Result<MessageVO> read(@PathVariable long id) {
        service.read(id);
        return Result.ok(new MessageVO("已读"));
    }
}
