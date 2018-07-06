package com.ztdx.eams.basic.task;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记到方法上（这是个空方法，只是用于拦截），方法必须为void。另外写一个Job结尾的方法作为异步执行调用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface Job {
}
