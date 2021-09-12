package com.github.doobo;

import com.github.doobo.annotation.DCache;
import com.github.doobo.annotation.RCache;
import com.github.doobo.annotation.UCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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
    @UCache(prefix = "uuid", key = "#uuid", expiredTime = 10)
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
}
