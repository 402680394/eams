package com.ztdx.eams.basic.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统应用异常基类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ApplicationException extends RuntimeException {

    private int code;

    ApplicationException(int code, String message, Throwable cause){
        super(message,cause);
        this.code=code;
    }
}
