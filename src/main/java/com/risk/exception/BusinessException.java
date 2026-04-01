package com.risk.exception;

/**
 * 业务异常
 *
 * 用于 Service 层抛出可预期的业务错误（如：规则不存在、数据校验失败等），
 * 由 GlobalExceptionHandler 统一拦截并返回友好的错误信息。
 */
public class BusinessException extends RuntimeException {

    /** 错误码，默认 500 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public int getCode() {
        return code;
    }
}
