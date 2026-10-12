package com.persagy.apccontrol.common.logtrace.exception;

public class BusinessNormalException extends RuntimeException {

    private String code;

    public BusinessNormalException(String message) {
        this(message, (String)null, (Throwable)null);
    }

    public BusinessNormalException(String message, String code) {
        this(message, code, (Throwable)null);
    }

    public BusinessNormalException(String message, Throwable cause) {
        this(message, (String)null, cause);
    }

    public BusinessNormalException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}

