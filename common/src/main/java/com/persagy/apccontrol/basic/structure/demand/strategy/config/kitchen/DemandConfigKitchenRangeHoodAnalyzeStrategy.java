package com.persagy.apccontrol.basic.structure.demand.strategy.config.kitchen;

import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.kitchen.DemandConfigKitchenRangeHoodValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.AbstractDemandConfigAnalyzeStrategy;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.function.Supplier;

/**
 * 厨房排油烟需求-配置解析策略
 *
 * @author JiYinchuan
 * @since v7.261001.0
 */
@Component(DemandConfigKitchenRangeHoodAnalyzeStrategy.BEAN_NAME)
@Validated
public class DemandConfigKitchenRangeHoodAnalyzeStrategy
        extends AbstractDemandConfigAnalyzeStrategy<DemandConfigKitchenRangeHoodValueObject> {

    /**
     * 策略Bean名称
     *
     * @since v7.261001.0
     */
    public static final String BEAN_NAME = "demandConfigKitchenRangeHoodAnalyzeStrategy";

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.KITCHEN_RANGE_HOOD;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.FIELD;
    }

    @Override
    public Supplier<DemandConfigKitchenRangeHoodValueObject> emptyConfig() {
        return DemandConfigKitchenRangeHoodValueObject::new;
    }

    @Override
    public Supplier<DemandConfigKitchenRangeHoodValueObject> defaultConfig(
            @NonNull DemandInstanceTypeEnum instanceType) {
        return this.emptyConfig();
    }

    @Override
    protected TypeReference<DemandConfigValueObject<DemandConfigKitchenRangeHoodValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigKitchenRangeHoodValueObject>>() {
        };
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-厨房排油烟";
    }
}
