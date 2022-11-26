package com.github.doobo.annotation;

import com.github.doobo.service.ICacheServiceUtils;
import com.github.doobo.utils.ZipUtils;
import com.alibaba.fastjson.JSON;
import com.github.doobo.vbo.Builder;
import com.github.doobo.vbo.ResultTemplate;
import com.github.doobo.vbo.UnionCacheRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Slf4j
@Component
public class ReadCacheHandler extends BaseHandler{

	/**
	 * 用于定位寻找注解
	 */
	@Pointcut("@annotation(com.github.doobo.annotation.RCache)")
	public void methodCachePointcut() {
	}

	@Around("methodCachePointcut()")
	public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
		if(!ICacheServiceUtils.enableCache()){
			return proceedingJoinPoint.proceed();
		}
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
			String redisKey = sb.toString();
			//获取returnType类型
			String rType = getReturnType(method.getReturnType());
			UnionCacheRequest request = Builder.of(UnionCacheRequest::new)
					.with(UnionCacheRequest::setMethod, method)
					.with(UnionCacheRequest::setRCache, cache)
					.with(UnionCacheRequest::setKey, redisKey)
					.with(UnionCacheRequest::setExpire, cache.expiredTime())
					.build();
			try {
				//获取缓存值,并转为相应的类型
				Object obj = null;
				if(cache.isBatch()){
					ResultTemplate<Object> template = ICacheServiceUtils.batchReadCache(request);
					Optional.ofNullable(template).filter(c -> !c.isSuccess())
							.ifPresent(l -> log.error("batchReadCacheError:{}", l));
					if(Objects.nonNull(template) && template.isSuccess()) {
						obj = template.getData();
					}
				}else{
					ResultTemplate<Object> template = ICacheServiceUtils.getCache(request);
					Optional.ofNullable(template).filter(c -> !c.isSuccess())
							.ifPresent(l -> log.error("getCacheError:{}", l));
					if(Objects.nonNull(template) && template.isSuccess()) {
						obj = template.getData();
					}
				}
				if(Objects.isNull(obj)){
					log.debug("Cache key:{} is null", redisKey);
				}else if("string".equals(rType)){
					redisCacheResult = obj;
				}else if("List".equals(rType) || "object".equals(rType) || "Map".equals(rType)){
					if(ICacheServiceUtils.enableCompress()) {
						redisCacheResult = JSON.parseObject(ZipUtils.unzip(String.valueOf(obj)), getType(proceedingJoinPoint, cache));
					}else{
						redisCacheResult = obj;
					}
				}else{
					redisCacheResult = obj;
				}
			} catch(Exception e) {
				log.warn("obtain value from redis error. key:{},", redisKey, e);
			}
			if(Objects.nonNull(redisCacheResult)){
				return redisCacheResult;
			}
			redisCacheResult = proceedingJoinPoint.proceed();
			//检测是否需要缓存
			boolean unless = super.unlessCheck(cache.unless(), redisCacheResult, methodSignature.getParameterNames(), args);
			if(Objects.nonNull(redisCacheResult) && !unless){
				try {
					request.setValue(redisCacheResult);
					saveCache(request, cache.isBatch(), rType);
				} catch (Exception e) {
					log.error("set value to redis error. key:{},", redisKey, e);
				}
			}
		} catch (Throwable e) {
			log.error("ReadCacheHandlerErr:", e);
			throw e;
		}
		return redisCacheResult;
	}
}
