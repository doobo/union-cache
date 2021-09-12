package com.github.doobo.service;

/**
 * 自定义缓存接口
 */
public interface ICache {

    /**
     * 添加缓存值 K，V键值对
     *
     * @param key            Key值
     * @param value          V值
     * @param periodInMillis 过期时间（毫秒）
     */
    void add(String key, Object value, long periodInMillis);

    /**
     * 根据Key值删除该Key对应的缓存值
     *
     * @param key Key键
     */
    void remove(String key);

    /**
     * 根据Key值获取缓存值
     *
     * @param key Key键
     */
    Object get(String key);

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 缓存的大小
     */
    long size();

    /**
     * 简单批量删除
     */
    int batchClear(String key);

}
