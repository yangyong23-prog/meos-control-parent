package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 需求模版日历新增参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandTemplateCalendarSaveDaoParam {

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
     * 日期
     */
    @NotNull(message = "{key_fa04a8c6c1bf466eb39ef6a1e3b2d870}")
    private LocalDate workDate;

    /**
     * 是否自定义配置
     */
    @NotNull(message = "{key_7b8125d87f184222859b6e6ee2f1ba2a}")
    private Boolean isCustomizedSetting;

    /**
     * 关联需求模版ID
     */
    @NotNull(message = "{key_8c0f8d5f23034cb09375574633685fb9}")
    private Long relDemandTemplateId;

    /**
     * 状态
     */
    @NotNull(message = "{key_31b1c9f4e49941dd933a4f97efcd1a5a}")
    private StateTypeEnum st = StateTypeEnum.VALID;

}
