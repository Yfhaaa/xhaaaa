package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.FileService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** File 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class FileController {

    private final FileService files;

    public FileController(FileService service) {
        this.files = service;
    }

    /** File 模块接口：/files。 */
    @PostMapping("/files")
    @RequiresAuthentication
    @RequiresPermissions("material:write")
    public Result<Object> upload(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.ok(files.upload(file));
    }

    /** File 模块接口：/files/{id}。 */
    @GetMapping("/files/{id}")
    @RequiresAuthentication
    @RequiresPermissions("material:read")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> download(
        @PathVariable long id
    ) {
        return files.download(id);
    }
}
