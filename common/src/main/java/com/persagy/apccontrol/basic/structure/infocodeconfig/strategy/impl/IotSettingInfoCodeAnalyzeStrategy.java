package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import org.springframework.stereotype.Component;

/**
 * 设备IOT设定信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component(IotSettingInfoCodeAnalyzeStrategy.BEAN_NAME)
public class IotSettingInfoCodeAnalyzeStrategy
        extends BaseIotInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentIotSettingInfoCodeAnalyzeStrategy";

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.IOT_RUNNING;
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-IOT设定信息点";
    }
}
