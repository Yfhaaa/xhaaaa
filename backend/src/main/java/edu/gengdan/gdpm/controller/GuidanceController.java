package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.GuidanceService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Guidance 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class GuidanceController {

    private final GuidanceService guidance;

    public GuidanceController(GuidanceService service) {
        this.guidance = service;
    }

    /** Guidance 模块接口：/guidance。 */
    @GetMapping("/guidance")
    @RequiresAuthentication
    @RequiresPermissions("guidance:read")
    public Result<Object> guidance(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(guidance.guidance(), pageQuery));
    }

    /** Guidance 模块接口：/guidance。 */
    @PostMapping("/guidance")
    @RequiresAuthentication
    @RequiresPermissions("guidance:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<Object> guidance(@RequestBody Map<String, Object> b) {
        return Result.ok(guidance.guidance(b));
    }
}
