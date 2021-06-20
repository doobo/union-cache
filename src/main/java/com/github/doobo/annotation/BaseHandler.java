package com.github.doobo.annotation;

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
     * @return
     */
    CacheSpELVO covertRCacheVO(String prefix, String key, String[] pNames, Object[] arg) {
        CacheSpELVO cacheVO = new CacheSpELVO();
        //基础信息配置
        boolean isSpEl = true;
        if(key != null && !key.contains("#")){
            cacheVO.setKey(key);
            isSpEl = false;
        }
		if(key != null && key.isEmpty()){
			isSpEl = false;
		}
        //处理key表达式
        handleSpEL(key, pNames, arg, cacheVO, isSpEl);
        isSpEl = true;
        if(prefix != null && !prefix.contains("#")){
            cacheVO.setPrefix(prefix);
            isSpEl = false;
        }
		if(prefix != null && prefix.isEmpty()){
			isSpEl = false;
		}
        handleSpEL(prefix, pNames, arg, cacheVO, isSpEl);
        return cacheVO;
    }

    /**
     * 处理sqEL语法
     * @param key
     * @param pNames
     * @param arg
     * @param cacheVO
     * @param isSpEl
     */
    private void handleSpEL(String key, String[] pNames, Object[] arg, CacheSpELVO cacheVO, boolean isSpEl) {
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
                String str = parser.parseExpression(key).getValue(context, String.class);
                cacheVO.setKey(str);
            }
        }
    }

    /**
     * 是否缓存值判断,true不缓存
     * @param unless
     * @param result
     * @param pNames
     * @param arg
     * @return
     */
    boolean unlessCheck(String unless, Object result, String[] pNames, Object[] arg){
        if(unless == null){
            return false;
        }
        if(!unless.contains("#") && !unless.isEmpty()){
			return "true".equals(unless);
        }
        if(unless.isEmpty()){
        	return false;
		}
        if(pNames == null || arg == null || pNames.length > arg.length){
            return false;
        }else{
            StandardEvaluationContext context = new StandardEvaluationContext();
            for(int i = 0; i < pNames.length; i++){
                context.setVariable(pNames[i], arg[i]);
            }
            context.setVariable("result", result);
            ExpressionParser parser = new SpelExpressionParser();
            Boolean is = parser.parseExpression(unless).getValue(context, Boolean.class);
            return is;
        }
    }

    /**
     * 获取方法的真实返回类型
     * @param pjp
     * @return
     * @throws NoSuchMethodException
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
     * @param returnType
     * @return
     */
    String getReturnType(Class returnType){
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
        if(list != null && !list.isEmpty()){
            Class c = list.stream().filter(List.class::equals).findFirst().orElse(null);
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
	 * @param redisCacheResult
	 * @param redisKey
	 * @param rType
	 * @param i
	 */
    void saveCache(Object redisCacheResult, String redisKey, String rType, int i) {
		if(STRING.equals(rType)){
            ICacheServiceUtils.getCacheService().setCache(redisKey, redisCacheResult, i);
		}else if("List".equals(rType) || "object".equals(rType) || "Map".equals(rType)){
            ICacheServiceUtils.getCacheService().setCache(redisKey, ZipUtils.zip(JSON.toJSONString(redisCacheResult)), i);
		}else{
            ICacheServiceUtils.getCacheService().setCache(redisKey, redisCacheResult, i);
		}
	}

}
