package com.github.doobo.container;


import com.github.doobo.service.ICacheService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SPI获取所有注册的字段处理器
 *
 * @Description: union-cache
 * @User: diding
 * @Time: 2022-03-24 15:56
 */
public abstract class UnionCacheHandlerContainer {

    public static List<ICacheService> getHandlerList() {
        // SPI机制，寻找所有的实现类
        ServiceLoader<ICacheService> filtersImplements = ServiceLoader.load(ICacheService.class);
        List<ICacheService> receiptHandlerList = new ArrayList<>();
        //把找到的所有的Filter的实现类放入List中
        for (ICacheService filtersImplement : filtersImplements) {
            receiptHandlerList.add(filtersImplement);
        }
        if(receiptHandlerList.isEmpty()){
            return receiptHandlerList;
        }
        receiptHandlerList.sort(Comparator.comparing(ICacheService::getPhase));
        return receiptHandlerList;
    }
}
