package com.persagy.apccontrol.basic.resource.demand.param.lighting;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * 需求配置-照明需求-删除参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingRemoveDaoParam implements BaseCheckParamProvider {

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
     * 需求实例类型
     */
    private DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例类型
     */
    private Collection<DemandInstanceTypeEnum> demandInstanceTypes;

    /**
     * 需求实例ID
     */
    private String demandInstanceId;

    /**
     * 需求实例ID
     */
    private Collection<String> demandInstanceIds;

    /**
     * 关联需求模版ID
     */
    private Long relDemandTemplateId;

    /**
     * 关联照明场景ID
     */
    private Long relDemandLightingSceneId;

    /**
     * 状态
     */
    private StateTypeEnum st;

}
