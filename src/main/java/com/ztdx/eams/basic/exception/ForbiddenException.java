package com.ztdx.eams.basic.exception;

/**
 * 无权限异常
 */
public class ForbiddenException extends ApplicationException {

    public ForbiddenException(String message) {
        super(403,message,null);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(403,message, cause);
    }
}
