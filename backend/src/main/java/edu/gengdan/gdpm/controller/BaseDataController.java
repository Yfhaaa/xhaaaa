package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.BaseDataSaveRequest;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.BaseDataService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.MessageVO;
import edu.gengdan.gdpm.vo.ReferenceVO;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 已登录用户获取专业、班级和届次，教师列表仅管理员可见。 管理员维护专业、班级和届次，班级专业变更须与已有用户一致，届次改名同步用户。 管理员删除未被引用的基础数据，引用冲突由持久化约束拦截。 */
@Tag(name = "专业班级")
@RestController
@RequestMapping("/api")
public class BaseDataController {

    private final BaseDataService baseData;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public BaseDataController(BaseDataService service) {
        this.baseData = service;
    }

    /** 已登录用户获取专业、班级和届次，教师列表仅管理员可见。 */
    @Operation(summary = "专业班级：Get /reference")
    @GetMapping("/reference")
    @RequiresAuthentication
    public Result<ReferenceVO> reference(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(baseData.reference());
    }

    /** 管理员维护专业、班级和届次，班级专业变更须与已有用户一致，届次改名同步用户。 */
    @Operation(summary = "专业班级：Post /system/base/{type}")
    @PostMapping("/system/base/{type}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> base(@PathVariable String type, @Valid @RequestBody BaseDataSaveRequest b) {
        return Result.ok(baseData.base(type, null, b));
    }

    /** 管理员维护专业、班级和届次，班级专业变更须与已有用户一致，届次改名同步用户。 */
    @Operation(summary = "专业班级：Put /system/base/{type}/{id}")
    @PutMapping("/system/base/{type}/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> base(
        @PathVariable String type,
        @PathVariable long id,
        @Valid @RequestBody BaseDataSaveRequest b
    ) {
        return Result.ok(baseData.base(type, id, b));
    }

    /** 管理员删除未被引用的基础数据，引用冲突由持久化约束拦截。 */
    @Operation(summary = "专业班级：Delete /system/base/{type}/{id}")
    @DeleteMapping("/system/base/{type}/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<MessageVO> removeBase(@PathVariable String type, @PathVariable long id) {
        return Result.ok(baseData.removeBase(type, id));
    }
}
