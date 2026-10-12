package com.persagy.apccontrol.basic.model.dto.strategy;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.persagy.apccontrol.basic.model.ControlFieldParser;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.validate.StrategyGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 策略计划DTO
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Schema(title = "策略计划DTO")
public class StrategyPlanDTO {

    @Nullable
    @Schema(description = "策略计划更新时间")
    private String updateTime;

    @Schema(description = "策略ID, 非当前服务时ID为空")
    @NotNull(message = "{key_26525506f274445b98a26817a4736cb3}")
    private String strategyId;

    @Schema(description = "策略计划ID, 非当前服务时ID为空")
    @NotNull(message = "{key_586f8065b36a469197fcfa5144f1b575}")
    private Long strategyPlanId;

    @Schema(description = "策略生效时间")
    @NotNull(message = "{key_856a7375b381487a86ac2ad0dae36620}", groups = StrategyGroup.FixedStrategy.class)
    private LocalDateTime startTime;

    @Schema(description = "策略失效时间")
    @NotNull(message = "{key_856a7375b381487a86ac2ad0dae36620}", groups = StrategyGroup.FixedStrategy.class)
    private LocalDateTime endTime;

    @Schema(description = "计划状态")
    @NotNull(message = "{key_75da3b4e1052421f887dec1ab30a3274}")
    private StateTypeEnum status;

    @JsonUnwrapped(prefix = "plan_")
    @Schema(description = "计划详情")
    @NotBlank(message = "{key_dcb9bb8693e14e188576e3473eaa9584}")
    private String result;

    /**
     * 排序并获取新的计划数组
     *
     * @param source 数据源
     * @param <T>    数据源类型
     * @return 排序后新的计划数组
     * @since v5.6.0
     */
    public static <T extends StrategyPlanDTO> List<T> sortedAndGetNew(@NonNull List<T> source) {
        return source.stream()
                .sorted(Comparator.comparing(StrategyPlanDTO::getEndTime).thenComparing(StrategyPlanDTO::getStartTime))
                .collect(Collectors.toList());
    }

    /**
     * 策略计划详情
     *
     * @author JiYinchuan
     * @since v5.6.0
     */
    @Data
    @Schema(title = "策略计划详情")
    @FieldNameConstants
    @SuppressWarnings("AlibabaAbstractClassShouldStartWithAbstractNaming")
    public static abstract class Detail implements ControlFieldParser {

        @Schema(description = "策略时间")
        @NotNull(message = "{key_856a7375b381487a86ac2ad0dae36620}", groups = StrategyGroup.FixedStrategy.class)
        protected LocalTime strategyTime;

        @Schema(description = "运行状态")
        @NotNull(message = "{key_f5a9bb1ac9e2458e810d226731a9a3b2}", groups = StrategyGroup.FixedStrategy.class)
        protected RunStatusEnum runStatus;

    }
}
