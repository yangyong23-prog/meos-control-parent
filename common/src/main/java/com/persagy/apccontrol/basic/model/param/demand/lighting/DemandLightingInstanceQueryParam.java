package com.persagy.apccontrol.basic.model.param.demand.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 照明需求管理-实例查询条件
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-实例查询条件")
public class DemandLightingInstanceQueryParam extends DemandLightingTemplateSceneParam {

    @Schema(description = "实例ID")
    @NotBlank(message = "{key_ea24192e2d4649279b763b2b19f83742}")
    private String instanceId;

    @Schema(description = "实例类型")
    @NotNull(message = "{key_1e47d670c73d4ab2803db5dc2965e783}")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "是否需要过滤已配置过的实例")
    private Boolean filterExists;

    @Schema(description = "引用状态（0-自身存在配置，1-引用上级配置）")
    //@NotNull(message = "引用状态，不能为空")
    private String referenceStatus;

}
