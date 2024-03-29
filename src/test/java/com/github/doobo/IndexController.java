package com.github.doobo;

import com.github.doobo.annotation.DCache;
import com.github.doobo.annotation.RCache;
import com.github.doobo.annotation.UCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
public class IndexController {

    /**
     * 每30秒钟,缓存失效,执行一次UUID
     */
    @GetMapping
    @RCache(prefix = "uuid", key = "key", expiredTime = 30)
    public Map<String,Object> index(){
        return Collections.singletonMap("key", UUID.randomUUID().toString());
    }

    /**
     * 更缓存
     */
    @GetMapping("update")
    @UCache(prefix = "uuid", key = "key", expiredTime = 10)
    public Map<String,Object> update(String uuid){
        return Collections.singletonMap("key", uuid);
    }

    /**
     * 删除缓存
     */
    @GetMapping("delete")
    @DCache(prefix = "uuid", key = "key")
    public Boolean delete(String key){
        return Boolean.TRUE;
    }

    /**
     * 批量删除
     */
    @GetMapping("batch")
    @DCache(prefix = "uuid", key = "*", batchClear = true)
    public Boolean batchClear(){
        return Boolean.TRUE;
    }

    /**
     * 每30秒钟,缓存失效,执行一次UUID
     */
    @GetMapping("list")
    @RCache(prefix = "list", key = "key", expiredTime = 30)
    public List<String> list(){
        return Collections.singletonList(UUID.randomUUID().toString());
    }

    /**
     * 空返回删除
     */
    @GetMapping("void")
    @DCache(prefix = "uuid", key = "*", batchClear = true)
    public void voidClear(String abc){
        log.info("void clear cache:{}", abc);
    }
}
