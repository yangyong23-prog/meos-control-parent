package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import org.springframework.stereotype.Component;

/**
 * 设备IOT运行信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Component(IotRunningInfoCodeAnalyzeStrategy.BEAN_NAME)
public class IotRunningInfoCodeAnalyzeStrategy
        extends BaseIotInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "equipmentIotRunningInfoCodeAnalyzeStrategy";

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.IOT_SETTING;
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-IOT运行信息点";
    }
}
