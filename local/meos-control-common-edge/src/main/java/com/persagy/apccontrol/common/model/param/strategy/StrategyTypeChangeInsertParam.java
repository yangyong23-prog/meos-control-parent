package com.persagy.apccontrol.common.model.param.strategy;

import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/10 5:53 PM
 */
@Data
public class StrategyTypeChangeInsertParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "系统ID")
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String systemId;

    @Schema(description = "策略类型")
    @NotNull(message = "{key_d54e1825d4fd48f6b23f00bd635bf0b8}")
    private StrategyBusinessTypeEnum strategyType;

    @Schema(description = "策略状态数据")
    @NotNull(message = "{key_4b16b47b7b7d4e989b9bfb8a47cd6293}")
    private List<AiStrategyStatus> statusList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiStrategyStatus{
        @Schema(description = "对象id")
        @NotNull(message = "{key_084485a601e5437688eeacf92ba08c45}")
        private String objectId;

        @Schema(description = "对象名称")
        private String localName;

        @Schema(description = "是否切换为AI策略")
        @NotNull(message = "{key_224538556473487fb32246004af304fc}")
        private Boolean isAiStrategy;
    }
}
