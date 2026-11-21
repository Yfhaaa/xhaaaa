package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.StatisticsService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Statistics 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class StatisticsController {

    private final StatisticsService statistics;

    public StatisticsController(StatisticsService service) {
        this.statistics = service;
    }

    /** Statistics 模块接口：/dashboard。 */
    @GetMapping("/dashboard")
    @RequiresAuthentication
    @RequiresPermissions("dashboard:read")
    public Result<Object> dashboard(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(statistics.dashboard(), pageQuery));
    }

    /** Statistics 模块接口：/statistics。 */
    @GetMapping("/statistics")
    @RequiresAuthentication
    @RequiresPermissions("stats:read")
    public Result<Object> stats(@RequestParam Map<String, String> filters, @ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(statistics.statistics(filters), pageQuery));
    }
}
