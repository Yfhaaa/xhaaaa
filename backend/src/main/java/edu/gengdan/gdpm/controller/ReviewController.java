package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.ReviewService;
import edu.gengdan.gdpm.vo.PageVO;
import java.util.*;
import org.apache.shiro.authz.annotation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** Review 模块 REST 接口，身份及权限由 Shiro 注解检查。 */
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviews;

    public ReviewController(ReviewService service) {
        this.reviews = service;
    }

    /** Review 模块接口：/reviews。 */
    @GetMapping("/reviews")
    @RequiresAuthentication
    @RequiresPermissions("material:read")
    public Result<Object> reviews(@ModelAttribute PageQuery pageQuery) {
        return Result.ok(PageVO.optional(reviews.reviews(), pageQuery));
    }
}
