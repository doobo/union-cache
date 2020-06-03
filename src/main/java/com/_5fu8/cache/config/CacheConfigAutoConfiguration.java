package com._5fu8.cache.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;


/**
 * 基本配置类
 */
@Configuration
@ComponentScans({@ComponentScan("com._5fu8.cache.annotation"), @ComponentScan("com._5fu8.cache.service")})
public class CacheConfigAutoConfiguration {
}
