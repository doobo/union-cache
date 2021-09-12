package com.github.doobo.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.ref.SoftReference;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

/**
 * 缓存实现
 */
@Data
@Slf4j
@Component
public class InMemoryCacheWithDelayQueue implements ICache {
    
    /**
     * ConcurrentHashMap是Java中的一个线程安全且高效的HashMap实现，解决涉及高并发的map结构。
     * SoftReference<Object>作为映射值，因为软引用可以保证在抛出OutOfMemory之前，如果缺少内存，将删除引用的对象。
     */
    private final ConcurrentHashMap<String, SoftReference<Object>> cache = new ConcurrentHashMap<>();
    /**
     * 延迟队列
     */
    private final DelayQueue<DelayedCacheObject> cleaningUpQueue = new DelayQueue<>();

    /**
     * 使用线程启动（安全性）
     */
    public InMemoryCacheWithDelayQueue() {
        Thread cleanerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    DelayedCacheObject delayedCacheObject = cleaningUpQueue.take();
                    cache.remove(delayedCacheObject.getKey(), delayedCacheObject.getReference());
                    log.info("缓存Key -> " + delayedCacheObject.getKey() + "，SoftReference<Object> -> " + delayedCacheObject.getReference() + "失效");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        cleanerThread.setDaemon(true);
        cleanerThread.start();
    }

    /**
     * 添加缓存
     *
     * @param key            Key值
     * @param value          V值
     * @param periodInMillis 过期时间（毫秒）
     */
    @Override
    public void add(String key, Object value, long periodInMillis) {
        if (key == null) {
            return;
        }
        if (value == null) {
            cache.remove(key);
        } else {
            long expiryTime = System.currentTimeMillis() + periodInMillis;
            SoftReference<Object> reference = new SoftReference<>(value);
            cache.put(key, reference);
            cleaningUpQueue.put(new DelayedCacheObject(key, reference, expiryTime));
        }
    }

    /**
     * 移除缓存
     *
     * @param key Key键
     */
    @Override
    public void remove(String key) {
        cache.remove(key);
    }

    /**
     * 获取缓存
     *
     * @param key Key键
     * @return 对象
     */
    @Override
    public Object get(String key) {
        return Optional.ofNullable(cache.get(key)).map(SoftReference::get).orElse(null);
    }

    /**
     * 清空缓存
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * 获取缓存数量
     *
     * @return Long值
     */
    @Override
    public long size() {
        return cache.size();
    }

}
