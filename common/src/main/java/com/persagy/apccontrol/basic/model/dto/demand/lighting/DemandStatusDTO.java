package com.persagy.apccontrol.basic.model.dto.demand.lighting;

import com.persagy.apccontrol.basic.model.enumtype.DemandSourceEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingRunningDeviationTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对象需求状态
 *
 * @author yuecaipu
 * @date 2024/12/6 17:22
 * @since v5.240402.00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemandStatusDTO {

    /**
     * 对象Id
     */
    private String objectId;

    /**
     * 回路配置来源
     */
    private DemandSourceEnum demandSource;

    /**
     * 回路配置模式
     */
    private DemandLightingCircuitSettingModeEnum settingMode;

    /**
     * 运行偏差类型 为空时表示无偏差
     */
    private LightingRunningDeviationTypeEnum runningDeviationType;

    public DemandStatusDTO(String objectId, DemandLightingCircuitSettingModeEnum settingMode) {
        this.objectId = objectId;
        this.settingMode = settingMode;
    }
}
