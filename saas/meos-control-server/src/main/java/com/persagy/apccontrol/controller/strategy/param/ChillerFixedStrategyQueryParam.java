package com.persagy.apccontrol.controller.strategy.param;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 策略对象查询参数
 *
 * @since v5.6.0
 */
@Data
@Schema(title = "策略对象查询参数")
public class ChillerFixedStrategyQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "系统类型")
    @NotNull(message = "{key_ada34b38fcf64928976a4f4d84f02c45}")
    private DtpRwdSystemClassCodeEnum systemType;

    @Schema(description = "系统ID")
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;

    @Schema(description = "开始时间")
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    private LocalDateTime beginDateTime;

    @Schema(description = "结束时间")
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    private LocalDateTime endDateTime;
}
