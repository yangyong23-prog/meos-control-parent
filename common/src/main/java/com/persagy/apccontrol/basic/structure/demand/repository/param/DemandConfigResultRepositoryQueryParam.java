package com.persagy.apccontrol.basic.structure.demand.repository.param;

import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 需求配置预计算结果查询参数
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigResultRepositoryQueryParam extends BaseRepositoryQueryParam<DemandConfigResult> {

    /**
     * ID
     */
    private @Nullable Long id;

    /**
     * ID集合
     */
    private @Nullable Collection<Long> ids;

    /**
     * 需求模板类型
     */
    private @Nullable DemandTemplateTypeEnum demandTemplateType;

    /**
     * 需求模板类型集合
     */
    private @Nullable Collection<DemandTemplateTypeEnum> demandTemplateTypes;

    /**
     * 需求实例类型
     */
    private @Nullable DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例类型集合
     */
    private @Nullable Collection<DemandInstanceTypeEnum> demandInstanceTypes;

    /**
     * 需求实例ID
     */
    private @Nullable String demandInstanceId;

    /**
     * 需求实例ID集合
     */
    private @Nullable Collection<String> demandInstanceIds;

    /**
     * 实例类型 -> 实例ID, 实例ID为空时查询该类型全部结果
     */
    private @Nullable Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndInstanceIds;

    /**
     * 关联需求模板ID
     */
    private @Nullable Long relTemplateId;

    /**
     * 关联需求模板ID集合
     */
    private @Nullable Collection<Long> relTemplateIds;

    /**
     * 关联需求场景ID
     */
    private @Nullable Long relSceneId;

    /**
     * 关联需求场景ID集合
     */
    private @Nullable Collection<Long> relSceneIds;

    /**
     * 是否查询空场景ID
     */
    private @Nullable Boolean relSceneIdIsNull;

    /**
     * 是否屏蔽空间
     */
    private @Nullable Boolean isShielded;

    /**
     * 业务键集合
     */
    private @Nullable Collection<DemandConfigResultBusinessKey> businessKeys;

}
