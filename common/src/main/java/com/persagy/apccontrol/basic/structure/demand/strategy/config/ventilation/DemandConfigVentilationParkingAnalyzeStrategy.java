package com.persagy.apccontrol.basic.structure.demand.strategy.config.ventilation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.ventilation.DemandVentilationProperties;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationParkingValueObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.function.Supplier;

/**
 * 通风需求-停车场通风解析策略
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Component(DemandConfigVentilationParkingAnalyzeStrategy.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigVentilationParkingAnalyzeStrategy
        extends AbstractDemandVentilationConfigAnalyzeStrategy<DemandConfigVentilationParkingValueObject> {

    public static final String BEAN_NAME = "demandConfigVentilationParkingAnalyzeStrategy";

    private final DemandVentilationProperties properties;

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.PARKING_VENTILATION;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.FIELD;
    }

    @Override
    public Supplier<DemandConfigVentilationParkingValueObject> emptyConfig() {
        return DemandConfigVentilationParkingValueObject::new;
    }

    @Override
    public Supplier<DemandConfigVentilationParkingValueObject> defaultConfig(
            @NonNull DemandInstanceTypeEnum instanceType) {
        return instanceType == DemandInstanceTypeEnum.PROJECT
                ? () -> properties.getDefaultSetting().getParking()
                : emptyConfig();
    }

    @Override
    protected TypeReference<DemandConfigValueObject<DemandConfigVentilationParkingValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigVentilationParkingValueObject>>() {
        };
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-通风-停车场";
    }
}
