package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.DefenseArrangeRequest;
import edu.gengdan.gdpm.dto.DefenseGroupRequest;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.DefenseService;
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

/**
 * 持 defense:read 权限查询角色范围内答辩分组。 管理员创建由有效教师组成的答辩组，记录时间与地点。 持 defense:read 权限读取本人或指导范围内答辩安排。
 * 管理员仅为定稿通过且评阅同意的学生安排答辩，录分后禁止调整。
 */
@Tag(name = "答辩管理")
@RestController
@RequestMapping("/api")
public class DefenseController {

    private final DefenseService defense;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public DefenseController(DefenseService service) {
        this.defense = service;
    }

    /** 持 defense:read 权限查询角色范围内答辩分组。 */
    @Operation(summary = "答辩管理：Get /defense/groups")
    @GetMapping("/defense/groups")
    @RequiresAuthentication
    @RequiresPermissions("defense:read")
    public Result<?> groups(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(defense.groups(), pageQuery);
    }

    /** 管理员创建由有效教师组成的答辩组，记录时间与地点。 */
    @Operation(summary = "答辩管理：Post /defense/groups")
    @PostMapping("/defense/groups")
    @RequiresAuthentication
    @RequiresPermissions("defense:write")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> group(@Valid @RequestBody DefenseGroupRequest b) {
        return Result.ok(defense.group(b));
    }

    /** 持 defense:read 权限读取本人或指导范围内答辩安排。 */
    @Operation(summary = "答辩管理：Get /defenses")
    @GetMapping("/defenses")
    @RequiresAuthentication
    @RequiresPermissions("defense:read")
    public Result<?> defenses(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(defense.defenses(), pageQuery);
    }

    /** 管理员仅为定稿通过且评阅同意的学生安排答辩，录分后禁止调整。 */
    @Operation(summary = "答辩管理：Post /defenses")
    @PostMapping("/defenses")
    @RequiresAuthentication
    @RequiresPermissions("defense:write")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> defense(@Valid @RequestBody DefenseArrangeRequest b) {
        return Result.ok(defense.arrange(b));
    }
}
