package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collection;

/**
 * 需求实例配置查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DemandInstanceConfigQueryDaoParam extends BaseRepositoryQueryParam<DemandInstanceConfig> {

    /**
     * ID
     */
    private Long id;

    /**
     * ID
     */
    private Collection<Long> ids;

    /**
     * 需求类型
     */
    private DemandTypeEnum demandType;

    /**
     * 需求模版类型
     */
    private DemandTemplateTypeEnum demandTemplateType;

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
     * 是否被屏蔽
     */
    private Boolean shielded;

    /**
     * 是否在黑名单中, 仅空间可配置
     */
    private Boolean inBlacklisted;

    /**
     * 状态
     */
    private StateTypeEnum st = StateTypeEnum.VALID;

}
