package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandConfigClearTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 需求引用操作请求参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "需求引用操作请求参数")
public class DemandClearParam extends DemandBasicsOperateParam {

    //操作参数
    @Schema(description = "清除范围")
    @NotNull(message = "{key_e6acd5c8b46f405ea18efba2be8f4320}")
    private DemandConfigClearTypeEnum clearRange;

}
