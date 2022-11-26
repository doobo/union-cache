package com.github.doobo.vbo;

import com.github.doobo.annotation.DCache;
import com.github.doobo.annotation.RCache;
import com.github.doobo.annotation.UCache;
import lombok.Data;

import java.lang.reflect.Method;

@Data
public class UnionCacheRequest implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 缓存KEY
     */
    private String key;

    /**
     * 缓存值
     */
    private Object value;

    /**
     * 缓存时间,TimeUnit.SECONDS
     */
    private int expire;

    /**
     * 执行方法
     */
    private Method method;

    /**
     * 读缓存注解
     */
    private RCache rCache;

    /**
     * 更新缓存注解
     */
    private UCache uCache;

    /**
     * 删除缓存注解
     */
    private DCache dCache;
}
