package com.github.doobo.annotation;

import com.github.doobo.service.ICacheServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author qpc
 */
@Aspect
@Component
@Slf4j
public class DeleteCacheHandler extends BaseHandler{

	/**
     * 用于定位寻找注解
	 */
	@Pointcut("@annotation(com.github.doobo.annotation.DCache)")
	public void methodCachePointcut() {
	}

	@Around("methodCachePointcut()")
	public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
		if(!ICacheServiceUtils.getCacheService().enableCache()){
			return proceedingJoinPoint.proceed();
		}
		Object redisCacheResult = null;
		try {
			MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
			Method method = methodSignature.getMethod();
			DCache cache = method.getAnnotation(DCache.class);
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
			String redisKey = sb.toString();
			redisCacheResult = proceedingJoinPoint.proceed();
			//检测是否需要缓存
			boolean unless = super.unlessCheck(cache.unless(), redisCacheResult, methodSignature.getParameterNames(), args);
			if(redisCacheResult != null && !unless){
				try {
					ICacheServiceUtils.getCacheService().clearCache(redisKey);
				} catch (Exception e) {
					log.warn("delete value to redis error. key: " + redisKey);
				}
			}
		} catch (Exception e) {
			//redis出现未知异常,直接执行原有方法,不影响逻辑,有可能方法表达式、sqEL错误、参数空异常等
			if(redisCacheResult == null){
				redisCacheResult = proceedingJoinPoint.proceed();
			}
			log.error("DeleteCacheHandlerErr", e);
		}
		return redisCacheResult;
	}
}
