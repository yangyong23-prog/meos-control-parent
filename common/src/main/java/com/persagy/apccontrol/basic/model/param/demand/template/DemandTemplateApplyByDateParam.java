package com.persagy.apccontrol.basic.model.param.demand.template;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.Range;

import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Set;

/**
 * 需求管理-需求日历应用模版参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-需求日历按日期应用模版参数")
public class DemandTemplateApplyByDateParam implements BaseCheckParamProvider {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模版ID")
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    @Schema(description = "配置日期")
    @NotEmpty(message = "{key_a6da86a6f4eb42b1819c5b7c08400461}")
    private Set<LocalDate> dates;

    @Override
    public void checkAndSet() {
        Range<ChronoLocalDate> allowApplyDateRange = DemandTemplateApplyByDateParam.allowApplyDateRange();
        LocalDate allowApplyBeginDate = LocalDate.from(allowApplyDateRange.getMinimum());
        LocalDate allowApplyEndDate = LocalDate.from(allowApplyDateRange.getMaximum());
        for (LocalDate date : dates) {
            if (date.isBefore(allowApplyBeginDate) || date.isAfter(allowApplyEndDate)) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_b6968bf76004427698d4b8cd33209139"));
            }
        }
    }

    /**
     * 允许应用模版日历的日期范围
     *
     * <p>[明天-次年年最后一天]
     *
     * @return 允许应用模版日历的日期范围
     * @since v6.0.0
     */
    public static Range<ChronoLocalDate> allowApplyDateRange() {
        LocalDate today = DateUtils.now().toLocalDate();
        return Range.between(today, LocalDate.ofYearDay(today.getYear() + 2, 1).minusDays(1));
    }
}
