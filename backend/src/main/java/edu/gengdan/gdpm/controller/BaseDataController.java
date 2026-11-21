package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.BaseDataService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** BaseData 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class BaseDataController {

    private final BaseDataService baseData;

    public BaseDataController(BaseDataService service) {
        this.baseData = service;
    }

    /** BaseData 模块接口：/reference。 */
    @GetMapping("/reference")
    @RequiresAuthentication
    public Result<Object> reference(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(baseData.reference(), pageQuery));
    }

    /** BaseData 模块接口：/system/base/{type}。 */
    @PostMapping("/system/base/{type}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> base(@PathVariable String type, @RequestBody Map<String, Object> b) {
        return Result.ok(baseData.base(type, null, b));
    }

    /** BaseData 模块接口：/system/base/{type}/{id}。 */
    @PutMapping("/system/base/{type}/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> base(@PathVariable String type, @PathVariable long id, @RequestBody Map<String, Object> b) {
        return Result.ok(baseData.base(type, id, b));
    }

    /** BaseData 模块接口：/system/base/{type}/{id}。 */
    @DeleteMapping("/system/base/{type}/{id}")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> removeBase(@PathVariable String type, @PathVariable long id) {
        return Result.ok(baseData.removeBase(type, id));
    }
}
