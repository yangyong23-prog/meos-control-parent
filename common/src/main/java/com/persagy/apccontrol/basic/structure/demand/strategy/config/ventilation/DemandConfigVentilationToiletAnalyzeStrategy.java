package com.persagy.apccontrol.basic.structure.demand.strategy.config.ventilation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.ventilation.DemandVentilationProperties;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationToiletValueObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.function.Supplier;

/**
 * 通风需求-卫生间通风解析策略
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Component(DemandConfigVentilationToiletAnalyzeStrategy.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigVentilationToiletAnalyzeStrategy
        extends AbstractDemandVentilationConfigAnalyzeStrategy<DemandConfigVentilationToiletValueObject> {

    public static final String BEAN_NAME = "demandConfigVentilationToiletAnalyzeStrategy";

    private final DemandVentilationProperties properties;

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.TOILET_VENTILATION;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.FIELD;
    }

    @Override
    public Supplier<DemandConfigVentilationToiletValueObject> emptyConfig() {
        return DemandConfigVentilationToiletValueObject::new;
    }

    @Override
    public Supplier<DemandConfigVentilationToiletValueObject> defaultConfig(
            @NonNull DemandInstanceTypeEnum instanceType) {
        return instanceType == DemandInstanceTypeEnum.PROJECT
                ? () -> properties.getDefaultSetting().getToilet()
                : emptyConfig();
    }

    @Override
    protected TypeReference<DemandConfigValueObject<DemandConfigVentilationToiletValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigVentilationToiletValueObject>>() {
        };
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-通风-卫生间";
    }
}
