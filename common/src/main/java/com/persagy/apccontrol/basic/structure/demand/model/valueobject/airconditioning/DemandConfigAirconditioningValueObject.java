package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * 空调㤹配置值对象
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class DemandConfigAirconditioningValueObject extends BaseDemandConfig implements ValueObject {

    @Schema(description = "价值选择配置")
    private DemandConfigAirconditioningValueSelectValueObject valueSelect;

    @Schema(description = "基础配置")
    private DemandConfigAirconditioningBaseConfigValueObject baseConfig;

    @Schema(description = "环境品质配置")
    private DemandConfigAirconditioningEnvironmentQualityObject environmentQuality;

    @Schema(description = "空间运行工况标准配置")
    private DemandConfigAirconditioningWorkConditionValueObject workCondition;

    @Override
    public void fillDefaultConfig(@NonNull DemandInstanceTypeEnum instanceType) {
        if (instanceType == DemandInstanceTypeEnum.PROJECT) {
            // 基础配置默认值
            DemandConfigAirconditioningBaseConfigValueObject baseConfig = new DemandConfigAirconditioningBaseConfigValueObject();
            baseConfig.setIsAlgorithmCompute(true);
            baseConfig.setIsAllowEarlyShutdown(true);
            baseConfig.setIsAllowResponseRegulation(false);
            this.setBaseConfig(baseConfig);

            // 环境品质配置默认值
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = new DemandConfigAirconditioningEnvironmentQualityObject();
            environmentQuality.setIsParticipateInScheduling(true);
            environmentQuality.setIsPressureDiffQualityEnabled(false);

            // 温度品质
            DemandConfigAirconditioningTdbQualityValueObject tdbQuality = new DemandConfigAirconditioningTdbQualityValueObject();
            tdbQuality.setTemperatureScheduledHourInDay(new BigDecimal("24"));
            tdbQuality.setTemperatureContinuousScheduledHour(new BigDecimal("1.5"));
            tdbQuality.setSeasonQuality(new ArrayList<>());

            MidCalendarAirSeasonTypeEnum[] seasonTypeNewEnums = MidCalendarAirSeasonTypeEnum.values();
            for (MidCalendarAirSeasonTypeEnum seasonTypeNewEnum : seasonTypeNewEnums) {
                BigDecimal lowerComfortSeasonLimitValue = MidCalendarAirSeasonTypeEnum.AIR_SUMMER.equals(seasonTypeNewEnum) ? new BigDecimal("24")
                        : MidCalendarAirSeasonTypeEnum.AIR_SUMMER_PEAK.equals(seasonTypeNewEnum) ? new BigDecimal("22")
                        : MidCalendarAirSeasonTypeEnum.AIR_WINTER.equals(seasonTypeNewEnum) ? new BigDecimal("18")
                        : MidCalendarAirSeasonTypeEnum.AIR_SEASON_SPRING.equals(seasonTypeNewEnum) ? new BigDecimal("18")
                        : MidCalendarAirSeasonTypeEnum.AIR_SEASON_AUTUMN.equals(seasonTypeNewEnum) ? new BigDecimal("18") : null;
                BigDecimal upperComfortSeasonLimitValue = MidCalendarAirSeasonTypeEnum.AIR_SUMMER.equals(seasonTypeNewEnum) ? new BigDecimal("26")
                        : MidCalendarAirSeasonTypeEnum.AIR_SUMMER_PEAK.equals(seasonTypeNewEnum) ? new BigDecimal("25")
                        : MidCalendarAirSeasonTypeEnum.AIR_WINTER.equals(seasonTypeNewEnum) ? new BigDecimal("26")
                        : MidCalendarAirSeasonTypeEnum.AIR_SEASON_SPRING.equals(seasonTypeNewEnum) ? new BigDecimal("26")
                        : MidCalendarAirSeasonTypeEnum.AIR_SEASON_AUTUMN.equals(seasonTypeNewEnum) ? new BigDecimal("26") : null;
                BigDecimal underCoolingSeasonLimitValue = MidCalendarAirSeasonTypeEnum.AIR_SUMMER.equals(seasonTypeNewEnum) ? new BigDecimal("22")
                        : MidCalendarAirSeasonTypeEnum.AIR_SUMMER_PEAK.equals(seasonTypeNewEnum) ? new BigDecimal("20")
                        : MidCalendarAirSeasonTypeEnum.AIR_WINTER.equals(seasonTypeNewEnum) ? new BigDecimal("16")
                        : MidCalendarAirSeasonTypeEnum.AIR_SEASON_SPRING.equals(seasonTypeNewEnum) ? new BigDecimal("16")
                        : MidCalendarAirSeasonTypeEnum.AIR_SEASON_AUTUMN.equals(seasonTypeNewEnum) ? new BigDecimal("16") : null;
                BigDecimal overHeatingSeasonLimitValue = MidCalendarAirSeasonTypeEnum.AIR_SUMMER.equals(seasonTypeNewEnum) ? new BigDecimal("27")
                        : MidCalendarAirSeasonTypeEnum.AIR_SUMMER_PEAK.equals(seasonTypeNewEnum) ? new BigDecimal("27")
                        : MidCalendarAirSeasonTypeEnum.AIR_WINTER.equals(seasonTypeNewEnum) ? new BigDecimal("27")
                        : MidCalendarAirSeasonTypeEnum.AIR_SEASON_SPRING.equals(seasonTypeNewEnum) ? new BigDecimal("27")
                        : MidCalendarAirSeasonTypeEnum.AIR_SEASON_AUTUMN.equals(seasonTypeNewEnum) ? new BigDecimal("27") : null;
                DemandConfigAirconditioningTdbSeasonValueObject seasonValueObject = new DemandConfigAirconditioningTdbSeasonValueObject();
                seasonValueObject.setLowerComfortSeasonLimitValue(lowerComfortSeasonLimitValue);
                seasonValueObject.setUpperComfortSeasonLimitValue(upperComfortSeasonLimitValue);
                seasonValueObject.setUnderCoolingSeasonLimitValue(underCoolingSeasonLimitValue);
                seasonValueObject.setOverHeatingSeasonLimitValue(overHeatingSeasonLimitValue);
                seasonValueObject.setAirSeason(seasonTypeNewEnum);
                tdbQuality.getSeasonQuality().add(seasonValueObject);
            }
            environmentQuality.setTdbQuality(tdbQuality);

            // CO2品质
            DemandConfigAirconditioningCO2QualityValueObject cO2Quality = new DemandConfigAirconditioningCO2QualityValueObject();
            cO2Quality.setCarbonDioxideScheduledHourInDay(new BigDecimal("24"));
            cO2Quality.setCarbonDioxideContinuousScheduledHour(new BigDecimal("1.5"));
            cO2Quality.setCarbonDioxideLimitValue(new BigDecimal("800"));
            cO2Quality.setCarbonDioxideUpperLimitValue(new BigDecimal("1200"));
            environmentQuality.setCO2Quality(cO2Quality);

            this.setEnvironmentQuality(environmentQuality);
        }
    }

}
