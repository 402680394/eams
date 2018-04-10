package com.ztdx.eams.basic.exception;

/**
 * 未授权、登陆失败错误异常
 */
public class UnauthorizedException extends ApplicationException {

    public UnauthorizedException(String message) {super(401, message,null);}

    public UnauthorizedException(String message, Throwable cause) {super(401, message, cause);}
}
