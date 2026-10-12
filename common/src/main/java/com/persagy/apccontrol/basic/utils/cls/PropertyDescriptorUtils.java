package com.persagy.apccontrol.basic.utils.cls;

import lombok.SneakyThrows;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;

/**
 * 属性描述符工具栏
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
public class PropertyDescriptorUtils {

    /**
     * 对象类下所有对象属性访问器
     *
     * @param cls 类型
     * @return 所有对象属性访问器
     * @since 5.241002.0
     */
    public static List<PropertyDescriptor> getPropertyDescriptors(@NonNull Class<?> cls) {
        synchronized (cls) {
            return Arrays.stream(PropertyUtils.getPropertyDescriptors(cls))
                    .map(item -> fillWriteMethodAndReturn(cls, item))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 获取对象属性访问器
     *
     * @param obj       当前对象
     * @param fieldName 属性名称
     * @return 对象属性访问器
     * @since 5.241002.0
     */
    @SneakyThrows
    public static @Nullable PropertyDescriptor getPropertyDescriptor(
            @NonNull Object obj, @NonNull String fieldName) {
        Class<?> cls = obj.getClass();
        synchronized (cls) {
            return fillWriteMethodAndReturn(cls, PropertyUtils.getPropertyDescriptor(obj, fieldName));
        }
    }

    /**
     * 填充写方法并返回
     *
     * @param cls        类型
     * @param descriptor 属性描述符
     * @return 属性描述符
     * @since 5.241002.0
     */
    private static @Nullable PropertyDescriptor fillWriteMethodAndReturn(
            @NonNull Class<?> cls, @Nullable PropertyDescriptor descriptor) {
        if (Objects.isNull(descriptor)) {
            return null;
        }
        try {
            if (Objects.isNull(descriptor.getWriteMethod())) {
                String fieldName = descriptor.getName();
                Class<?> type = descriptor.getPropertyType();
                Class<?>[] args = (type == null) ? null : new Class<?>[]{type};
                String methodName = "set" + fieldName.substring(0, 1).toUpperCase(ENGLISH) + fieldName.substring(1);
                Method writeMethod = MethodUtils.getMatchingAccessibleMethod(cls, methodName, args);
                descriptor.setWriteMethod(writeMethod);
            }
            return descriptor;
        } catch (IntrospectionException ex) {
            throw new RuntimeException(ex);
        }
    }
}
