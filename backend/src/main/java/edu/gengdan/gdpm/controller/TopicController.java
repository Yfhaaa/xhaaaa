package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.TopicService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Topic 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class TopicController {

    private final TopicService topics;

    public TopicController(TopicService service) {
        this.topics = service;
    }

    /** Topic 模块接口：/topics。 */
    @GetMapping("/topics")
    @RequiresAuthentication
    @RequiresPermissions("topic:read")
    public Result<Object> topics(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(topics.topics(), pageQuery));
    }

    /** Topic 模块接口：/topics。 */
    @PostMapping("/topics")
    @RequiresAuthentication
    @RequiresPermissions("topic:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<Object> topic(@RequestBody Map<String, Object> b) {
        return Result.ok(topics.save(null, b));
    }

    /** Topic 模块接口：/topics/{id}。 */
    @PutMapping("/topics/{id}")
    @RequiresAuthentication
    @RequiresPermissions("topic:write")
    @RequiresRoles(value = { "TEACHER" })
    public Result<Object> topic(@PathVariable long id, @RequestBody Map<String, Object> b) {
        return Result.ok(topics.save(id, b));
    }

    /** Topic 模块接口：/topics/{id}/review。 */
    @PostMapping("/topics/{id}/review")
    @RequiresAuthentication
    @RequiresPermissions("system:manage")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> reviewTopic(@PathVariable long id, @RequestBody Map<String, Object> b) {
        return Result.ok(topics.reviewTopic(id, b));
    }
}
