package com.persagy.apccontrol.basic.model.param.strategy;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 策略干预公共参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Schema(title = "策略干预公共参数")
public class StrategyCommonParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "系统类型")
    @NotNull(message = "{key_ada34b38fcf64928976a4f4d84f02c45}")
    private DtpRwdSystemClassCodeEnum systemType;

    @Schema(description = "系统ID")
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;

    @Schema(description = "业务类型")
    @NotNull(message = "{key_f36f4ee25a9844569ccb17a57e5e3271}")
    private StrategyBusinessTypeEnum businessType;

    @Schema(description = "策略类型")
    @NotNull(message = "{key_d54e1825d4fd48f6b23f00bd635bf0b8}")
    private StrategyTypeEnum strategyType;

    @Schema(description = "对象ID")
    private String objectId;

}
