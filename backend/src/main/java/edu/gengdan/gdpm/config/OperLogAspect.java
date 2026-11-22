package edu.gengdan.gdpm.config;

import edu.gengdan.gdpm.auth.Auth;
import edu.gengdan.gdpm.common.ApiException;
import edu.gengdan.gdpm.common.OperLog;
import edu.gengdan.gdpm.service.LogService;
import edu.gengdan.gdpm.vo.IdVO;
import edu.gengdan.gdpm.vo.LoginVO;
import edu.gengdan.gdpm.vo.ScoreSavedVO;
import edu.gengdan.gdpm.vo.SubmissionVO;
import edu.gengdan.gdpm.vo.UploadVO;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作审计切面：业务方法成功返回后记录“谁、在何时、从哪里、对什么对象、做了什么”。
 * 只记录请求方法、接口路径、业务对象编号与来源 IP，不序列化请求体、密码或令牌。
 */
@Aspect
@Component
@Order(10)
public class OperLogAspect {

    private final Auth auth;
    private final LogService logs;

    /** 装配已配置的依赖或已验证的数据，供所属业务使用。 */
    public OperLogAspect(Auth auth, LogService logs) {
        this.auth = auth;
        this.logs = logs;
    }

    /** 环绕标注了 {@link OperLog} 的方法，仅在成功执行后写入审计日志。 */
    @Around("@annotation(annotation)")
    public Object record(ProceedingJoinPoint joinPoint, OperLog annotation) throws Throwable {
        Long userId = currentUserId();
        Object result = joinPoint.proceed();
        if (userId == null && result instanceof LoginVO) {
            userId = ((LoginVO) result).user.getId();
        }
        if (userId != null) {
            logs.record(userId, annotation.value(), describe(joinPoint, result));
        }
        return result;
    }

    private Long currentUserId() {
        try {
            return auth.uid();
        } catch (UnavailableSecurityManagerException | ApiException ignored) {
            return null;
        }
    }

    /** 生成审计详情，如：POST /api/topics/5/review · 对象 #5 · 来源 127.0.0.1 */
    private String describe(ProceedingJoinPoint joinPoint, Object result) {
        StringBuilder detail = new StringBuilder();
        HttpServletRequest request = currentRequest();
        if (request != null) {
            detail.append(request.getMethod()).append(' ').append(request.getRequestURI());
        } else {
            detail.append(joinPoint.getSignature().getName());
        }
        Long objectId = null;
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long || arg instanceof Integer) {
                objectId = ((Number) arg).longValue();
                break;
            }
        }
        if (objectId == null) {
            objectId = resultId(result);
        }
        if (objectId != null) {
            detail.append(" · 对象 #").append(objectId);
        }
        if (request != null) {
            detail.append(" · 来源 ").append(clientIp(request));
        }
        return detail.length() > 500 ? detail.substring(0, 500) : detail.toString();
    }

    /** 写接口无路径编号时使用明确响应类型中的编号，禁止序列化整个结果。 */
    private Long resultId(Object result) {
        if (result instanceof IdVO) {
            return ((IdVO) result).id;
        }
        if (result instanceof SubmissionVO) {
            return ((SubmissionVO) result).id;
        }
        if (result instanceof ScoreSavedVO) {
            return ((ScoreSavedVO) result).id;
        }
        if (result instanceof UploadVO) {
            return ((UploadVO) result).id;
        }
        if (result instanceof LoginVO) {
            return ((LoginVO) result).user.getId();
        }
        return null;
    }

    private static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }
        return null;
    }

    /** 优先取反向代理头中的真实客户端地址。 */
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
