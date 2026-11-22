package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.ArchiveRequest;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.ArchiveService;
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

/** 持 stats:read 权限查询本人或指导范围内归档快照。 管理员在材料全部通过且成绩已发布后生成唯一不可变归档快照。 */
@Tag(name = "归档管理")
@RestController
@RequestMapping("/api")
public class ArchiveController {

    private final ArchiveService archives;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public ArchiveController(ArchiveService service) {
        this.archives = service;
    }

    /** 持 stats:read 权限查询本人或指导范围内归档快照。 */
    @Operation(summary = "归档管理：Get /archives")
    @GetMapping("/archives")
    @RequiresAuthentication
    @RequiresPermissions("stats:read")
    public Result<?> archives(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(archives.archives(), pageQuery);
    }

    /** 管理员在材料全部通过且成绩已发布后生成唯一不可变归档快照。 */
    @Operation(summary = "归档管理：Post /archives")
    @PostMapping("/archives")
    @RequiresAuthentication
    @RequiresPermissions("archive:write")
    @RequiresRoles(value = { "ADMIN" })
    public Result<IdVO> archive(@Valid @RequestBody ArchiveRequest b) throws Exception {
        return Result.ok(archives.archive(b));
    }
}
