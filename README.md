# union-cache

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

> 基于springboot的注解式缓存,自定义实现接口,方便集成多种缓存(redis、memcache)而不改变原有代码逻辑,防止雪崩等.
## 如何添加
```
 <dependency>
   <groupId>com.github.doobo</groupId>
   <artifactId>union-cache</artifactId>
   <version>1.1</version>
 </dependency>
```

## 1 简单使用
```java
/**
 * 用户相关操作接口
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	RestTemplate restTemplate;

	/**
	 * 用户登录、注册等接口的URL
	 */
	@Value("${com.userUrls}")
	private String userUrls;

	@Autowired
	UserService userService;

	/**
	 * 外网用户登录
	 * @return
	 */
	@Override
	public ResultTemplate<List<OuterToken>> outerLogin(OutUserLogin body) {
		ResultTemplate<List<OuterToken>> loginInfo = RestTemplateUtil.postExchange(userUrls + "/outerUser/login"
			, ContextHolderUtil.getRequest()
			//,body
			,new ParameterizedTypeReference<ResultTemplate<List<OuterToken>>>() {});
		if(loginInfo != null && loginInfo.getOk() && loginInfo.getData() != null && !loginInfo.getData().isEmpty()){
			//当前类的方法,若想缓存生效,需要注入self,代理才会执行
			userService.addOuterLoginCache(loginInfo.getData().get(0).getAccessToken(), loginInfo);
		}
		return loginInfo;
	}

	/**
	 * 设置登录缓存
	 * @return
	 */
	@Override
	@UCache(prefix = CacheConfig.USER_PREFIX, key = "#key", expiredTime = CacheConfig.USER_EXPIRED_TIME)
	public ResultTemplate<List<OuterToken>> addOuterLoginCache(String key, ResultTemplate<List<OuterToken>> obj) {
		//可以对obj进行相关判断,设置obj为null,则不会缓存到redis,也可以进行其它逻辑处理
		return obj;
	}

	/**
	 * 从redis获取用户登录信息
	 * @return
	 */
	@Override
	@RCache(prefix = CacheConfig.USER_PREFIX, key = "#key", expiredTime = CacheConfig.USER_EXPIRED_TIME, unless = "#result == null || !#result.ok")
	public ResultTemplate<List<OuterToken>> getOuterLogin(String key) {
		//这里可实现未获取到用户信息时,进行的相关业务处理
		//如果缓存不存在，返回null或者unless为true不进行缓存,否则会存返回值哦
		return ResultUtils.fail("登录信息已失效",401);
	}

	/**
	 * 删除用户登录信息
	 * @return
	 */
	@Override
	@DCache(prefix = CacheConfig.USER_PREFIX, key = "#key", unless = "#result == null || !#result.ok")
	public ResultTemplate delOuterLogin(String key) {
		//调用用户中心接口,注销登录信息
		ResultTemplate<List<OuterToken>> rs = userService.getOuterLogin(key);
		if(rs == null || !rs.getOk()){
			//当前登录信息已失效
			return ResultUtils.fail("当前登录信息已经失效", 401);
		}
		//返回null不删除,其它则删除
		return ResultUtils.success(true);
	}
}

```

## 2 redis接口实现
```
   import com.yizheng.annotation.ICacheService;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.data.redis.core.RedisTemplate;
   import org.springframework.stereotype.Service;
   
   import java.util.concurrent.TimeUnit;
   
   @Service
   public class ICacheServiceImpl implements ICacheService {
   
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
   
       @Override
       public Object getSortedSetRange(String key, int start, int end) {
           return null;
       }
       
       @Override
       public boolean enable() {
          //是否启用缓存,false不启用,跳过缓存注解
          return true;
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
