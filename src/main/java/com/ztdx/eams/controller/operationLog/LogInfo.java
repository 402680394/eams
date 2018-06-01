package com.ztdx.eams.controller.operationLog;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogInfo {
    String message() default "Check entity msg";
}