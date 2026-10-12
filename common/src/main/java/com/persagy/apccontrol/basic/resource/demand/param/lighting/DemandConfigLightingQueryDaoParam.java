package com.persagy.apccontrol.basic.resource.demand.param.lighting;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * 需求配置-照明需求-查询参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingQueryDaoParam {

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
     * 需求模版类型
     */
    private Collection<DemandTemplateTypeEnum> demandTemplateTypes;

    /**
     * 需求模版标签
     */
    private String demandTemplateTag;

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
     * 是否自定义配置
     */
    private Boolean isCustomizedSetting;

    /**
     * Like配置
     */
    private Object likeConfigData;

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

    /**
     * 状态
     */
    private StateTypeEnum st = StateTypeEnum.VALID;

}
