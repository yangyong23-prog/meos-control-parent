package com.persagy.apccontrol.basic.model.param.demand.scene;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class DemandSceneParam {

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
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private DemandTimeValueObject beginTime;

    @Valid
    @Schema(description = "结束时间")
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private DemandTimeValueObject endTime;

}
