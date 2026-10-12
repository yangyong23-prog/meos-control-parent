package com.persagy.apccontrol.basic.model.param.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 照明需求管理-操作-移除某卡片配置
 *
 * @author yuyao
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-操作-移除某卡片配置")
public class DemandLightingRemoveCardParam extends DemandLightingInstanceQueryParam {

    @Schema(description = "配置实例ID")
    @NotBlank(message = "{key_b7007ce071d84cf8b12eb4a9c3edd863}")
    private String configId;

    @Schema(description = "引用状态（0-自身存在配置，1-引用上级配置）")
    @NotNull(message = "{key_f11f280b2fff4350bc0de17476356a4d}")
    private String referenceStatus;

    @Schema(description = "是否删除反馈调节配置")
    private Boolean delSpaceFeedbackAdjust;


}
