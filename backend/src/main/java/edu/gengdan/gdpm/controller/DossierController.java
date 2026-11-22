package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.service.DossierService;
import edu.gengdan.gdpm.vo.BoardVO;
import edu.gengdan.gdpm.vo.DossierVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 校验学生范围后聚合本人全过程时间线、全部材料版本、指导记录与可见成绩。 仅管理员获取大屏汇总、负载、三十天趋势和逾期预警，允许统计未发布成绩。 */
@Tag(name = "学生全过程档案")
@RestController
@RequestMapping("/api")
@RequiresAuthentication
public class DossierController {

    private final DossierService service;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public DossierController(DossierService service) {
        this.service = service;
    }

    /** 校验学生范围后聚合本人全过程时间线、全部材料版本、指导记录与可见成绩。 */
    @Operation(summary = "学生全过程档案：Get /students/{id}/dossier")
    @GetMapping("/students/{id}/dossier")
    @RequiresPermissions("material:read")
    public Result<DossierVO> dossier(@PathVariable long id) {
        return Result.ok(service.dossier(id));
    }

    /** 仅管理员获取大屏汇总、负载、三十天趋势和逾期预警，允许统计未发布成绩。 */
    @Operation(summary = "学生全过程档案：Get /statistics/board")
    @GetMapping("/statistics/board")
    @RequiresRoles("ADMIN")
    @RequiresPermissions("system:manage")
    public Result<BoardVO> board() {
        return Result.ok(service.board());
    }
}
