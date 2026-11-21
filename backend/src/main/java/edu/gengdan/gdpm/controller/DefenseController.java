package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.DefenseService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Defense 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class DefenseController {

    private final DefenseService defense;

    public DefenseController(DefenseService service) {
        this.defense = service;
    }

    /** Defense 模块接口：/defense/groups。 */
    @GetMapping("/defense/groups")
    @RequiresAuthentication
    @RequiresPermissions("defense:read")
    public Result<Object> groups(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(defense.groups(), pageQuery));
    }

    /** Defense 模块接口：/defense/groups。 */
    @PostMapping("/defense/groups")
    @RequiresAuthentication
    @RequiresPermissions("defense:write")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> group(@RequestBody Map<String, Object> b) {
        return Result.ok(defense.group(b));
    }

    /** Defense 模块接口：/defenses。 */
    @GetMapping("/defenses")
    @RequiresAuthentication
    @RequiresPermissions("defense:read")
    public Result<Object> defenses(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(defense.defenses(), pageQuery));
    }

    /** Defense 模块接口：/defenses。 */
    @PostMapping("/defenses")
    @RequiresAuthentication
    @RequiresPermissions("defense:write")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> defense(@RequestBody Map<String, Object> b) {
        return Result.ok(defense.arrange(b));
    }
}
