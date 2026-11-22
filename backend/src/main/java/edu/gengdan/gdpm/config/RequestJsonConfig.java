package edu.gengdan.gdpm.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** JSON 编号和整数字段拒绝小数截断，保留原有非法编号返回参数错误的约定。 */
@Configuration
public class RequestJsonConfig {

    /** 保留整数字符串兼容绑定，浮点数不得静默变为其他业务记录的编号。 */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer strictIntegerBinding() {
        return builder -> builder.featuresToDisable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
    }
}
