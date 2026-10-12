package com.persagy.apccontrol.basic.model.param.dashboard;

import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/10 5:53 PM
 */
@Data
public class StrategyAupportAiQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "系统ID")
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;

    @Schema(description = "策略类型")
    @NotNull(message = "{key_19816f305ede4b4cb9dfa17f77d4900f}")
    private StrategyBusinessTypeEnum strategyType;

}
