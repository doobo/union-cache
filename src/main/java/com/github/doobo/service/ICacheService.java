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

    /**
     * 简单批量删除
     */
    int batchClear(String key);

    /**
     * 是否开启压缩
     */
    boolean enableCompress();

    /**
     * 是否启用缓存
     */
    boolean enableCache();
}
