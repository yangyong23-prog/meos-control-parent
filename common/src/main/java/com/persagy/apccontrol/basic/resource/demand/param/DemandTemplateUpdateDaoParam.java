package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 需求模版保存参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandTemplateUpdateDaoParam {

    /**
     * ID
     */
    @NotNull(message = "{key_3bbfbcb78c1744b5b14eac0557d4c399}")
    private Long id;

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
     * 需求模版名
     */
    private String demandTemplateName;

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
    private StateTypeEnum st;

}
