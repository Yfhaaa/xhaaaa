package edu.gengdan.gdpm.common;

import java.util.Comparator;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** 统一业务、鉴权、参数与持久化错误响应。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 请求 DTO 约束失败返回原有 400 状态及字段对应的中文提示。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> invalidRequest(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .sorted(Comparator.comparing(FieldError::getField))
            .map(FieldError::getDefaultMessage)
            .findFirst().orElse("请求参数格式不正确");
        return error(400, message);
    }

    /** 直接服务调用和条件分组校验失败时复用请求 DTO 的中文错误。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> invalidServiceRequest(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
            .sorted(Comparator.comparing(value -> value.getPropertyPath().toString()))
            .map(ConstraintViolation::getMessage)
            .findFirst().orElse("请求参数格式不正确");
        return error(400, message);
    }

    /** 构造统一错误响应。 */
    private ResponseEntity<Result<Void>> error(int status, String message) {
        return ResponseEntity.status(status).body(Result.error(status, message));
    }

    /** 将业务异常转换为对应 HTTP 状态。 */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Result<Void>> api(ApiException e) {
        return error(e.status, e.getMessage());
    }

    /** 返回角色或权限不足的错误。 */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Result<Void>> forbidden(Exception e) {
        return error(403, "当前角色或权限不允许此操作");
    }

    /** 返回身份认证失败的错误。 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> unauthorized(Exception e) {
        return error(401, "请先登录");
    }

    /** 返回请求参数校验错误。 */
    @ExceptionHandler({
        IllegalArgumentException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        BindException.class,
        MissingServletRequestParameterException.class,
    })
    public ResponseEntity<Result<Void>> bad(Exception e) {
        for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException) {
                return error(400, cause.getMessage());
            }
        }
        return error(400, "请求参数格式不正确");
    }

    /** 返回数据唯一约束或引用冲突。 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> conflict(Exception e) {
        return error(409, "数据重复或被其他业务引用，请检查后重试");
    }

    /** 返回上传文件大小超限错误。 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> large(Exception e) {
        return error(413, "文件不能超过20MB");
    }

    /** 记录非预期异常并隐藏内部错误细节。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> other(Exception e) {
        LoggerFactory.getLogger(getClass()).error("请求处理失败", e);
        return error(500, "服务器处理失败，请查看后端日志");
    }
}
