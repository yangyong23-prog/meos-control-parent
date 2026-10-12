package com.persagy.apccontrol.basic.structure.demand.repository.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 根据实例批量查询需求配置参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigByInstanceRepositoryBatchQueryParam extends BaseRepositoryQueryParam<DemandConfig> {

    /**
     * 配置ID
     */
    private Set<Long> configIds;

    /**
     * 模版ID
     */
    private Long templateId;

    /**
     * 模版ID
     */
    private Collection<Long> templateIds;

    /**
     * 场景ID, 存在场景时必传
     */
    private Long sceneId;

    /**
     * 场景ID, 存在场景时必传
     */
    private Set<Long> sceneIds;

    /**
     * 配置组ID
     */
    private Set<Long> configGroupIds;

    /**
     * 实例类型 -> 实例ID, 为空时查询所有
     *
     * @see #instanceTypes
     * @see #instanceIds
     */
    @Deprecated
    private Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds;

    /**
     * 实例类型
     */
    private Set<String> instanceTypes;

    /**
     * 实例ID
     */
    private Set<String> instanceIds;

    /**
     * 配置数据模糊匹配
     */
    private Object likeConfigData;

    // ------------------------- 拓展字段 -------------------------

    /**
     * 模版类型
     */
    private DemandTemplateTypeEnum templateType;

}
