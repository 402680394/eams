package com.ztdx.eams.controller.operationLog;

import java.lang.annotation.*;

/**
 * 操作日志记录 注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationInfo {
    String message() default "Check entity msg";
}