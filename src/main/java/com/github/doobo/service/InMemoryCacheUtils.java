package com.github.doobo.service;


import java.util.Objects;

/**
 * 基础缓存初始化
 */
public abstract class InMemoryCacheUtils {
    
    private static InMemoryCacheWithDelayQueue INSTANCE;
    
    public static InMemoryCacheWithDelayQueue cache(){
        if(Objects.isNull(INSTANCE)){
            synchronized (InMemoryCacheUtils.class){
                if(Objects.isNull(INSTANCE)){
                    INSTANCE = new InMemoryCacheWithDelayQueue();
                }
            }
        }
        return INSTANCE;
    }
}
