package com.persagy.apccontrol.basic.model.param.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 需求管理-照明需求-楼层关联照明回路查询参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
@Data
@Schema(title = "需求管理-照明需求-楼层关联照明回路查询参数")
public class DemandLightingFloorWithCircuitsQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模版ID")
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    @Schema(description = "场景ID")
    @NotNull(message = "{key_59537c1a56194bd6ae31797f5dcad6ad}")
    private Long sceneId;

    @Schema(description = "楼层ID, 为空时查询所有")
    private Set<String> floorIds;

    @Schema(description = "照明回路ID, 为空时查询所有")
    private Set<String> circuitIds;

}
