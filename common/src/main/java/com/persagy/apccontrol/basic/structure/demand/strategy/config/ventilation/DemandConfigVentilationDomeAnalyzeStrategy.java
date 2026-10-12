package com.persagy.apccontrol.basic.structure.demand.strategy.config.ventilation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.config.properties.saas.children.demand.ventilation.DemandVentilationProperties;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationDomeValueObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.function.Supplier;

/**
 * 通风需求-穹顶通风解析策略
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Component(DemandConfigVentilationDomeAnalyzeStrategy.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigVentilationDomeAnalyzeStrategy
        extends AbstractDemandVentilationConfigAnalyzeStrategy<DemandConfigVentilationDomeValueObject> {

    public static final String BEAN_NAME = "demandConfigVentilationDomeAnalyzeStrategy";

    private final DemandVentilationProperties properties;

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.DOME_VENTILATION;
    }

    @Override
    public InheritConfigTypeEnum inheritConfigType() {
        return InheritConfigTypeEnum.FIELD;
    }

    @Override
    public Supplier<DemandConfigVentilationDomeValueObject> emptyConfig() {
        return DemandConfigVentilationDomeValueObject::new;
    }

    @Override
    public Supplier<DemandConfigVentilationDomeValueObject> defaultConfig(
            @NonNull DemandInstanceTypeEnum instanceType) {
        return instanceType == DemandInstanceTypeEnum.PROJECT
                ? () -> properties.getDefaultSetting().getDome()
                : emptyConfig();
    }

    @Override
    protected TypeReference<DemandConfigValueObject<DemandConfigVentilationDomeValueObject>> typeReference() {
        return new TypeReference<DemandConfigValueObject<DemandConfigVentilationDomeValueObject>>() {
        };
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-通风-穹顶";
    }
}
