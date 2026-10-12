package com.persagy.apccontrol.basic.model.param;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * 调试Bean调试参数
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@Schema(title = "调试Bean调试参数")
public class DebugBeanParam {

    private static final String LOG_TAG = "DebugBean";
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugBeanParam.class);

    @Schema(description = "Bean名称")
    @NotBlank(message = "{key_af90bc3dff9b45cba76e7ef68bb8db4b}")
    private String beanName;

    @Schema(description = "方法名称")
    @NotBlank(message = "{key_b6a6646e5ffc40ebbb496f16f49a191c}")
    private String methodName;

    @Schema(description = "参数列表（支持同类型参数）")
    private List<ParameterEntry> parameters;

    /**
     * 参数条目
     */
    @Data
    @Schema(title = "参数条目")
    public static class ParameterEntry {
        @Schema(description = "参数类型（完整类名），支持泛型如 java.util.List/java.lang.String")
        private String type;

        @Schema(description = "参数值")
        private Object value;
    }

    /**
     * 执行
     *
     * @return 方法返回
     * @since 5.241201.0
     */
    public Object process() {
        Object bean;
        try {
            bean = SpringUtil.getBean(beanName);
        } catch (NoSuchBeanDefinitionException e) {
            try {
                bean = Class.forName(beanName).newInstance();
            } catch (ClassNotFoundException ex) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_8c033c412fef4f74968153376a020bea"));
            } catch (InstantiationException ex) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_e6597b9d758147a994b73b0877c784da"));
            } catch (IllegalAccessException ex) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_6256c0b8e8264925a340709d766b1400"));
            }
        }

        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

        // 使用 parameters 列表
        Class<?>[] methodParameterTypes;
        Object[] methodParameters;

        if (parameters == null || parameters.isEmpty()) {
            methodParameterTypes = new Class[0];
            methodParameters = null;
        } else {
            methodParameterTypes = new Class[parameters.size()];
            methodParameters = new Object[parameters.size()];
            TypeFactory typeFactory = objectMapper.getTypeFactory();

            for (int i = 0; i < parameters.size(); i++) {
                ParameterEntry entry = parameters.get(i);
                try {
                    String[] split = entry.getType().split("/");
                    Class<?> rawType = ClassUtils.getClass(split[0]);
                    Class<?>[] types = null;
                    if (split.length > 1) {
                        types = new Class[split.length - 1];
                        for (int j = 0; j + 1 < split.length; j++) {
                            types[j] = ClassUtils.getClass(split[j + 1]);
                        }
                    }
                    JavaType javaType = types != null
                            ? typeFactory.constructParametricType(rawType, types)
                            : typeFactory.constructType(rawType);
                    methodParameterTypes[i] = rawType;
                    String parameterString = objectMapper.writeValueAsString(entry.getValue());
                    methodParameters[i] = objectMapper.readValue(parameterString, javaType);
                } catch (ClassNotFoundException e) {
                    throw new ValidationException(String.format(LocaleMessageUtil.getMessage("key_ca8d4c347ad948a6a4b74d7b098ee2a7"), entry.getType()));
                } catch (JsonProcessingException e) {
                    throw new ValidationException(String.format(LocaleMessageUtil.getMessage("key_3b0632b9891c45ffb4acef5bbd95ada3"), entry.getValue()));
                }
            }
        }

        Method method = MethodUtils.getMatchingMethod(bean.getClass(), methodName, methodParameterTypes);
        if (Objects.isNull(method)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_a524b5b19e674c9d963b8ef976d17851"));
        }
        
        method.setAccessible(true);
        try {
            return method.invoke(bean, methodParameters);
        } catch (IllegalAccessException e) {
            LogExecutor.instance().error(LOGGER, LOG_TAG, "方法不可访问", e);
            throw new ValidationException("方法不可访问");
        } catch (InvocationTargetException e) {
            LogExecutor.instance().error(LOGGER, LOG_TAG, "方法无法调用", e);
            throw new ValidationException("方法无法调用");
        }
    }
}
