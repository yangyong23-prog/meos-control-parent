package com.persagy.apccontrol.basic.model.param.demand.lighting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 需求管理-照明需求-检查场景名称参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-照明需求-检查场景名称参数")
public class DemandLightingSceneNameCheckParam {

    /**
     * 模版名称允许长度
     */
    public static final int SCENE_NAME_ALLOW_LENGTH = 20;

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "场景名称")
    @NotBlank(message = "{key_364c43d7367446ba9e8ff60070ec270d}")
    @Length(max = SCENE_NAME_ALLOW_LENGTH, message = "{key_0ca407a3a96543a4831314fe352ee292}")
    private String sceneName;

    @Schema(description = "场景ID, 存在时将过滤该ID记录")
    private Long sceneId;

    @Schema(description = "是否抛出异常")
    private Boolean isThrows = false;

}
