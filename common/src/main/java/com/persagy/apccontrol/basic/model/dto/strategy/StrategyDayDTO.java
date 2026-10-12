package com.persagy.apccontrol.basic.model.dto.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 日策略DTO
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Schema(title = "策略日DTO")
public class StrategyDayDTO {

    @Nullable
    @Schema(description = "策略ID, 非当前服务数据时ID为空")
    private String strategyId;

    @Nullable
    @Schema(description = "策略日ID, 非当前服务数据或临时策略时ID为空")
    private String strategyDayId;

    @Schema(description = "策略日期")
    @NotNull(message = "{key_a937a48ad05542cdbaf9d98863ad71eb}")
    private LocalDate date;

    @Schema(description = "策略计划")
    @NotEmpty(message = "{key_8cd1dfb766134474838eba98f0e1ee52}")
    @Valid
    private List<StrategyPlanDTO> plans;

}
