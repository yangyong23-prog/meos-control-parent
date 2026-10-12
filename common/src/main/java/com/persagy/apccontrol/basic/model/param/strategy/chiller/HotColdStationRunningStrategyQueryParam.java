package com.persagy.apccontrol.basic.model.param.strategy.chiller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 冷站源运行策略查询参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Schema(title = "冷站源运行策略查询参数")
@NoArgsConstructor
@AllArgsConstructor
public class HotColdStationRunningStrategyQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "系统ID")
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;

    @Schema(description = "开始时间, 闭区间")
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private LocalDateTime beginDateTime;

    @Schema(description = "结束时间, 开区间")
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    private LocalDateTime endDateTime;

}
