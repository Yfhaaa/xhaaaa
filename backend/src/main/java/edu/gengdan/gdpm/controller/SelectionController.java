package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.SelectionService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Selection 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class SelectionController {

    private final SelectionService selections;

    public SelectionController(SelectionService service) {
        this.selections = service;
    }

    /** Selection 模块接口：/topics/{id}/apply。 */
    @PostMapping("/topics/{id}/apply")
    @RequiresAuthentication
    @RequiresPermissions("topic:read")
    @RequiresRoles(value = { "STUDENT" })
    public Result<Object> apply(@PathVariable long id) {
        return Result.ok(selections.apply(id));
    }

    /** Selection 模块接口：/selections。 */
    @GetMapping("/selections")
    @RequiresAuthentication
    @RequiresPermissions("topic:read")
    public Result<Object> selections(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(selections.selections(), pageQuery));
    }

    /** Selection 模块接口：/selections/{id}/review。 */
    @PostMapping("/selections/{id}/review")
    @RequiresAuthentication
    @RequiresPermissions("topic:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<Object> confirm(@PathVariable long id, @RequestBody Map<String, Object> b) {
        return Result.ok(selections.confirm(id, b));
    }
}
