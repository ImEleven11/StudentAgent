package com.uestc.studentagent.backend.common.error;

public enum ErrorCode {
    INVALID_REQUEST(40000, "Invalid request"),
    UNAUTHORIZED(40100, "Unauthorized"),
    FORBIDDEN(40300, "Forbidden"),
    NOT_FOUND(40400, "Resource not found"),
    CONFLICT(40900, "Resource conflict"),
    INTERNAL_ERROR(50000, "Internal server error");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
