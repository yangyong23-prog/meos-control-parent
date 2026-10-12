package com.persagy.apccontrol.basic.model.param.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingClearRangeTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 照明需求管理-操作-清除配置参数
 *
 * @author yuyao
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-操作-清除配置")
public class DemandLightingFromClearParam extends DemandLightingInstanceQueryParam {

    @Schema(description = "清除范围")
    @NotNull(message = "{key_e6acd5c8b46f405ea18efba2be8f4320}")
    private DemandLightingClearRangeTypeEnum clearRange;

    @Schema(description = "是否物理清除范围")
    //@NotNull(message = "是否物理清除范围")
    private Boolean removeConfig;

}
