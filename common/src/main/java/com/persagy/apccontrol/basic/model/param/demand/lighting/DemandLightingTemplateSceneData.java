package com.persagy.apccontrol.basic.model.param.demand.lighting;

import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingSceneTimeIntervalDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * 照明需求管理-场景保存参数
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-场景保存参数")
public class DemandLightingTemplateSceneData {

    /**
     * 不传，走新增逻辑
     */
    @Schema(description = "场景ID")
    private Long sceneId;

    @Schema(description = "场景名称")
    @NotBlank(message = "{key_e2cbebc987794936ac63b1109415901b}")
    private String sceneName;

    @Schema(description = "排序")
    private Integer sequence;

    @Valid
    @Schema(description = "开始时间")
    private DemandLightingSceneTimeIntervalDTO beginTime;

    @Valid
    @Schema(description = "结束时间")
    private DemandLightingSceneTimeIntervalDTO endTime;

}
