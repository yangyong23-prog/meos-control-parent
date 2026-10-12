package com.persagy.apccontrol.basic.model.vo.intervention;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.demand.SceneTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 描述
 *
 * @author yuecaipu
 * @date 2024/7/23 16:12
 * @since v5.240402.00
 */
@Data
public class InterventionQueryParam {

    /**
     * 配置ID，自动需求场景ID或干预配置ID
     */
    private Long configId;
    /**
     * 实例id
     */
    @NotNull
    private String instanceId;

    /**
     * 实例类型
     */
    @NotNull
    private DemandInstanceTypeEnum instanceType;
    /**
     * 查询类型
     */
    @NotNull
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 需求场景类型
     */
    private SceneTypeEnum sceneType;
}
