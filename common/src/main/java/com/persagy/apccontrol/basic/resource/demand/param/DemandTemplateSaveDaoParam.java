package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求模版保存参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandTemplateSaveDaoParam {

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
     * 需求模版标签
     */
    private String demandTemplateTag;

    /**
     * 需求模版名
     */
    @NotBlank(message = "{key_1b98bffdece04905a3964064f2b6c54b}")
    private String demandTemplateName;

    /**
     * 是否为预置模版
     */
    private Boolean isInitialized = false;

    /**
     * 是否启用
     */
    @NotNull(message = "{key_3c520583bf9643059a43db8c6a8b4456}")
    private Boolean isEnabled;

    /**
     * 状态
     */
    @NotNull(message = "{key_31b1c9f4e49941dd933a4f97efcd1a5a}")
    private StateTypeEnum st = StateTypeEnum.VALID;

}
