package com.github.doobo.service;

import org.springframework.stereotype.Component;

@Component
public class InMemoryCacheUtils {
    
    private static InMemoryCacheWithDelayQueue INSTANCE;

    public InMemoryCacheUtils(InMemoryCacheWithDelayQueue queue) {
        InMemoryCacheUtils.INSTANCE = queue;
    }
    
    public static InMemoryCacheWithDelayQueue cache(){
        return INSTANCE;
    }
}
