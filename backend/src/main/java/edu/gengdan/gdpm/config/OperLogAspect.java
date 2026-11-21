package edu.gengdan.gdpm.config;

import edu.gengdan.gdpm.auth.Auth;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.entity.User;
import edu.gengdan.gdpm.service.LogService;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 统一记录成功操作，不序列化请求体、密码或令牌。 */
@Aspect
@Component
@Order(10)
public class OperLogAspect {

    private final Auth auth;
    private final LogService logs;

    public OperLogAspect(Auth auth, LogService logs) {
        this.auth = auth;
        this.logs = logs;
    }

    /** 记录成功操作或审核留痕。 */
    @Around("@annotation(annotation)")
    public Object record(ProceedingJoinPoint joinPoint, OperLog annotation) throws Throwable {
        Long userId = null;
        try {
            userId = auth.uid();
        } catch (org.apache.shiro.UnavailableSecurityManagerException | edu.gengdan.gdpm.common.ApiException ignored) {}
        Object result = joinPoint.proceed();
        if (userId == null && result instanceof Map) {
            Object user = ((Map<?, ?>) result).get("user");
            if (user instanceof User) {
                userId = ((User) user).getId();
            }
        }
        if (userId != null) {
            logs.record(
                userId,
                annotation.value(),
                joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName()
            );
        }
        return result;
    }
}
