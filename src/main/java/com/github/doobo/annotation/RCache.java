package com.github.doobo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 读取缓存注解
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
     */
    String symbol() default ".";

    /**
     * 缓存失效时间1分钟,具体要看缓存实现类
     */
    int expiredTime() default 60 * 3;

    /**
     * #result == null || !#result.ok
	 * 判断结果返回
     */
    String unless() default "";

	/**
	 * 不能解析出方法返回类型时,采用指定类型
	 */
	Class<?> cacheType() default Object.class;

    /**
     * 是否批量读取或批量写
     */
    boolean isBatch() default false;
}

