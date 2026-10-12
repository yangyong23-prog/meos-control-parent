package com.persagy.apccontrol.basic.resource.demand.param.lighting.scene;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * 需求配置-照明需求-场景结果查询参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingResultQueryDaoParam {

    /**
     * ID
     */
    private Long id;

    /**
     * ID
     */
    private Collection<Long> ids;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 照明回路ID
     */
    private String circuitId;

    /**
     * 照明回路ID
     */
    private Collection<String> circuitIds;


    /**
     * 照明回路配置模式
     */
    private DemandLightingCircuitSettingModeEnum circuitSettingMode;

    /**
     * 关联需求模版ID
     */
    private Long relDemandTemplateId;

    /**
     * 关联需求模版ID
     */
    private Collection<Long> relDemandTemplateIds;

    /**
     * 关联照明场景ID
     */
    private Long relDemandLightingSceneId;

    /**
     * 关联照明场景ID
     */
    private Collection<Long> relDemandLightingSceneIds;
    
    public StateTypeEnum getSt() {
        return StateTypeEnum.VALID;
    }
    
    public void setSt(StateTypeEnum st) {
    
    }
}
