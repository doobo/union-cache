package com._5fu8.cache.service;

/**
 * redis的基本操作接口
 */
public interface ICacheService {
    /**
    * @param expire TimeUnit.SECONDS
    */
    void setCache(String key, Object value, int expire);

    Object getCache(String key);

    void clearCache(String key);

    Object getSortedSetRange(String key, int start, int end);
}
