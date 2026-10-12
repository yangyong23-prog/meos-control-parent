package com.persagy.apccontrol.basic.structure.demand.strategy.config.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 需求配置解析策略-照明需求-景观
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Component(DemandConfigLightingLandscapeAnalyzeStrategy.BEAN_NAME)
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigLightingLandscapeAnalyzeStrategy extends AbstractDemandConfigLightingAnalyzeStrategy {

    public static final String BEAN_NAME = "demandConfigLightingLandscapeAnalyzeStrategy";

    @Override
    public DemandTemplateTypeEnum templateType() {
        return DemandTemplateTypeEnum.LANDSCAPE_LIGHTING;
    }

    @Override
    public String logTag() {
        return "需求配置解析策略-照明需求-景观";
    }
}
