package com.ztdx.eams.basic.exception;

/**
 * 未找到异常
 */
public class NotFoundException extends ApplicationException {

    public NotFoundException(String message) {
        super(404, message, null);
    }

    public NotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
}
