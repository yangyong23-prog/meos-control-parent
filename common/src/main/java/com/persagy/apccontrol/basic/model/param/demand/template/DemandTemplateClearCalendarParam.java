package com.persagy.apccontrol.basic.model.param.demand.template;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.validate.utility.enumeration.DateTypeEnum;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;

import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.Year;
import java.time.chrono.ChronoLocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * 需求管理-需求日历清空参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-需求日历清空参数")
public class DemandTemplateClearCalendarParam implements BaseCheckParamProvider {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "是否重置")
    private Boolean isReset = false;

    @Schema(description = "年份, 重置时不能为空")
    private Year year;

    @Schema(description = "日期列表, 非重置时不能为空")
    private Set<LocalDate> dates;

    @Override
    public void checkAndSet() {
        if (BooleanUtils.isNotTrue(isReset) && CollectionUtils.isEmpty(dates)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_20ba46ea2bde4d0b9e53e46fe42fdbf1"));
        }
        Range<ChronoLocalDate> allowApplyDateRange = DemandTemplateApplyByDateParam.allowApplyDateRange();
        LocalDate allowApplyBeginDate = LocalDate.from(allowApplyDateRange.getMinimum());
        LocalDate allowApplyEndDate = LocalDate.from(allowApplyDateRange.getMaximum());
        if (BooleanUtils.isTrue(isReset)) {
            if (Objects.isNull(year)) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_db79ffa28fde4762a64880f92e0f738c"));
            }
            if (year.getValue() < Year.now().getValue()) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_c3ae86ee60234cdc9b54089cc14886e7"));
            }
            if (year.getValue() > allowApplyEndDate.getYear()) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_f5327501d75a47849e47a47987a96ce2"));
            }
            dates = generateResetDates();
        }
        for (LocalDate date : dates) {
            if (date.isBefore(allowApplyBeginDate) || date.isAfter(allowApplyEndDate)) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_d43e728acf944fbb951737e54743905b"));
            }
        }
    }

    /**
     * 生成重置日期
     *
     * @return 重置日期
     * @since 7.250101.0
     */
    private Set<LocalDate> generateResetDates() {
        if (Objects.isNull(year)) {
            return Sets.newHashSet();
        }
        LocalDate today = DateUtils.now().toLocalDate();
        LocalDate beginDate;
        LocalDate endDate;
        // 年份小于当前年时, 无有效重置日期
        if (year.getValue() < today.getYear()) {
            return Sets.newHashSet();
        } else if (today.getYear() == year.getValue()) {
            // 为当年时开始日期为当日, 结束日期为当年最后一天
            beginDate = today;
            endDate = LocalDate.ofYearDay(today.getYear() + 1, 1).minusDays(1);
        } else {
            // 未来时开始日期为当年第一日, 结束日期为当年最后一天
            beginDate = LocalDate.ofYearDay(today.getYear() + 1, 1);
            endDate = LocalDate.ofYearDay(beginDate.getYear() + 1, 1).minusDays(1);
        }
        return DateTimeRangeExecutor.generateDate(DateTypeEnum.DAY, beginDate, endDate, true, eachDate -> eachDate).keySet();
    }
}
