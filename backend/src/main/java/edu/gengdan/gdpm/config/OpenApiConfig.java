package edu.gengdan.gdpm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.lang.reflect.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 接口文档的 Bearer 令牌认证配置。 */
@Configuration
public class OpenApiConfig {

    /** Swagger 中登录后可粘贴令牌调用受保护接口。 */
    @Bean
    public OpenAPI gdpmOpenApi() {
        return new OpenAPI()
            .components(
                new Components().addSecuritySchemes(
                    "Bearer",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("Bearer"));
    }
}
