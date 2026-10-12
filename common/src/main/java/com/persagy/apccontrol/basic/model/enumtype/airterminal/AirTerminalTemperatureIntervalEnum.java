package com.persagy.apccontrol.basic.model.enumtype.airterminal;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
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
 * 空调末端-品质温度区间枚举
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@AllArgsConstructor
public enum AirTerminalTemperatureIntervalEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 过冷
     */
    TOO_COLD("TOO_COLD", "key_ddf41f2614ef4298b892ed646f775632", "if %s < %s then {return '%s'}", "key_ac7bf0c241ab4f8e9edf700626273ed9"),

    /**
     * 偏冷
     */
    COLD("COLD", "key_d5efabec90a94abeb411beaa49e41e6e", "if %s >= %s && %s < %s then {return '%s'}", "[%s-%s)"),

    /**
     * 舒适
     */
    APPROPRIATE("APPROPRIATE", "key_5c29afa4bc2f40d89ab4a74714a3241d", "if %s >= %s && %s < %s then {return '%s'}", "[%s-%s)"),

    /**
     * 偏热
     */
    HOT("HOT", "key_597b521f490e44beb2eb1ca328e19ccc", "if %s >= %s && %s <= %s then {return '%s'}", "[%s-%s]"),

    /**
     * 过热
     */
    TOO_HOT("TOO_HOT", "key_3502aed459374bf4962a3a08c52a4f88", "if %s > %s then {return '%s'}", "key_cc55ca6bc94945c88b670ddcee35e661");

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

    public String getLimitTips() {
        return LocaleMessageUtil.getMessage(this.limitTips);
    }

    @Override
    public Map<String, String> additionalMapValue() {
        return new HashMap<>(16);
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * 构建详情
     *
     * @param name             表达式Key
     * @param tdbSeasonQuality 温度季节区间
     * @return 详情
     * @since 5.241102.0
     */
    public Detail ofDetail(
            @NonNull String name,
            @Nullable DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality) {
        BigDecimal limitMin;
        BigDecimal limitMax;
        switch (this) {
            case TOO_COLD:
                limitMax = BigDecimal.valueOf(16);
                if (Objects.nonNull(tdbSeasonQuality)) {
                    if (Objects.nonNull(tdbSeasonQuality.getUnderCoolingSeasonLimitValue())) {
                        limitMax = tdbSeasonQuality.getUnderCoolingSeasonLimitValue();
                    }
                }
                return new Detail(
                        String.format(calculationExpress, name, limitMax.toPlainString(), value),
                        String.format(getLimitTips(), limitMax.toPlainString()));
            case COLD:
                limitMin = BigDecimal.valueOf(16);
                limitMax = BigDecimal.valueOf(18);
                if (Objects.nonNull(tdbSeasonQuality)) {
                    if (Objects.nonNull(tdbSeasonQuality.getUnderCoolingSeasonLimitValue())) {
                        limitMin = tdbSeasonQuality.getUnderCoolingSeasonLimitValue();
                    }
                    if (Objects.nonNull(tdbSeasonQuality.getLowerComfortSeasonLimitValue())) {
                        limitMax = tdbSeasonQuality.getLowerComfortSeasonLimitValue();
                    }
                }
                return new Detail(String.format(calculationExpress,
                        name, limitMin.toPlainString(),
                        name, limitMax.toPlainString(), value),
                        String.format(getLimitTips(), limitMin.toPlainString(), limitMax.toPlainString()));
            case APPROPRIATE:
                limitMin = BigDecimal.valueOf(18);
                limitMax = BigDecimal.valueOf(24);
                if (Objects.nonNull(tdbSeasonQuality)) {
                    if (Objects.nonNull(tdbSeasonQuality.getLowerComfortSeasonLimitValue())) {
                        limitMin = tdbSeasonQuality.getLowerComfortSeasonLimitValue();
                    }
                    if (Objects.nonNull(tdbSeasonQuality.getUpperComfortSeasonLimitValue())) {
                        limitMax = tdbSeasonQuality.getUpperComfortSeasonLimitValue();
                    }
                }
                return new Detail(String.format(calculationExpress,
                        name, limitMin.toPlainString(),
                        name, limitMax.toPlainString(), value),
                        String.format(getLimitTips(), limitMin.toPlainString(), limitMax.toPlainString()));
            case HOT:
                limitMin = BigDecimal.valueOf(24);
                limitMax = BigDecimal.valueOf(26);
                if (Objects.nonNull(tdbSeasonQuality)) {
                    if (Objects.nonNull(tdbSeasonQuality.getUpperComfortSeasonLimitValue())) {
                        limitMin = tdbSeasonQuality.getUpperComfortSeasonLimitValue();
                    }
                    if (Objects.nonNull(tdbSeasonQuality.getOverHeatingSeasonLimitValue())) {
                        limitMax = tdbSeasonQuality.getOverHeatingSeasonLimitValue();
                    }
                }
                return new Detail(String.format(calculationExpress,
                        name, limitMin.toPlainString(),
                        name, limitMax.toPlainString(), value),
                        String.format(getLimitTips(), limitMin.toPlainString(), limitMax.toPlainString()));
            case TOO_HOT:
                limitMax = BigDecimal.valueOf(26);
                if (Objects.nonNull(tdbSeasonQuality)) {
                    if (Objects.nonNull(tdbSeasonQuality.getOverHeatingSeasonLimitValue())) {
                        limitMax = tdbSeasonQuality.getOverHeatingSeasonLimitValue();
                    }
                }
                return new Detail(String.format(calculationExpress, name, limitMax.toPlainString(), value),
                        String.format(getLimitTips(), limitMax.toPlainString()));
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AirTerminalTemperatureIntervalEnum analyze(String value) {
        return BaseEnum.analyze(value, AirTerminalTemperatureIntervalEnum.class);
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
