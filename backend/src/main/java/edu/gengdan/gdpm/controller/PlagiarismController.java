package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.dto.PlagiarismRegisterRequest;
import edu.gengdan.gdpm.service.PlagiarismService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 持 material:read 权限查询授权学生的查重记录及对应定稿版本。 指导教师或管理员为尚未通过的最新定稿登记本人报告，新版本不能复用旧结论。 */
@Tag(name = "查重记录")
@RestController
@RequestMapping("/api")
@RequiresAuthentication
public class PlagiarismController {

    private final PlagiarismService service;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public PlagiarismController(PlagiarismService service) {
        this.service = service;
    }

    /** 持 material:read 权限查询授权学生的查重记录及对应定稿版本。 */
    @Operation(summary = "查重记录：Get /plagiarism-checks")
    @GetMapping("/plagiarism-checks")
    @RequiresPermissions("material:read")
    public Result<?> list(@ModelAttribute PageQuery page) {
        return PageVO.optional(service.records(), page);
    }

    /** 指导教师或管理员为尚未通过的最新定稿登记本人报告，新版本不能复用旧结论。 */
    @Operation(summary = "查重记录：Post /plagiarism-checks")
    @PostMapping("/plagiarism-checks")
    @RequiresPermissions("material:write")
    public Result<IdVO> register(@Valid @RequestBody PlagiarismRegisterRequest body) {
        return Result.ok(service.register(body));
    }
}
