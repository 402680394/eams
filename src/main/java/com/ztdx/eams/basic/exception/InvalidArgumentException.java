package com.ztdx.eams.basic.exception;

/**
 * 参数错误异常
 */
public class InvalidArgumentException extends ApplicationException {

    public InvalidArgumentException(String message, Throwable cause) {super(400, message, cause);}

    public InvalidArgumentException(String message) { super(400, message, null);}
}
