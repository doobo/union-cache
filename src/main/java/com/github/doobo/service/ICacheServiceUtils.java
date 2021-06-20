package com.github.doobo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ICacheServiceUtils {

    /**
     * 缓存工具
     */
    private static ICacheService cacheService;

    @Autowired(required = false)
    public static void setCacheService(ICacheService cacheService) {
        ICacheServiceUtils.cacheService = cacheService;
    }

    /**
     * 获取缓存工具
     */
    public static ICacheService getCacheService() {
        if(ICacheServiceUtils.cacheService == null){
            ICacheServiceUtils.cacheService = new AbstractCacheService() {};
        }
        return ICacheServiceUtils.cacheService;
    }
}
