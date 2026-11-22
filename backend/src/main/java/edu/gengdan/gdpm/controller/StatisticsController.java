package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.StatisticsService;
import edu.gengdan.gdpm.vo.DashboardVO;
import edu.gengdan.gdpm.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 按当前身份的数据范围汇总九节点进度，学生未发布成绩保持隐藏。 持 dashboard:read 权限查看当前身份的待办、进度、通知与截止提醒。 持 stats:read
 * 权限按专业、班级或教师统计，先做身份范围过滤再应用业务筛选。
 */
@Tag(name = "统计分析")
@RestController
@RequestMapping("/api")
public class StatisticsController {

    private final StatisticsService statistics;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public StatisticsController(StatisticsService service) {
        this.statistics = service;
    }

    /** 持 dashboard:read 权限查看当前身份的待办、进度、通知与截止提醒。 */
    @Operation(summary = "统计分析：Get /dashboard")
    @GetMapping("/dashboard")
    @RequiresAuthentication
    @RequiresPermissions("dashboard:read")
    public Result<DashboardVO> dashboard(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(statistics.dashboard());
    }

    /** 持 stats:read 权限按专业、班级或教师统计，先做身份范围过滤再应用业务筛选。 */
    @Operation(summary = "统计分析：Get /statistics")
    @GetMapping("/statistics")
    @RequiresAuthentication
    @RequiresPermissions("stats:read")
    public Result<StatisticsVO> stats(
        @RequestParam Map<String, String> filters,
        @ModelAttribute PageQuery pageQuery
    ) {
        return Result.ok(statistics.statistics(filters));
    }
}
