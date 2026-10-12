package com.persagy.apccontrol.basic.structure.common.service.param;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * 继承配置参数
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
public class InheritConfigHandlerParam<T> {

    /**
     * 继承类型
     */
    @NotNull(message = "{key_f378f86930f140d99bcd21c1f4e81e04}")
    private InheritConfigTypeEnum inheritSettingType;

    /**
     * 创建新配置函数式接口
     */
    @NotNull(message = "{key_d3a081182afd4fe7a1229c0ce0072402}")
    private Supplier<T> createSettingSupplier;

    /**
     * 自身配置
     */
    private T selfSetting;

    /**
     * 上级实例 -> 上级实例配置
     */
    private LinkedHashMap<DemandInstanceValueObject, T> parentInstanceAndSetting;

}
