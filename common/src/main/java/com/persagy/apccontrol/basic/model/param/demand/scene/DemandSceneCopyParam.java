package com.persagy.apccontrol.basic.model.param.demand.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 照明需求管理-场景复制参数
 *
 * @author wxf
 * @since 7.250601.0
 */
@Getter
@Setter
@ToString(callSuper = true)
@Schema(title = "照明需求管理-场景复制参数")
public class DemandSceneCopyParam {

    @Schema(description = "场景来源ID")
    @NotNull(message = "{key_046d5c42f8014544ac70ea5a305ed9e9}")
    private Long fromSceneId;

    @Schema(description = "场景ID")
    @NotNull(message = "{key_7fe82377eb104dfa8424cffc012e5006}")
    private Long sceneId;

}
