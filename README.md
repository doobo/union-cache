# union-cache

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

> 基于springboot的注解式缓存,方便集成多种缓存(redis、MemCache)而不改变原有代码逻辑,防止雪崩等,
> 默认基于ConcurrentHashMap实现了本地缓存,通过继承AbstractCacheService即可替换成redis或者MemCache缓存

## 如何添加
```
 <dependency>
   <groupId>com.github.doobo</groupId>
   <artifactId>union-cache</artifactId>
   <version>1.2</version>
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
     * 每十秒钟,缓存失效,执行一次UUID
     */
    @GetMapping
    @RCache(prefix = "index", key = "key", expiredTime = 10)
    public String index(){
        return UUID.randomUUID().toString();
    }

    /**
     * 更缓存
     */
    @GetMapping("update")
    @UCache(prefix = "index", key = "key", expiredTime = 10)
    public String update(String uuid){
        return uuid;
    }

    /**
     * 删除缓存
     */
    @GetMapping("delete")
    @DCache(prefix = "index", key = "key")
    public Boolean delete(String key){
        return Boolean.TRUE;
    }
}
```

## 2 redis接口实现
```

@Service
public class ICacheServiceImpl extends AbstractCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setCache(String key, Object value, int expire) {
       redisTemplate.opsForValue().set(key, value);
       redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    @Override
    public Object getCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void clearCache(String key) {
        redisTemplate.delete(key);
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
