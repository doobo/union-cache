package com.github.doobo.annotation;

import lombok.Data;
import lombok.experimental.Accessors;


/**
 * 缓存注解SpEl解析结果
 *
 * @author qpc
 */
@Data
@Accessors(chain = true)
public class CacheSpELVO {

    /**
     * 前缀后面会拼参数
     */
    private String prefix = "";

    /**
     * 缓存key
     */
    private String key = "";
}

