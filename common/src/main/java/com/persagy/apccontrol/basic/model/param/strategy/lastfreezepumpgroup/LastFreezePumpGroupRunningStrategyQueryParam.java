package com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 末级泵组运行策略查询参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Schema(title = "末级泵组运行策略查询参数")
@NoArgsConstructor
@AllArgsConstructor
public class LastFreezePumpGroupRunningStrategyQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "中央供冷系统ID")
    @NotBlank(message = "{key_f424e5924de240339438b71ec6a25e84}")
    private String acccSystemId;

    @Schema(description = "开始时间")
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private LocalDateTime beginDateTime;

    @Schema(description = "结束时间")
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    private LocalDateTime endDateTime;

}
