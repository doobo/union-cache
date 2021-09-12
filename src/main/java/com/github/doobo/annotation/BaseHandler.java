package com.github.doobo.annotation;

import com.github.doobo.service.ICacheService;
import com.github.doobo.service.ICacheServiceUtils;
import com.github.doobo.utils.ClassUtils;
import com.github.doobo.utils.ZipUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 缓存常用处理器
 * @author qpc
 */
@Slf4j
public class BaseHandler {

	private final static String STRING = "string";

    /**
     * 转换成缓存基础对象
     */
    CacheSpELVO covertRCacheVO(String prefix, String key, String[] pNames, Object[] arg) {
        CacheSpELVO cacheVO = new CacheSpELVO();
        //基础信息配置
        boolean isSpEl = true;
        if(key != null && !key.contains("#")){
            isSpEl = false;
        }
		if(key != null && !key.isEmpty()){
            //处理key表达式
            String rk = handleSpEL(key, pNames, arg, cacheVO, isSpEl);
            if(rk != null){
                cacheVO.setKey(rk);
            }else{
                cacheVO.setKey(key);
            }
		}
        isSpEl = prefix == null || prefix.contains("#");
		if(prefix != null && !prefix.isEmpty()){
            //处理key表达式
            String pk = handleSpEL(prefix, pNames, arg, cacheVO, isSpEl);
            if(pk != null){
                cacheVO.setPrefix(pk);
            }else{
                cacheVO.setPrefix(prefix);
            }
		}
        return cacheVO;
    }

    /**
     * 处理sqEL语法
     */
    private String handleSpEL(String key, String[] pNames, Object[] arg, CacheSpELVO cacheVO, boolean isSpEl) {
        if(isSpEl){
            //如果参数异常,不处理key
            if(pNames == null || arg == null || pNames.length > arg.length){
            	log.info("BaseHandler参数异常,不处理key");
			}else{
                StandardEvaluationContext context = new StandardEvaluationContext();
                for(int i = 0; i < pNames.length; i++){
                    context.setVariable(pNames[i], arg[i]);
                }
                ExpressionParser parser = new SpelExpressionParser();
                return parser.parseExpression(key).getValue(context, String.class);
            }
        }
        return null;
    }

    /**
     * 是否缓存值判断,true不缓存
     */
    boolean unlessCheck(String unless, Object result, String[] pNames, Object[] arg){
        if(unless == null || unless.isEmpty()){
            return false;
        }
        if(!unless.contains("#")){
			return "true".equals(unless);
        }
        if(pNames == null || arg == null || pNames.length > arg.length){
            return false;
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        for(int i = 0; i < pNames.length; i++){
            context.setVariable(pNames[i], arg[i]);
        }
        context.setVariable("result", result);
        ExpressionParser parser = new SpelExpressionParser();
        Boolean value = parser.parseExpression(unless).getValue(context, Boolean.class);
        return value == null? Boolean.FALSE: value;
    }

    /**
     * 获取方法的真实返回类型
     */
    Type getType(ProceedingJoinPoint pjp, RCache cache){
        //获取方法返回值类型
		try {
			//获取方法
			MethodSignature mSig = (MethodSignature) pjp.getSignature();
 			Method currentMethod = pjp.getTarget().getClass().getMethod(mSig.getName(), mSig.getParameterTypes());
			//获取返回值类型
			return currentMethod.getAnnotatedReturnType().getType();
		} catch (Exception e) {
			log.error("获取返回类型失败", e);
		}
		return cache.cacheType();
	}

    /**
     * 基本类、String类多返回string
     * 自定义类多返回object
     */
    String getReturnType(Class<?> returnType){
        //自定义类
        //自定义类型,序列化根据
        if(!ClassUtils.isJavaClass(returnType)){
            return "object";
        }
        //基本类
        if(ClassUtils.isWrapClass(returnType)){
            return STRING;
        }
        //字符串
        if(String.class.equals(returnType)){
            return STRING;
        }
        //list和set
        List<Class<?>> list = ClassUtils.getSuperClass(returnType);
        if(!list.isEmpty()){
            Class<?> c = list.stream().filter(List.class::equals).findFirst().orElse(null);
            if(c != null){
                return "List";
            }
            c = list.stream().filter(AbstractList.class::equals).findFirst().orElse(null);
            if(c != null){
                return "List";
            }
            c = list.stream().filter(Set.class::equals).findFirst().orElse(null);
            if(c != null){
                return "Set";
            }
            c = list.stream().filter(AbstractSet.class::equals).findFirst().orElse(null);
            if(c != null){
                return "Set";
            }
            c = list.stream().filter(Map.class::equals).findFirst().orElse(null);
            if(c != null){
                return "Map";
            }
            c = list.stream().filter(AbstractMap.class::equals).findFirst().orElse(null);
            if(c != null){
                return "Map";
            }
        }
        //其它类型,和string类型一样,直接存进去
        return STRING;
    }

	/**
	 * 设置缓存
	 */
    void saveCache(Object redisCacheResult, String redisKey, String rType, int i) {
		if(STRING.equals(rType)){
            ICacheServiceUtils.getCacheService().setCache(redisKey, redisCacheResult, i);
		}else if("List".equals(rType) || "object".equals(rType) || "Map".equals(rType)){
		    if(ICacheServiceUtils.getCacheService().enableCompress()) {
                ICacheServiceUtils.getCacheService().setCache(redisKey, ZipUtils.zip(JSON.toJSONString(redisCacheResult)), i);
            }else{
                ICacheServiceUtils.getCacheService().setCache(redisKey, redisCacheResult, i);
            }
		}else{
            ICacheServiceUtils.getCacheService().setCache(redisKey, redisCacheResult, i);
		}
	}

}
