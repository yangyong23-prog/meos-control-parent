package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求实例新增参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandInstanceConfigSaveDaoParam {

    /**
     * 需求类型
     */
    @NotNull(message = "{key_35d947c009e1424b9bad13655e23026d}")
    private DemandTypeEnum demandType;

    /**
     * 需求模版类型
     */
    @NotNull(message = "{key_67d92d2b08624257a2b1303cf198a074}")
    private DemandTemplateTypeEnum demandTemplateType;

    /**
     * 需求实例类型
     */
    @NotNull(message = "{key_bef82d36a29642ba8174459b23bd0ccd}")
    private DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例ID
     */
    @NotBlank(message = "{key_b6a999341d384e8d8e3c837821c85508}")
    private String demandInstanceId;

    /**
     * 配置数据
     */
    @NotNull(message = "{key_198da2c118da4426a857e4a52e9957cb}")
    private DemandInstanceConfigValueObject configData;

    /**
     * 状态
     */
    @NotNull(message = "{key_31b1c9f4e49941dd933a4f97efcd1a5a}")
    private StateTypeEnum st = StateTypeEnum.VALID;

}
