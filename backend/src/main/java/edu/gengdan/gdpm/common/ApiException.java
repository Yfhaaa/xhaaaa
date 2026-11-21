package edu.gengdan.gdpm.common;

/** 携带 HTTP 状态的业务异常。 */
public class ApiException extends RuntimeException {

    public final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    /** 校验业务前置条件。 */
    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new ApiException(400, message);
        }
    }
}
