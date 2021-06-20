package com.github.doobo;

import com.github.doobo.service.AbstractCacheService;
import org.springframework.stereotype.Component;

@Component
public class ICacheServiceImpl extends AbstractCacheService {
    
    @Override
    public void setCache(String key, Object value, int expire) {
        super.setCache(key, value, expire);
    }

    @Override
    public Object getCache(String key) {
        return super.getCache(key);
    }

    @Override
    public void clearCache(String key) {
        super.clearCache(key);
    }
}
