package com.ztdx.eams.basic.log;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String message() default "Check entity msg";  
  
    String key() default "#id";  
}