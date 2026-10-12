package com.persagy.apccontrol.basic.structure.common.model.context;

import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.common.service.param.InheritConfigHandlerParam;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 继承配置上下文
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
public class InheritConfigContext<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InheritConfigContext.class);
    private static final String LOG_TAG = "继承配置上下文";
    private static final Map<Class<?>, List<PropertyDescriptor>> CLASS_AND_FIELD_DESCRIPTOR = new ConcurrentHashMap<>();

    // ------------------------- 入参 -------------------------

    /**
     * 参数
     */
    private final InheritConfigHandlerParam<T> param;

    // ------------------------- 过程 -------------------------

    // ------------------------- 结果 -------------------------

    /**
     * 最终配置
     */
    @Nullable
    private T finalSetting;

    /**
     * 继承对象实例
     */
    private List<DemandInstanceValueObject> inheritInstances = new ArrayList<>();

    /**
     * 继承字段 -> 字段所属实例
     */
    private Map<String, DemandInstanceValueObject> inheritFieldAndInstance = new HashMap<>();

    /**
     * 构造器
     *
     * @param param 参数
     * @since 5.240702.0
     */
    public InheritConfigContext(@NonNull InheritConfigHandlerParam<T> param) {
        InheritConfigTypeEnum inheritConfigType = param.getInheritSettingType();
        Supplier<T> createSettingSupplier = param.getCreateSettingSupplier();
        T selfSetting = param.getSelfSetting();
        LinkedHashMap<DemandInstanceValueObject, T> parentInstanceAndSetting = param.getParentInstanceAndSetting();

        this.param = param;
        // 当不存在上级配置时, 最终配置为自身
        if (MapUtils.isEmpty(parentInstanceAndSetting)) {
            finalSetting = selfSetting;
            return;
        }
        switch (inheritConfigType) {
            case OBJECT:
                for (Map.Entry<DemandInstanceValueObject, T> entry : parentInstanceAndSetting.entrySet()) {
                    inheritInstances.add(entry.getKey());
                    this.finalSetting = entry.getValue();
                    break;
                }
                break;
            case FIELD:
                if (allFieldValueNonNull(selfSetting)) {
                    this.finalSetting = selfSetting;
                    return;
                }
                T newSetting = copySetting(selfSetting, createSettingSupplier);
                for (Map.Entry<DemandInstanceValueObject, T> parent : parentInstanceAndSetting.entrySet()) {
                    DemandInstanceValueObject parentInstance = parent.getKey();
                    T parentInstanceSetting = parent.getValue();

                    // 填充所有非null字段值
                    List<String> setFieldNames = fillAllFieldValueIfNull(newSetting, parentInstanceSetting);
                    if (CollectionUtils.isNotEmpty(setFieldNames)) {
                        inheritInstances.add(parentInstance);
                        setFieldNames.forEach(item -> inheritFieldAndInstance.put(item, parentInstance));
                    }
                    // 当所有字段都不为null时结束
                    if (allFieldValueNonNull(newSetting)) {
                        break;
                    }
                }
                this.finalSetting = newSetting;
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(
                        inheritConfigType.getClass().getSimpleName(), inheritConfigType);
        }
    }

    // ------------------------- 行为 -------------------------

    /**
     * 复制配置
     *
     * @param setting               原始配置
     * @param createSettingSupplier 创建配置函数式接口
     * @return 复制后的配置
     * @since 5.240702.0
     */
    private T copySetting(@Nullable T setting, @NonNull Supplier<T> createSettingSupplier) {
        T newSetting = createSettingSupplier.get();
        if (Objects.isNull(setting)) {
            return newSetting;
        }
        fillAllFieldValueIfNull(newSetting, setting);
        return newSetting;
    }

    /**
     * 为 {@code null} 填充所有字段值
     *
     * @param target 目标源
     * @param source 数据源
     * @return 被设置的字段名称
     * @since 5.240702.0
     */
    private List<String> fillAllFieldValueIfNull(@NonNull T target, @NonNull T source) {
        List<String> setFieldNames = new ArrayList<>();
        List<PropertyDescriptor> fieldDescriptors = cacheFieldDescriptors(target.getClass());
        for (PropertyDescriptor fieldDescriptor : fieldDescriptors) {
            try {
                Object targetValue = fieldDescriptor.getReadMethod().invoke(target);
                if (Objects.isNull(targetValue)) {
                    Object originValue = fieldDescriptor.getReadMethod().invoke(source);
                    fieldDescriptor.getWriteMethod().invoke(target, originValue);
                    setFieldNames.add(fieldDescriptor.getName());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "写入配置值失败", e, Arrays.asList(
                        Pair.of("fieldName", fieldDescriptor.getName()),
                        Pair.of("source", source),
                        Pair.of("target", target)
                ));
                throw new RuntimeException(e);
            }
        }
        return setFieldNames;
    }

    /**
     * 是否所有字段均存不为 {@code null}
     *
     * @param setting 配置
     * @return 是否所有字段均存不为 {@code null}
     * @since 5.240702.0
     */
    private boolean allFieldValueNonNull(@Nullable T setting) {
        if (Objects.isNull(setting)) {
            return false;
        }
        List<PropertyDescriptor> fieldDescriptors = cacheFieldDescriptors(setting.getClass());
        for (PropertyDescriptor fieldDescriptor : fieldDescriptors) {
            Method readMethod = fieldDescriptor.getReadMethod();
            try {
                Object fieldValue = readMethod.invoke(setting);
                if (Objects.isNull(fieldValue)) {
                    return false;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "读取配置值失败", e, Pair.of("setting", setting));
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    /**
     * 缓存字段访问器
     *
     * @param cls 类
     * @return 字段访问器
     * @since 5.240702.0
     */
    private List<PropertyDescriptor> cacheFieldDescriptors(@NonNull Class<?> cls) {
        List<PropertyDescriptor> fieldDescriptors = CLASS_AND_FIELD_DESCRIPTOR.get(cls);
        if (Objects.nonNull(fieldDescriptors)) {
            return fieldDescriptors;
        }
        fieldDescriptors = new ArrayList<>();
        List<Field> fields = FieldUtils.getAllFieldsList(cls);
        for (Field field : fields) {
            try {
                fieldDescriptors.add(new PropertyDescriptor(field.getName(), cls));
            } catch (IntrospectionException e) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "解析配置字段失败", e, Pair.of("class", cls));
                throw new RuntimeException(e);
            }
        }
        CLASS_AND_FIELD_DESCRIPTOR.put(cls, fieldDescriptors);
        return fieldDescriptors;
    }
}
