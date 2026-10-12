package com.persagy.apccontrol.basic.structure.demand.model;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.utils.ValidateUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 需求配置值对象
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
public class DemandConfigValueObject<T extends BaseDemandConfig> implements BaseCheckParamProvider {

    /**
     * 全局配置
     */
    private T globalConfig;

    /**
     * 空间功能类型配置
     */
    private List<DemandSpaceFuncTypeConfig<T>> spaceFuncTypeConfigs;

    /**
     * 是否所有字段均存在值
     *
     * @return 是否所有字段均存在值
     * @since 5.240902.0
     */
    @SneakyThrows
    public boolean allFieldHasValue() {
        if (ObjectUtils.anyNull(globalConfig, spaceFuncTypeConfigs)) {
            return false;
        }
        if (Objects.nonNull(globalConfig)) {
            List<String> fieldNames = globalConfig.cacheFieldNames();
            for (String fieldName : fieldNames) {
                Object fieldValue = globalConfig.cachePropertyDescriptor(fieldName).getReadMethod().invoke(globalConfig);
                if (ObjectUtils.isEmpty(fieldValue)) {
                    return false;
                }
            }
        }
        if (Objects.nonNull(spaceFuncTypeConfigs)) {
            for (DemandSpaceFuncTypeConfig<T> spaceFuncTypeConfig : spaceFuncTypeConfigs) {
                T config = spaceFuncTypeConfig.getConfig();
                List<String> fieldNames = config.cacheFieldNames();
                for (String fieldName : fieldNames) {
                    Object fieldValue = config.cachePropertyDescriptor(fieldName).getReadMethod().invoke(config);
                    if (ObjectUtils.isEmpty(fieldValue)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 配置是否不为空
     *
     * @return 配置是否不为空
     * @since 5.240902.0
     */
    public boolean notEmpty() {
        return ObjectUtils.anyNotNull(globalConfig, spaceFuncTypeConfigs);
    }

    /**
     * 过滤空间功能类型配置中指定分组ID
     *
     * @param relGroupIds 关联分组ID
     * @since 7.250202.0
     */
    public void filterSpaceFuncTypeRelGroupId(@NonNull Set<Long> relGroupIds) {
        if (CollectionUtils.isEmpty(relGroupIds) || CollectionUtils.isEmpty(this.spaceFuncTypeConfigs)) {
            return;
        }
        this.spaceFuncTypeConfigs.removeIf(spaceFuncTypeConfig -> !relGroupIds.contains(spaceFuncTypeConfig.getRelConfigGroupId()));
    }

    /**
     * 创建默认配置值对象
     *
     * @param globalConfig         全局配置
     * @param spaceFuncTypeConfigs 空间功能类型配置
     * @param <T>                  配置类型
     * @return 默认配置值对象
     * @since 5.241001.0
     */
    public static <T extends BaseDemandConfig> DemandConfigValueObject<T> ofDefault(
            @Nullable T globalConfig,
            @Nullable List<DemandSpaceFuncTypeConfig<T>> spaceFuncTypeConfigs) {
        DemandConfigValueObject<T> valueObject = new DemandConfigValueObject<>();
        valueObject.setGlobalConfig(globalConfig);
        valueObject.setSpaceFuncTypeConfigs(spaceFuncTypeConfigs);
        return valueObject;
    }

    @Override
    public void checkAndSet() {
        if (Objects.nonNull(this.globalConfig)) {
            ValidateUtils.validate(this.globalConfig);
            this.globalConfig.checkAndSet();
        }
        if (CollectionUtils.isNotEmpty(this.spaceFuncTypeConfigs)) {
            for (DemandSpaceFuncTypeConfig<T> spaceFuncTypeConfig : this.spaceFuncTypeConfigs) {
                ValidateUtils.validate(spaceFuncTypeConfig);
                spaceFuncTypeConfig.checkAndSet();
            }
        }
    }
}
