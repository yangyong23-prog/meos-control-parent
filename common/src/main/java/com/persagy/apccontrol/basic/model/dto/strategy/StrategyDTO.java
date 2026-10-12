package com.persagy.apccontrol.basic.model.dto.strategy;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyBusinessTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.StrategyTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 策略DTO
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Schema(title = "策略DTO")
public class StrategyDTO {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "系统类编码")
    @NotNull(message = "{key_a01cdb254aed43de972f18166fba85c8}")
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
    @NotBlank(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private String objectId;

    @Schema(description = "日策略计划")
    @NotEmpty(message = "{key_810e1c4ceefb4f32ace837e6fe9c3ef4}")
    @Valid
    private List<StrategyDayDTO> dayPlans;


    /**
     * 遍历策略计划
     *
     * @param consumer 函数式接口
     * @since v5.6.0
     */
    public void eachPlans(@NonNull BiConsumer<LocalDate, List<StrategyPlanDTO>> consumer) {
        if (CollectionUtils.isEmpty(dayPlans)) {
            return;
        }
        for (StrategyDayDTO dayPlan : dayPlans) {
            LocalDate planDate = dayPlan.getDate();
            List<StrategyPlanDTO> plans = dayPlan.getPlans();
            if (CollectionUtils.isEmpty(plans)) {
                continue;
            }
            consumer.accept(planDate, plans);
        }
    }
}
