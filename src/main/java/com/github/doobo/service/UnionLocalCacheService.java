package com.github.doobo.service;

import com.github.doobo.utils.ResultUtils;
import com.github.doobo.vbo.ResultTemplate;
import com.github.doobo.vbo.UnionCacheRequest;

import java.util.concurrent.TimeUnit;

/**
 * 联合本地缓存
 */
public class UnionLocalCacheService implements ICacheService{

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public ResultTemplate<Boolean> setCache(UnionCacheRequest request) {
        InMemoryCacheUtils.cache().add(request.getKey(), request.getValue(), TimeUnit.SECONDS.toMillis(request.getExpire()));
        return ResultUtils.of(true);
    }

    @Override
    public ResultTemplate<Object> getCache(UnionCacheRequest request) {
        return ResultUtils.ofUnsafe(InMemoryCacheUtils.cache().get(request.getKey()));
    }

    @Override
    public ResultTemplate<Boolean> clearCache(UnionCacheRequest request) {
        InMemoryCacheUtils.cache().remove(request.getKey());
        return ResultUtils.of(true);
    }

    @Override
    public ResultTemplate<Integer> batchClear(UnionCacheRequest request) {
        int count = InMemoryCacheUtils.cache().batchClear(request.getKey());
        return ResultUtils.of(Integer.valueOf(count));
    }

    @Override
    public boolean enableCompress() {
        return Boolean.FALSE;
    }

    @Override
    public boolean enableCache() {
        return Boolean.TRUE;
    }
}
