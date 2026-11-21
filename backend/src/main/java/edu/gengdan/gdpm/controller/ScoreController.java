package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.ScoreService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Score 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class ScoreController {

    private final ScoreService scores;

    public ScoreController(ScoreService service) {
        this.scores = service;
    }

    /** Score 模块接口：/scores。 */
    @GetMapping("/scores")
    @RequiresAuthentication
    @RequiresPermissions("defense:read")
    public Result<Object> scores(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(scores.scores(), pageQuery));
    }

    /** Score 模块接口：/scores。 */
    @PostMapping("/scores")
    @RequiresAuthentication
    @RequiresPermissions("score:write")
    @RequiresRoles(value = { "TEACHER", "ADMIN" }, logical = Logical.OR)
    public Result<Object> score(@RequestBody Map<String, Object> b) {
        return Result.ok(scores.score(b));
    }

    /** Score 模块接口：/scores/{id}/publish。 */
    @PostMapping("/scores/{id}/publish")
    @RequiresAuthentication
    @RequiresPermissions("score:write")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> publish(@PathVariable long id) {
        return Result.ok(scores.publish(id));
    }
}
