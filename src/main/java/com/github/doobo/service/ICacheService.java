package com.github.doobo.service;

import com.github.doobo.vbo.ResultTemplate;
import com.github.doobo.vbo.UnionCacheRequest;

/**
 * 缓存服务定义
 */
public interface ICacheService {

    int DEFAULT_PHASE = 1024;

    /**
     * 优先级
     */
    default int getPhase() {
        return DEFAULT_PHASE;
    }

    /**
     * 判断是否匹配
     */
    default boolean matching(UnionCacheRequest request){
        return true;
    }

    /**
     * 添加缓存
     */
    ResultTemplate<Boolean> setCache(UnionCacheRequest request);

    /**
     * 获取缓存
     */
    ResultTemplate<Object> getCache(UnionCacheRequest request);

    /**
     * 删除缓存
     */
    ResultTemplate<Boolean> clearCache(UnionCacheRequest request);

    /**
     * 简单批量删除
     */
    ResultTemplate<Integer> batchClear(UnionCacheRequest request);

    /**
     * 是否开启压缩
     */
    boolean enableCompress();

    /**
     * 是否启用缓存
     */
    boolean enableCache();
}
