package edu.gengdan.gdpm.config;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * 单包部署时的前端路由兜底：前端打包进 jar 的 classpath:/static 后，
 * 浏览器直接访问或刷新 /topics 等前端路由会 404，此处将非接口的 404 转发到 index.html，
 * 由 Vue Router 接管。未打包前端（开发模式）时 index.html 不存在，不做任何处理。
 */
@Configuration
public class SpaFallbackConfig {

    private static final String INDEX = "static/index.html";

    /** 非 /api、非静态文件的 404 请求返回前端入口页。 */
    @Bean
    public ErrorViewResolver spaErrorViewResolver() {
        boolean frontendBundled = new ClassPathResource(INDEX).exists();
        return (HttpServletRequest request, HttpStatus status, Map<String, Object> model) -> {
            if (!frontendBundled || status != HttpStatus.NOT_FOUND) {
                return null;
            }
            Object uri = model.get("path");
            String path = uri == null ? "" : uri.toString();
            if (path.startsWith("/api/") || path.contains(".")) {
                return null;
            }
            return new ModelAndView("forward:/index.html", HttpStatus.OK);
        };
    }
}
