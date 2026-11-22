package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.service.FileService;
import edu.gengdan.gdpm.vo.UploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 持 material:write 权限上传二十兆以内的允许格式，校验文件特征后登记所有者。 持 material:read 权限且具备归属、指导或评阅关系时下载，拒绝越权和路径逃逸。 */
@Tag(name = "材料附件")
@RestController
@RequestMapping("/api")
public class FileController {

    private final FileService files;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public FileController(FileService service) {
        this.files = service;
    }

    /** 持 material:write 权限上传二十兆以内的允许格式，校验文件特征后登记所有者。 */
    @Operation(summary = "材料附件：Post /files")
    @PostMapping("/files")
    @RequiresAuthentication
    @RequiresPermissions("material:write")
    public Result<UploadVO> upload(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.ok(files.upload(file));
    }

    /** 持 material:read 权限且具备归属、指导或评阅关系时下载，拒绝越权和路径逃逸。 */
    @Operation(summary = "材料附件：Get /files/{id}")
    @GetMapping("/files/{id}")
    @RequiresAuthentication
    @RequiresPermissions("material:read")
    public ResponseEntity<Resource> download(
        @PathVariable long id
    ) {
        return files.download(id);
    }
}
