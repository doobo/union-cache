package com.github.doobo.service;

import lombok.Data;

import java.lang.ref.SoftReference;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 带key和过期时间的缓存对象
 */
@Data
public class DelayedCacheObject implements Delayed {

    private final String key;
    private final SoftReference<Object> reference;
    private final long expiryTime;

    public DelayedCacheObject(String key, SoftReference<Object> reference, long expiryTime) {
        this.key = key;
        this.reference = reference;
        this.expiryTime = expiryTime;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expiryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(expiryTime, ((DelayedCacheObject) o).expiryTime);
    }
}
