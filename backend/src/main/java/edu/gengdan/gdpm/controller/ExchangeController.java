package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.service.ExchangeService;
import edu.gengdan.gdpm.vo.ImportResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员下载含可删除示例行的固定七列表头用户模板。 管理员逐行导入合规用户，失败行隔离，不回传密码，最多两千行二十兆。 持 stats:read 权限导出与查询相同范围的成绩、进度或缺失材料。
 * 范围内用户下载齐全项目的五阶段材料和成绩；管理员可按班级打包，缺文件时失败。
 */
@Tag(name = "Excel与归档下载")
@RestController
@RequestMapping("/api")
@RequiresAuthentication
public class ExchangeController {

    private final ExchangeService service;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public ExchangeController(ExchangeService service) {
        this.service = service;
    }

    /** 管理员下载含可删除示例行的固定七列表头用户模板。 */
    @Operation(summary = "Excel与归档下载：Get /system/users/template")
    @GetMapping("/system/users/template")
    @RequiresRoles("ADMIN")
    @RequiresPermissions("system:manage")
    public ResponseEntity<byte[]> template() {
        return file(service.template(), "用户导入模板.xlsx");
    }

    /** 管理员逐行导入合规用户，失败行隔离，不回传密码，最多两千行二十兆。 */
    @Operation(summary = "Excel与归档下载：Post /system/users/import")
    @PostMapping("/system/users/import")
    @RequiresRoles("ADMIN")
    @RequiresPermissions("system:manage")
    public Result<ImportResultVO> importUsers(@RequestParam MultipartFile file) throws Exception {
        return Result.ok(service.importUsers(file));
    }

    /** 持 stats:read 权限导出与查询相同范围的成绩、进度或缺失材料。 */
    @Operation(summary = "Excel与归档下载：Get /exports/{type}")
    @GetMapping("/exports/{type}")
    @RequiresPermissions("stats:read")
    public ResponseEntity<byte[]> excel(
        @PathVariable String type,
        @RequestParam Map<String, String> filters
    ) {
        return file(service.excel(type, filters), type + ".xlsx");
    }

    /** 范围内用户下载齐全项目的五阶段材料和成绩；管理员可按班级打包，缺文件时失败。 */
    @Operation(summary = "Excel与归档下载：Get /archives/download")
    @GetMapping("/archives/download")
    @RequiresPermissions("stats:read")
    public ResponseEntity<byte[]> archive(
        @RequestParam(required = false) Long studentId,
        @RequestParam(required = false) Long classId
    ) throws Exception {
        return file(service.archive(studentId, classId), "毕业设计归档.zip");
    }

    /** 返回安全附件响应。 */
    private ResponseEntity<byte[]> file(byte[] bytes, String name) {
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename(name, StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bytes);
    }
}
