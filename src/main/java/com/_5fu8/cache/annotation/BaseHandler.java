package com._5fu8.cache.annotation;

import com._5fu8.cache.utils.ClassUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 缓存常用处理器
 */
public class BaseHandler {

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
            }else{
                StandardEvaluationContext context = new StandardEvaluationContext();
                for(int i = 0; i < pNames.length; i++){
                    context.setVariable(pNames[i], arg[i]);
                }
                ExpressionParser parser = new SpelExpressionParser();
                Object str = parser.parseExpression(key).getValue(context);
                cacheVO.setKey(str.toString());
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
        if(unless != null && !unless.contains("#")){
            if(unless == null || !unless.isEmpty()){
                return "true".equals(unless)?true:false;
            }
        }
        if(unless != null && unless.isEmpty()){
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
    Type getType(ProceedingJoinPoint pjp) throws NoSuchMethodException {
        //获取方法返回值类型
        Object[] args = pjp.getArgs();
        Class<?>[] paramsCls = new Class<?>[args.length];
        for (int i = 0; i < args.length; ++i) {
            paramsCls[i] = args[i].getClass();
        }
        //获取方法
        Method method = pjp.getTarget().getClass().getMethod(pjp.getSignature().getName(), paramsCls);
        //获取返回值类型
        return method.getAnnotatedReturnType().getType();
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
            return "string";
        }
        //字符串
        if(String.class.equals(returnType)){
            return "string";
        }
        //list和set
        List<Class<?>> list = ClassUtils.getSuperClass(returnType);
        if(list != null && !list.isEmpty()){
            Class c = list.stream().filter(f->List.class.equals(f)).findFirst().orElse(null);
            if(c != null){
                return "List";
            }
            c = list.stream().filter(f->AbstractList.class.equals(f)).findFirst().orElse(null);
            if(c != null){
                return "List";
            }
            c = list.stream().filter(f->Set.class.equals(f)).findFirst().orElse(null);
            if(c != null){
                return "Set";
            }
            c = list.stream().filter(f->AbstractSet.class.equals(f)).findFirst().orElse(null);
            if(c != null){
                return "Set";
            }
            c = list.stream().filter(f->Map.class.equals(f)).findFirst().orElse(null);
            if(c != null){
                return "Map";
            }
            c = list.stream().filter(f->AbstractMap.class.equals(f)).findFirst().orElse(null);
            if(c != null){
                return "Map";
            }
        }
        //其它类型,和string类型一样,直接存进去
        return "string";
    }


}
