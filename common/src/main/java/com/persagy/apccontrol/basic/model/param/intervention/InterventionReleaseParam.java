package com.persagy.apccontrol.basic.model.param.intervention;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 干预解除参数
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
@Schema(title = "干预解除参数")
public class InterventionReleaseParam {

    @Schema(description = "干预ID")
    @NotNull(message = "{key_ed218a2910884e4782fab5b05ac6a635}")
    private Long interventionId;

}
