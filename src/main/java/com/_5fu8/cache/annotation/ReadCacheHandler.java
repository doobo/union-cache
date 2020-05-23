package com._5fu8.cache.annotation;

import com._5fu8.cache.service.ICacheService;
import com._5fu8.cache.utils.ZipUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class ReadCacheHandler extends BaseHandler{

	@Autowired(required = false)
	ICacheService iCacheService;

	@Pointcut("@annotation(com._5fu8.cache.annotation.RCache)")
	public void methodCachePointcut() {
	}

	@Around("methodCachePointcut()")
	public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
		Object redisCacheResult = null;
		try {
			MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
			Method method = methodSignature.getMethod();
			RCache cache = method.getAnnotation(RCache.class);
			StringBuilder sb = new StringBuilder();
			Object[] args = proceedingJoinPoint.getArgs();
			CacheSpELVO vo = super.covertRCacheVO(cache.prefix(), cache.key(), methodSignature.getParameterNames(), args);
			if(vo != null && vo.getPrefix() != null && !vo.getPrefix().isEmpty()){
				sb.append(vo.getPrefix()).append(cache.symbol());
			}
			if(vo != null && vo.getKey() != null && !vo.getKey().isEmpty()){
				sb.append(vo.getKey());
			}
			//返回结构
			redisCacheResult = null;
			String redisKey = sb.toString();
			//获取returnType类型
			String rType = getReturnType(method.getReturnType());
			try {
				//获取缓存值,并转为相应的类型
				Object obj = iCacheService.getCache(redisKey);
				if(obj == null){
					//空值或者不存在,直接返回
					redisCacheResult = obj;
				}else if("string".equals(rType)){
					redisCacheResult = obj;
				}else if("List".equals(rType) || "object".equals(rType) || "Map".equals(rType)){
					redisCacheResult = JSON.parseObject(ZipUtils.unzip(String.valueOf(obj)), getType(proceedingJoinPoint));
				}else{
					redisCacheResult = obj;
				}
			} catch (Exception e) {
				log.warn("obtain value from redis error. key:",redisKey);
			}
			if(redisCacheResult != null){
				return redisCacheResult;
			}
			redisCacheResult = proceedingJoinPoint.proceed();
			//检测是否需要缓存
			boolean unless = super.unlessCheck(cache.unless(), redisCacheResult, methodSignature.getParameterNames(), args);
			if(redisCacheResult != null && !unless){
				try {
					if("string".equals(rType)){
						iCacheService.setCache(redisKey, redisCacheResult, cache.expiredTime());
					}else if("List".equals(rType) || "object".equals(rType) || "Map".equals(rType)){
						iCacheService.setCache(redisKey, ZipUtils.zip(JSON.toJSONString(redisCacheResult)), cache.expiredTime());
					}else{
						iCacheService.setCache(redisKey, redisCacheResult, cache.expiredTime());
					}
				} catch (Exception e) {
					log.warn("set value to redis error. key: " + redisKey);
				}
			}
		} catch (Exception e) {
			//redis出现未知异常,直接执行原有方法,不影响逻辑,有可能方法表达式、sqEL错误、参数空异常等
			if(redisCacheResult == null){
				redisCacheResult = proceedingJoinPoint.proceed();
			}
			log.error("ReadCacheHandlerErr", e);
		}
		return redisCacheResult;
	}
}
