package com.github.doobo.service;

public abstract class AbstractCacheService implements ICacheService{
    
    @Override
    public void setCache(String key, Object value, int expire) {
        InMemoryCacheUtils.cache().add(key, value, expire*1000);
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
    public Object getSortedSetRange(String key, int start, int end) {
        return null;
    }

    @Override
    public boolean enable() {
        return true;
    }
}
