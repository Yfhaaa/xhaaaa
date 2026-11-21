package edu.gengdan.gdpm.common;

/** 所有 JSON 业务接口共享的响应结构，HTTP 状态表达实际结果。 */
public class Result<T> {

    public final int code;
    public final String message;
    public final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 构造统一成功响应。 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 构造统一错误响应。 */
    public static Result<Void> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
