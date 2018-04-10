package com.ztdx.eams.basic.params;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonParam {

    String defaultValue() default "";
}
