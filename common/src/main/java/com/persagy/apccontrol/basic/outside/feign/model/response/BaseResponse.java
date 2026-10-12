package com.persagy.apccontrol.basic.outside.feign.model.response;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 核心响应类
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
public class BaseResponse {

    /**
     * 未定义属性列表
     */
    private Map<String, Object> undefinedProperties;

    /**
     * 设置属性
     *
     * @param key   键
     * @param value 值
     * @since v5.1.0
     */
    @JsonAnySetter
    public void anySetter(String key, Object value) {
        if (Objects.isNull(undefinedProperties)) {
            undefinedProperties = new HashMap<>(16);
        }
        undefinedProperties.put(key, value);
    }
}
