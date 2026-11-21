package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.MaterialService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Material 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class MaterialController {

    private final MaterialService materials;

    public MaterialController(MaterialService service) {
        this.materials = service;
    }

    /** Material 模块接口：/materials。 */
    @GetMapping("/materials")
    @RequiresAuthentication
    @RequiresPermissions("material:read")
    public Result<Object> materials(@RequestParam(required = false) String stage, @ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(materials.listMaterials(stage), pageQuery));
    }

    /** Material 模块接口：/materials/{stage}。 */
    @PostMapping("/materials/{stage}")
    @RequiresAuthentication
    @RequiresPermissions("material:write")
    public Result<Object> material(@PathVariable String stage, @RequestBody Map<String, Object> b) {
        return Result.ok(materials.submit(stage, b));
    }

    /** Material 模块接口：/materials/{stage}/{id}/review。 */
    @PostMapping("/materials/{stage}/{id}/review")
    @RequiresAuthentication
    @RequiresPermissions("material:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<Object> reviewMaterial(
        @PathVariable String stage,
        @PathVariable long id,
        @RequestBody Map<String, Object> b
    ) {
        return Result.ok(materials.review(stage, id, b));
    }
}
