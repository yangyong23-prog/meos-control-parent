package com.persagy.apccontrol.basic.model.enumtype.airterminal;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningCO2QualityValueObject;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 空调末端-品质二氧化碳区间枚举
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@AllArgsConstructor
public enum AirTerminalCarbonDioxideIntervalEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 合适
     */
    APPROPRIATE("APPROPRIATE", "key_1cada3cbb81d41a699337f1b9681d21d", "if %s <= %s then {return '%s'}", "key_ef96d7558c8144809ec442d4cc3cebe7"),

    /**
     * 偏高
     */
    HIGH("HIGH", "key_8127278821ee4d3e8e6cec38d29a738a", "", ""),
    /**
     * 过高
     */
    TOO_HIGH("TOO_HIGH", "key_0e32ed2304dd45aeb871f81bd7de4e53", "if %s > %s then {return '%s'}", "key_34596773db8344cab07cdfbec7703bf6");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 计算公式
     */
    private final String calculationExpress;

    /**
     * 提示
     */
    private final String limitTips;

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @Override
    public Map<String, String> additionalMapValue() {
        return new HashMap<>(16);
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String getLimitTips() {
        return LocaleMessageUtil.getMessage(this.limitTips);
    }

    /**
     * 构建详情
     *
     * @param name       表达式Key
     * @param co2Quality 二氧化碳区间
     * @return 详情
     * @since 5.241102.0
     */
    public AirTerminalCarbonDioxideIntervalEnum.Detail ofDetail(
            @NonNull String name,
            @Nullable DemandConfigAirconditioningCO2QualityValueObject co2Quality) {
        BigDecimal limit = BigDecimal.valueOf(800);
        switch (this) {
            case APPROPRIATE:
                // 合适区间临时采用过高门限判断, 后续加了较高类型再随着产品需求改
                if (Objects.nonNull(co2Quality) && Objects.nonNull(co2Quality.getCarbonDioxideUpperLimitValue())) {
                    limit = co2Quality.getCarbonDioxideUpperLimitValue();
                }
                return new AirTerminalCarbonDioxideIntervalEnum.Detail(String.format(
                        calculationExpress, name, limit.toPlainString(), value),
                        String.format(getLimitTips(), limit.toPlainString()));
            case TOO_HIGH:
                if (Objects.nonNull(co2Quality) && Objects.nonNull(co2Quality.getCarbonDioxideUpperLimitValue())) {
                    limit = co2Quality.getCarbonDioxideUpperLimitValue();
                }
                return new AirTerminalCarbonDioxideIntervalEnum.Detail(String.format(
                        calculationExpress, name, limit.toPlainString(), value),
                        String.format(getLimitTips(), limit.toPlainString()));
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AirTerminalCarbonDioxideIntervalEnum analyze(String value) {
        return BaseEnum.analyze(value, AirTerminalCarbonDioxideIntervalEnum.class);
    }

    /**
     * 详情
     *
     * @author JiYinchuan
     * @since 5.241102.0
     */
    @Data
    @AllArgsConstructor
    public static class Detail {

        /**
         * 计算公式
         */
        private String express;

        /**
         * 限制提示
         */
        private String limitTips;

    }
}
