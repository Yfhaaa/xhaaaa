package edu.gengdan.gdpm.controller;

import edu.gengdan.gdpm.common.Result;
import edu.gengdan.gdpm.dto.PageQuery;
import edu.gengdan.gdpm.service.ReviewService;
import edu.gengdan.gdpm.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 持 material:read 权限读取范围内审核留痕。 业务审核成功后记录结果、审核人和意见，失败事务不保留成功留痕。 */
@Tag(name = "审核留痕")
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviews;

    /** 装配业务依赖，后续请求仍须通过身份、权限与数据范围检查。 */
    public ReviewController(ReviewService service) {
        this.reviews = service;
    }

    /** 持 material:read 权限读取范围内审核留痕。 */
    @Operation(summary = "审核留痕：Get /reviews")
    @GetMapping("/reviews")
    @RequiresAuthentication
    @RequiresPermissions("material:read")
    public Result<?> reviews(@ModelAttribute PageQuery pageQuery) {
        return PageVO.optional(reviews.reviews(), pageQuery);
    }
}
