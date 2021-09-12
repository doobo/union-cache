package com.github.doobo.service;

import java.util.concurrent.TimeUnit;

public abstract class AbstractCacheService implements ICacheService{
    
    @Override
    public void setCache(String key, Object value, int expire) {
        InMemoryCacheUtils.cache().add(key, value, TimeUnit.SECONDS.toMillis(expire));
    }

    @Override
    public Object getCache(String key) {
        return InMemoryCacheUtils.cache().get(key);
    }

    @Override
    public void clearCache(String key) {
        InMemoryCacheUtils.cache().remove(key);
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
