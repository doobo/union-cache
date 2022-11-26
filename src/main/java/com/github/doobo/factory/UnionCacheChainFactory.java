package com.github.doobo.factory;

import com.github.doobo.container.UnionCacheHandlerContainer;
import com.github.doobo.service.ICacheService;
import com.github.doobo.utils.ResultUtils;
import com.github.doobo.vbo.HookTuple;
import com.github.doobo.vbo.ResultTemplate;
import com.github.doobo.vbo.UnionCacheRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 字段处理工厂类
 *
 * @Description: model-schema
 * @User: diding
 * @Time: 2022-03-24 15:39
 */
@Slf4j
@Component
public class UnionCacheChainFactory {
    /**
     * 导入处理器列表
     */
    private static List<ICacheService> handlerList;

    /**
     * 添加字段处理器
     */
    public synchronized static void addHandler(ICacheService handler){
        if(Objects.isNull(handlerList)){
            handlerList = new ArrayList<>();
        }
        if(Objects.nonNull(handler)){
            handlerList.add(handler);
            handlerList.sort(Comparator.comparing(ICacheService::getPhase));
        }
    }

    /**
     * 添加字段处理器列表
     */
    public synchronized static void addHandlerList(List<ICacheService> handlers){
        if(Objects.isNull(handlerList)){
            handlerList = new ArrayList<>();
        }
        if(Objects.nonNull(handlers) && !handlers.isEmpty()){
            handlerList.addAll(handlers);
            handlerList.sort(Comparator.comparing(ICacheService::getPhase));
        }
    }

    /**
     * 移除指定类型的处理器
     */
    public static <T> boolean removeHandler(Class<T> cls){
        ICacheService handler = null;
        for(ICacheService item : handlerList){
            if(Objects.nonNull(item) && item.getClass().getName().equals(cls.getName())){
                handler = item;
                break;
            }
        }
        if(Objects.nonNull(handler)){
            return handlerList.remove(handler);
        }
        return false;
    }

    /**
     * 具体执行器
     */
    public static <T> ResultTemplate<T> executeHandler(UnionCacheRequest request, Function<ICacheService, ResultTemplate<T>> fun){
        ICacheService handler = getInstanceHandler(request);
        return execute(handler, fun, request, null);
    }

    /**
     * 具体执行器
     */
    public static <T> ResultTemplate<T> executeHandler(UnionCacheRequest request, Function<ICacheService, ResultTemplate<T>> fun, HookTuple tuple){
        ICacheService handler = getInstanceHandler(request);
        return execute(handler, fun, request, tuple);
    }

    /**
     * 获取处理器
     */
    public static ICacheService getInstanceHandler(UnionCacheRequest request){
        if(Objects.isNull(handlerList) || handlerList.isEmpty()){
            return null;
        }
        for(ICacheService item : handlerList){
            if (matching(request, item::matching)){
                return item;
            }
        }
        return null;
    }

    /**
     * 属性字段匹配器
     */
    public static List<ICacheService> matchingList(UnionCacheRequest request){
        if(Objects.isNull(handlerList) || handlerList.isEmpty()){
            return Collections.emptyList();
        }
        return handlerList.stream().filter(Objects::nonNull)
                .filter(f -> matching(request, f::matching)).collect(Collectors.toList());
    }

    /**
     * 判断,并具体执行方法
     */
    private static <T> ResultTemplate<T> execute(ICacheService handler, Function<ICacheService, ResultTemplate<T>> fun, UnionCacheRequest request, HookTuple tuple) {
        if(Objects.isNull(handler)){
            return ResultUtils.ofFail("未匹配到执行器");
        }
        try {
            Optional.ofNullable(tuple).flatMap(c -> Optional.ofNullable(c.beforeTuple(handler))).ifPresent(n -> n.accept(request));
            return fun.apply(handler);
        }catch (Throwable e){
            if(Objects.isNull(tuple)){
                throw e;
            }
            Consumer<Object> orElse = Optional.ofNullable(tuple.errorTuple(e)).orElse(null);
            if(Objects.isNull(orElse)){
                throw e;
            }
            orElse.accept(request);
        }finally {
            Optional.ofNullable(tuple).flatMap(c -> Optional.ofNullable(c.endTuple(handler))).ifPresent(n -> n.accept(request));
        }
        return ResultUtils.ofFail("方法执行异常");
    }

    /**
     * 匹配执行器
     */
    private static boolean matching(UnionCacheRequest request, Predicate<UnionCacheRequest> predicate){
        return predicate.test(request);
    }

    /**
     * 获取所有处理器
     */
    public static List<ICacheService> getHandlerList() {
        return handlerList;
    }

	/*初始化*/
	static {
        addHandlerList(UnionCacheHandlerContainer.getHandlerList());
    }

    /**
     * 注册缓存服务
     */
    @Autowired(required = false)
    public void registerCacheServiceList(List<ICacheService> list) {
        UnionCacheChainFactory.addHandlerList(list);
    }
}
