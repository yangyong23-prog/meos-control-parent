package com.persagy.apccontrol.basic.structure.demand.model.valueobject;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingSceneTimeIntervalDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherSunRiseAndSetDTO;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.TimeDeviationValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTimeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingSceneTimeTypeEnum;
import com.persagy.validate.utility.enumeration.TimeTypeEnum;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.enumeration.error.client.RequestServerErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalTime;
import java.util.Objects;

/**
 * 需求时间值对象
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Accessors(chain = true)
public class DemandTimeValueObject implements ValueObject {

    /**
     * 需求时间类型
     */
    private DemandTimeTypeEnum timeType;

    /**
     * 时间偏移, 无时间偏移时不存在
     */
    @Nullable
    private TimeDeviationValueObject timeDeviation;

    /**
     * 绝对时间, 仅当需求时间类型为 [ABSOLUTE] 时存在
     */
    private LocalTime absoluteTime;

    /**
     * 解析绝对时间
     *
     * @return 解析后的绝对时间
     * @since 7.250702.0
     */
    public LocalTime analyzeAbsoluteTime() {
        return analyzeAbsoluteTime(null);
    }

    /**
     * 根据日出日落时间解析绝对时间
     *
     * @param sunRiseAndSet 日出日落时间, 当时间类型为日出或日落时间时不能为空
     * @return 解析后的绝对时间
     * @since 5.240902.0
     */
    public LocalTime analyzeAbsoluteTime(@Nullable WeatherSunRiseAndSetDTO sunRiseAndSet) {
        LocalTime result;
        if (timeType == DemandTimeTypeEnum.ABSOLUTE) {
            result = absoluteTime;
        } else if (isSunRiseAndSetTimeType()) {
            if (Objects.isNull(sunRiseAndSet)) {
                throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_09fd476ad09c4732a6fdd65d2787c786"));
            }
            switch (timeType) {
                case SUNRISE:
                    result = sunRiseAndSet.getSunRise().toLocalTime();
                    break;
                case SUNSET:
                    result = sunRiseAndSet.getSunSet().toLocalTime();
                    break;
                default:
                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(timeType.getClass().getSimpleName(), timeType);
            }
        } else {
            throw new ApplicationException(RequestServerErrorEnum.SERVICE_ERROR, LocaleMessageUtil.getMessage("key_b1b9210f356a4deebecd5135ed63e6cc"));
        }
        if (Objects.nonNull(timeDeviation)) {
            result = result.plus(timeDeviation.getOffset(), timeDeviation.getTimeType().convertTemporalUnit());
        }
        return result;
    }

    /**
     * 是否为日出日落时间
     *
     * @return 是否为日出日落时间
     * @since 5.240902.0
     */
    @JsonIgnore
    @JSONField(serialize = false, deserialize = false)
    public boolean isSunRiseAndSetTimeType() {
        return getTimeType() == DemandTimeTypeEnum.SUNSET || getTimeType() == DemandTimeTypeEnum.SUNRISE;
    }

    /**
     * 创建绝对时间值对象
     *
     * @param absoluteTime 绝对时间
     * @return 绝对时间值对象
     * @since v7.261001.0
     */
    public static @NonNull DemandTimeValueObject ofAbsoluteTime(@NonNull LocalTime absoluteTime) {
        return new DemandTimeValueObject().setTimeType(DemandTimeTypeEnum.ABSOLUTE).setAbsoluteTime(absoluteTime);
    }

    /**
     * 创建最小绝对时间值对象
     *
     * @return 最小绝对时间值对象
     * @since 5.240902.0
     */
    public static DemandTimeValueObject ofAbsoluteTimeByMin() {
        return new DemandTimeValueObject().setTimeType(DemandTimeTypeEnum.ABSOLUTE).setAbsoluteTime(LocalTime.MIN);
    }

    /**
     * 转换对象
     *
     * @param source 数据源
     * @return 目标对象
     * @since 7.250801.0
     */
    public static DemandTimeValueObject of(@NonNull DemandLightingSceneTimeIntervalDTO source) {
        DemandLightingSceneTimeTypeEnum timeType = source.getTimeType();
        Integer offsetMinutes = source.getOffsetMinutes();

        DemandTimeValueObject result = new DemandTimeValueObject();
        switch (timeType) {
            case ABSOLUTE:
                result.setTimeType(DemandTimeTypeEnum.ABSOLUTE);
                break;
            case SUNRISE:
                result.setTimeType(DemandTimeTypeEnum.SUNRISE);
                break;
            case SUNSET:
                result.setTimeType(DemandTimeTypeEnum.SUNSET);
                break;
            default:
        }
        if (Objects.nonNull(offsetMinutes)) {
            TimeDeviationValueObject timeDeviationValueObject = new TimeDeviationValueObject();
            timeDeviationValueObject.setTimeType(TimeTypeEnum.MINUTE);
            timeDeviationValueObject.setOffset(offsetMinutes);
            result.setTimeDeviation(timeDeviationValueObject);
        }
        result.setAbsoluteTime(source.getAbsoluteTime());
        return result;
    }
}
