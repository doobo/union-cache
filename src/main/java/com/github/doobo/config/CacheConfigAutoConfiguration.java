package com.github.doobo.config;

import com.github.doobo.service.ICacheService;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;


/**
 * 基本配置类
 */
@Configuration
@AutoConfigureOrder(ICacheService.DEFAULT_PHASE)
@ComponentScans({
        @ComponentScan("com.github.doobo.annotation")
        , @ComponentScan("com.github.doobo.factory")
        })
public class CacheConfigAutoConfiguration {
}
