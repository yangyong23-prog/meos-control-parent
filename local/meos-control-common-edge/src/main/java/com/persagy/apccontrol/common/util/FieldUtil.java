package com.persagy.apccontrol.common.util;

import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.reflect.Field;


/**
 * @author zhangyanchang
 * @version 1.0
 * @title: FieldUtil
 * @description: Never give up
 * @date: 2024/1/8 15:44
 */
public class FieldUtil {

    public static boolean allFieldsNotNull(Object obj) throws IllegalAccessException {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Schema schema = field.getAnnotation(Schema.class);
            if (schema != null) {
                boolean required = schema.required();
                Object value = field.get(obj);
                if (required && (value == null || (value instanceof String && ((String) value).isEmpty()))) {
                    return false;
                }
            }
        }
        return true;
    }

}
