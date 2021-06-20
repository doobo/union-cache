package com.github.doobo.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 常用类工具
 * @author qpc
 */
public class ClassUtils {

	private ClassUtils() {
	}

	/**
     * 判断一个类型是Java本身的类型，还是用户自定义的类型
     * @param clz
     * @return
     */
    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

    /**
     * 判断是基本类还是封装类
     * .isPrimitive()是用来判断是否是基本类型的：void.isPrimitive() //true;
     * @param clz
     * @return
     */
    public static boolean isWrapClass(Class clz) {
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     *
     * 获取除去Object的类继承关系
     * @param clz
     * @return
     */
    public static List<Class<?>> getSuperClass(Class<?> clz){
        Class<?> superclass = clz.getSuperclass();
        List<Class<?>> all = new ArrayList<>();
        all.add(clz);
        while (superclass != null) {
            if(superclass.isAssignableFrom(Object.class)){
                break;
            }
            all.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return all;
    }
}
