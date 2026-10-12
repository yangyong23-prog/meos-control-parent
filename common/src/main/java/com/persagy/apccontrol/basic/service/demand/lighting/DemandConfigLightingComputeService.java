package com.persagy.apccontrol.basic.service.demand.lighting;

import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingResultDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

/**
 * 照明配置结果计算
 *
 * @author yuecaipu
 * @version 1.0
 * @date 2024/3/27 14:37
 */
public interface DemandConfigLightingComputeService {



    /**
     * 查询当前时间点照明回路ID和需求干预配置结果映射
     *
     * @param projectId                项目ID
     * @param runningMonitorSecondType 运行监测二级类型枚举
     * @return {@link Map<String,DemandLightingCircuitSettingModeEnum>}
     * @author yuecaipu
     * @date 2024/9/3 16:23
     * @since v5.240901
     */
    @Deprecated
    //目前只有室外照明使用后面去掉
    Map<String, DemandLightingCircuitSettingModeEnum> mapCircuitIdAndSettingMode(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType);

    /**
     * 根据时间点计算需求干预配置结果
     *
     * @param projectId                项目ID
     * @param runningMonitorSecondType 运行监测二级类型枚举
     * @param localDateTime            干预时间
     * @return {@link Collection<DemandConfigLightingResultDTO>}
     * @author yuecaipu
     * @date 2024/9/6 17:00
     * @since v5.240901
     */
    @Deprecated
    //目前只有室外照明使用后面去掉
    Collection<DemandConfigLightingResultDTO> interventionResultCompute(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType, LocalDateTime localDateTime);
}
