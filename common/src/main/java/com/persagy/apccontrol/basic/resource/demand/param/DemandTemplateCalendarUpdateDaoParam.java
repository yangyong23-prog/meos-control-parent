package com.persagy.apccontrol.basic.resource.demand.param;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 需求模版日历编辑参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
public class DemandTemplateCalendarUpdateDaoParam {

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
     * 日期
     */
    private LocalDate workDate;

    /**
     * 是否自定义配置
     */
    private Boolean isCustomizedSetting;

    /**
     * 关联需求模版ID
     */
    private Long relDemandTemplateId;

    /**
     * 状态
     */
    private StateTypeEnum st;

}
