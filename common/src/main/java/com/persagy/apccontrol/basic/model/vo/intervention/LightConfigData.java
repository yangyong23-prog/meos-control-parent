package com.persagy.apccontrol.basic.model.vo.intervention;

import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandConfigLightingGlobalConfigVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandConfigLightingSpaceConfigVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 照明干预配置
 *
 * @author yuecaipu
 * @date 2024/7/23 16:16
 * @since v5.240402.00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LightConfigData implements ConfigData {

    /**
     * 全局配置
     */
    private DemandConfigLightingGlobalConfigVO global;

    /**
     * 空间类型配置
     */
    private DemandConfigLightingSpaceConfigVO space;
}
