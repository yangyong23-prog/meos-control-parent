package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.base.domain.repository.BaseRepositoryQueryParam;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Objects;

/**
 * 需求模版查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DemandTemplateQueryDaoParam extends BaseRepositoryQueryParam<DemandTemplate> {

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
     * 需求模版标签
     */
    private String demandTemplateTag;

    /**
     * 需求模版标签
     */
    private Collection<String> demandTemplateTags;

    /**
     * 是否为预置模版
     */
    private Boolean isInitialized;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 状态
     */
    private StateTypeEnum st = StateTypeEnum.VALID;

    /**
     * 设置需求模版类型
     *
     * @param demandTemplateType 需求模版类型
     * @return 当前实例
     * @since v6.0.0
     */
    public DemandTemplateQueryDaoParam setDemandTemplateType(DemandTemplateTypeEnum demandTemplateType) {
        this.demandTemplateType = demandTemplateType;
        if (Objects.nonNull(demandTemplateType)) {
            this.demandType = demandTemplateType.toDemandTypeEnum();
        }
        return this;
    }
}
