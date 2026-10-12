package com.persagy.apccontrol.basic.model.dto.demand.lighting;

import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingSettingObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingSettingScopeTypeEnum;
import lombok.Data;

/**
 * 需求管理-照明结果DTO
 *
 * @author yuecaipu
 * @date 2024/3/29 17:35
 * @since 24Q1 T3S2-5.18.0
 */
@Data
public class DemandConfigLightingResultDTO extends DemandConfigLightingResult {


    /**
     * 配置来源
     */
    //private DemandInstanceTypeEnum configSource;

    /**
     * 配置对象类型
     */
    private DemandLightingSettingObjectTypeEnum settingObject;

    /**
     * 设置类型
     */
    private DemandLightingSettingScopeTypeEnum scopeType;




}
