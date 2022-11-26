# union-cache

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

> 基于springboot的注解式缓存,方便集成多种缓存(redis、MemCache)而不改变原有代码逻辑,防止雪崩等,
> 默认基于ConcurrentHashMap实现了本地缓存,通过实现ICacheService即可替换成redis或者MemCache缓存

## 如何添加
```
 <dependency>
   <groupId>com.github.doobo</groupId>
   <artifactId>union-cache</artifactId>
   <version>1.3</version>
 </dependency>
```

## 几种注解
```
//读缓存,先判断key是否有缓存,有就从缓存读取返回(不执行方法)，否则就执行一次方法，并把结果写入缓存
@RCache(prefix = "index", key = "key", expiredTime = 10)
@RCache(prefix = CacheConfig.USER_PREFIX, key = "#key", expiredTime = CacheConfig.USER_EXPIRED_TIME, unless = "#result == null || !#result.ok")

//更新缓存,用于方法上,方法执行后，更新到缓存
@UCache(prefix = CacheConfig.USER_PREFIX, key = "#key", expiredTime = CacheConfig.USER_EXPIRED_TIME)

//删除缓存,方法执行后,删除缓存
@DCache(prefix = CacheConfig.USER_PREFIX, key = "#key", unless = "#result == null || !#result.ok")
```

## 简单使用
```java
@RestController
public class IndexController {

    /**
     * 每30秒钟,缓存失效,执行一次UUID
     */
    @GetMapping
    @RCache(prefix = "uuid", key = "key", expiredTime = 30)
    public Map<String,Object> index(){
        return Collections.singletonMap("key", UUID.randomUUID().toString());
    }

    /**
     * 更缓存
     */
    @GetMapping("update")
    @UCache(prefix = "uuid", key = "#uuid", expiredTime = 10)
    public Map<String,Object> update(String uuid){
        return Collections.singletonMap("key", uuid);
    }

    /**
     * 删除缓存
     */
    @GetMapping("delete")
    @DCache(prefix = "uuid", key = "key")
    public Boolean delete(String key){
        return Boolean.TRUE;
    }

    /**
     * 批量删除
     */
    @GetMapping("batch")
    @DCache(prefix = "uuid", key = "*", batchClear = true)
    public Boolean batchClear(){
        return Boolean.TRUE;
    }
}
```

## 本地缓存实现
```java
@Service
public class UnionLocalCacheService implements ICacheService{

    @Override
    public ResultTemplate<Boolean> setCache(UnionCacheRequest request) {
        InMemoryCacheUtils.cache().add(request.getKey(), request.getValue(), TimeUnit.SECONDS.toMillis(request.getExpire()));
        return ResultUtils.of(true);
    }

    @Override
    public ResultTemplate<Object> getCache(UnionCacheRequest request) {
        return ResultUtils.ofUnsafe(InMemoryCacheUtils.cache().get(request.getKey()));
    }

    @Override
    public ResultTemplate<Boolean> clearCache(UnionCacheRequest request) {
        InMemoryCacheUtils.cache().remove(request.getKey());
        return ResultUtils.of(true);
    }

    @Override
    public ResultTemplate<Integer> batchClear(UnionCacheRequest request) {
        int count = InMemoryCacheUtils.cache().batchClear(request.getKey());
        return ResultUtils.of(Integer.valueOf(count));
    }

    @Override
    public boolean enableCompress() {
        return Boolean.FALSE;
    }

    @Override
    public boolean enableCache() {
        return Boolean.TRUE;
    }
}
```

License
-------
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
