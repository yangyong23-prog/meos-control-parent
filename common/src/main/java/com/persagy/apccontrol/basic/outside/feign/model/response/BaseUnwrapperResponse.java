package com.persagy.apccontrol.basic.outside.feign.model.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 核心响应类,自动拆包为定义属性
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Data
@ToString(callSuper = true)
public class BaseUnwrapperResponse extends BaseResponse {

    // 动态属性
    @JsonAnyGetter
    public Map<String, Object> getUndefinedProperties() {
        if (Objects.isNull(super.getUndefinedProperties())) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>(super.getUndefinedProperties());
        Arrays.stream(this.getClass().getDeclaredFields()).map(Field::getName).forEach(result::remove);
        return result;
    }

}
