package com.persagy.apccontrol.basic.resource.demand.param.lighting.scene;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * 需求配置-照明需求-场景查询参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingSceneQueryDaoParam {

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
     * 需求类型
     */
    private DemandTypeEnum demandType;

    /**
     * 需求模版类型
     */
    private DemandTemplateTypeEnum demandTemplateType;

    /**
     * 需求模版标签
     */
    private String demandTemplateTag;

    /**
     * 关联需求模版ID
     */
    private Long relDemandTemplateId;

    /**
     * 关联需求模版ID
     */
    private Collection<Long> relDemandTemplateIds;

    /**
     * 状态
     */
    private StateTypeEnum st = StateTypeEnum.VALID;

}
