package com.ztdx.eams.basic.exception;

/**
 * 业务异常基类
 */
public class BusinessException extends ApplicationException {

    public BusinessException(String message) {super(500, message, null);}

    public BusinessException(String message, Throwable cause) {super(500, message, cause);}
}
