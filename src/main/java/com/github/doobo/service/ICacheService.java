package com.github.doobo.service;

/**
 * redis的基本操作接口
 * @author qpc
 */
public interface ICacheService {
    /**
    * @param expire TimeUnit.SECONDS
    */
    void setCache(String key, Object value, int expire);

    Object getCache(String key);

    void clearCache(String key);

    Object getSortedSetRange(String key, int start, int end);

    /**
     * 是否启用缓存
     * @return
     */
    boolean enable();
}
