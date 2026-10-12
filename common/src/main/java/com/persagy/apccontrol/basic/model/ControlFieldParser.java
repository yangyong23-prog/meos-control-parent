package com.persagy.apccontrol.basic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.NonNull;

import java.beans.PropertyDescriptor;
import java.util.*;

/**
 * 管控字段解析基类
 *
 * @author JiYinchuan
 * @deprecated 切换 {@link com.persagy.apccontrol.basic.structure.common.parser.PropertyDescriptorParser}
 * @since v5.6.0
 */
@Deprecated
public interface ControlFieldParser {

    Map<String, Set<String>> CLASS_AND_CONTROL_FIELD_NAME = new HashMap<>(16);
    Map<String, Map<String, PropertyDescriptor>> CLASS_CONTROL_FIELD_NAME_AND_DESCRIPTOR = new HashMap<>(16);

    /**
     * 控制字段名称
     *
     * @return 控制字段名称
     * @since v5.7.0
     */
    @JsonIgnore
    default Set<String> controlFieldNames() {
        return cacheControlFieldNames();
    };

    /**
     * 缓存控制参数字段名称
     *
     * @return 缓存控制参数字段名称
     * @since v5.6.0
     */
    @JsonIgnore
    default Set<String> cacheControlFieldNames() {
        String targetClassName = this.getClass().getName();
        Set<String> controlFieldNames = CLASS_AND_CONTROL_FIELD_NAME.get(targetClassName);
        if (Objects.nonNull(controlFieldNames)) {
            return controlFieldNames;
        }
        controlFieldNames = controlFieldNames();
        CLASS_AND_CONTROL_FIELD_NAME.put(targetClassName, controlFieldNames);
        return controlFieldNames;
    }

    /**
     * 获取对象属性访问器
     *
     * @param fieldName 属性名称
     * @return 对象属性访问器
     * @since v5.6.0
     */
    @SneakyThrows
    @JsonIgnore
    default PropertyDescriptor cachePropertyDescriptor(@NonNull String fieldName) {
        String targetClassName = this.getClass().getName();
        Map<String, PropertyDescriptor> controlFieldAndDescriptor = Optional.ofNullable(
                CLASS_CONTROL_FIELD_NAME_AND_DESCRIPTOR.get(targetClassName)).orElseGet(HashMap::new);
        PropertyDescriptor descriptor;
        if (MapUtils.isEmpty(controlFieldAndDescriptor) || !controlFieldAndDescriptor.containsKey(fieldName)) {
            descriptor = new PropertyDescriptor(fieldName, this.getClass());
            controlFieldAndDescriptor.put(fieldName, descriptor);
        } else {
            descriptor = controlFieldAndDescriptor.get(fieldName);
        }
        CLASS_CONTROL_FIELD_NAME_AND_DESCRIPTOR.put(targetClassName, controlFieldAndDescriptor);
        return descriptor;
    }
}
