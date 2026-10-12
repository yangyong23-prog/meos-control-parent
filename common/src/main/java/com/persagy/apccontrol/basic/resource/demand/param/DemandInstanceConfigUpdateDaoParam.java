package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 需求实例配置更新参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandInstanceConfigUpdateDaoParam {

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
     * 需求实例类型
     */
    private DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例ID
     */
    private String demandInstanceId;

    /**
     * 配置数据
     */
    @NotNull(message = "{key_198da2c118da4426a857e4a52e9957cb}")
    private DemandInstanceConfigValueObject configData;

    /**
     * 状态
     */
    private StateTypeEnum st;

}
