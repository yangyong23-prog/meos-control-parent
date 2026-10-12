package com.persagy.apccontrol.basic.model.param.airterminal;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 末端历史回放
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/9/18 14:39
 */
@Getter
@Setter
@ToString
public class AirTerminalHistoryPlaybackParam {

    @Schema(description = "项目编码")
    //@NotBlank(message = "项目ID不可为空")
    private String projectId;

    @Schema(description = "实例ID")
    @NotBlank(message = "{key_0a46a15af9454051b2964100753715aa}")
    private String instanceId;

    @Schema(description = "实例类型")
    @NotNull(message = "{key_8f11a8c423744b25b3ce8cf1c2bd7b7d}")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "开始时间")
    @NotNull(message = "{key_7b4cdaeff2874d8ca9e02799aa9e3df6}")
    private LocalDateTime startDateTime;

    @Schema(description = "结束时间")
    @NotNull(message = "{key_26389379d5084a518620451304c6116c}")
    private LocalDateTime endDateTime;

}
