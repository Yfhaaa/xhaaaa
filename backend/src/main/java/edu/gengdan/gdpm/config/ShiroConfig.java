package edu.gengdan.gdpm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.gengdan.gdpm.auth.TokenFilter;
import edu.gengdan.gdpm.auth.TokenRealm;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.Filter;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 注册 Shiro Web 过滤链、无状态主题与 Spring 方法注解鉴权。 */
@Configuration
public class ShiroConfig {

    /** 创建禁用会话存储的安全管理器。 */
    @Bean
    public SecurityManager securityManager(TokenRealm realm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager(realm);
        manager.setRememberMeManager(null);
        DefaultSubjectDAO dao = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator evaluator = new DefaultSessionStorageEvaluator();
        evaluator.setSessionStorageEnabled(false);
        dao.setSessionStorageEvaluator(evaluator);
        manager.setSubjectDAO(dao);
        return manager;
    }

    /** 配置匿名端点和受保护端点的过滤链。 */
    @Bean
    public ShiroFilterFactoryBean shiroFilter(
        SecurityManager securityManager,
        ObjectMapper json
    ) {
        ShiroFilterFactoryBean factory = new ShiroFilterFactoryBean();
        factory.setSecurityManager(securityManager);
        Map<String, Filter> filters = new LinkedHashMap<>();
        filters.put("token", new TokenFilter(json));
        factory.setFilters(filters);
        Map<String, String> chains = new LinkedHashMap<>();
        chains.put("/api/health", "anon");
        chains.put("/api/auth/login", "anon");
        chains.put("/error", "anon");
        chains.put("/api/**", "noSessionCreation,token");
        chains.put("/**", "anon");
        factory.setFilterChainDefinitionMap(chains);
        return factory;
    }

    /** 将 Shiro 主过滤器注册到 Web 容器。 */
    @Bean
    public FilterRegistrationBean<Filter> shiroRegistration(ShiroFilterFactoryBean factory) throws Exception {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(
            (Filter) factory.getObject()
        );
        registration.setName("shiroFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(-100);
        return registration;
    }

    /** 启用角色与权限注解拦截。 */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAdvisor(
        SecurityManager securityManager
    ) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
