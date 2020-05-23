package com._5fu8.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 读取缓存注解
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RCache{

    /**
     * 前缀后面会拼参数
     */
    String prefix() default "";

    /**
     * 缓存key
     */
    String key();

    /**
     * key值拼接符号
     * @return
     */
    String symbol() default ".";

    /**
     * 缓存失效时间1分钟
     */
    int expiredTime() default 60 * 1;

    /**
     * #result == null || !#result.ok
     * 判断结果返回,先不实现
     * @return
     */
    String unless() default "";
}

