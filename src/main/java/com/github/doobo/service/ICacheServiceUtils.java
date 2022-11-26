package com.github.doobo.service;


import com.github.doobo.factory.UnionCacheChainFactory;
import com.github.doobo.utils.ResultUtils;
import com.github.doobo.vbo.ResultTemplate;
import com.github.doobo.vbo.UnionCacheRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 缓存基础工具类
 */
@Slf4j
public abstract class ICacheServiceUtils {

    /**
     * 是否开启压缩
     */
    public static boolean enableCache(){
        List<ICacheService> handlerList = UnionCacheChainFactory.getHandlerList();
        if(Objects.nonNull(handlerList) && handlerList.size() > 0){
            return handlerList.get(0).enableCache();
        }
        return false;
    }

    /**
     * 是否开启压缩
     */
    public static boolean enableCompress(){
        List<ICacheService> handlerList = UnionCacheChainFactory.getHandlerList();
        if(Objects.nonNull(handlerList) && handlerList.size() > 0){
            return handlerList.get(0).enableCompress();
        }
        return false;
    }

    /**
     * 设置单个缓存
     */
    public static ResultTemplate<Boolean> setCache(UnionCacheRequest request) {
        return UnionCacheChainFactory.executeHandler(request, handler -> handler.setCache(request));
    }

    /**
     * 获取单个缓存
     */
    public static ResultTemplate<Object> getCache(UnionCacheRequest request) {
        return UnionCacheChainFactory.executeHandler(request, handler -> handler.getCache(request));
    }

    /**
     * 清空单个缓存
     */
    public static ResultTemplate<Boolean> clearCache(UnionCacheRequest request) {
        return UnionCacheChainFactory.executeHandler(request, handler -> handler.clearCache(request));
    }

    /**
     * 批量清空缓存
     */
    public static ResultTemplate<Integer> batchClear(UnionCacheRequest request) {
        return UnionCacheChainFactory.executeHandler(request, handler -> handler.batchClear(request));
    }

    /**
     * 批量写缓存
     */
    public static ResultTemplate<Integer> batchSetCache(UnionCacheRequest request) {
        List<ICacheService> services = UnionCacheChainFactory.matchingList(request);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Optional.ofNullable(services).ifPresent(c ->{
            c.forEach(m ->{
                ResultTemplate<Boolean> template = m.setCache(request);
                atomicInteger.incrementAndGet();
                Optional.ofNullable(template).filter(f -> !f.isSuccess())
                        .ifPresent(p -> {
                            log.error("setCacheError,class:{},{}", m.getClass().getName(), p);
                            atomicInteger.decrementAndGet();
                        });
            });
        });
        return ResultUtils.of(Integer.valueOf(atomicInteger.get()));
    }

    /**
     * 批量读缓存
     */
    public static ResultTemplate<Object> batchReadCache(UnionCacheRequest request) {
        List<ICacheService> services = UnionCacheChainFactory.matchingList(request);
        for(ICacheService service : services){
            try{
                ResultTemplate<Object> cache = service.getCache(request);
                if(Objects.nonNull(cache) && cache.isSuccess()){
                    return cache;
                }
            }catch (Throwable e){
                log.error("batchReadCacheError:", e);
            }
        }
        if(services.size() > 0){
            return services.get(0).getCache(request);
        }
        return ResultUtils.ofFail("not match cache service.");
    }
}
