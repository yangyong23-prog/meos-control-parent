package com.persagy.apccontrol.basic.structure.intervention.model.valueobject;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.List;

/**
 * 干预配置值对象
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
public class InterventionConfigValueObject<T extends InterventionConfigData> {

    /**
     * 自身配置
     */
    private T selfConfig;

    /**
     * 最终配置
     */
    private T finalConfig;

    /**
     * 继承实例, 从下往上继承
     */
    private List<DemandInstanceValueObject> inheritInstances = Collections.emptyList();

    /**
     * 获取最终配置
     *
     * @return 最终配置, 最终配置不存在时使用自身配置
     * @since 5.240801.0
     */
    public T getFinalConfig() {
        return ObjectUtils.isNotEmpty(finalConfig) ? finalConfig : selfConfig;
    }
}
