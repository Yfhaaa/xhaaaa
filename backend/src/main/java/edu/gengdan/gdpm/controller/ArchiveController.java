package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.ArchiveService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Archive 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class ArchiveController {

    private final ArchiveService archives;

    public ArchiveController(ArchiveService service) {
        this.archives = service;
    }

    /** Archive 模块接口：/archives。 */
    @GetMapping("/archives")
    @RequiresAuthentication
    @RequiresPermissions("stats:read")
    public Result<Object> archives(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(archives.archives(), pageQuery));
    }

    /** Archive 模块接口：/archives。 */
    @PostMapping("/archives")
    @RequiresAuthentication
    @RequiresPermissions("archive:write")
    @RequiresRoles(value = { "ADMIN" })
    public Result<Object> archive(@RequestBody Map<String, Object> b) throws Exception {
        return Result.ok(archives.archive(b));
    }
}
