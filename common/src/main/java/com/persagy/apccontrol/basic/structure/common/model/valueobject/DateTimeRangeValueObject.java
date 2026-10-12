package com.persagy.apccontrol.basic.structure.common.model.valueobject;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Range;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.TemporalUnit;

/**
 * 时间范围值对象
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DateTimeRangeValueObject implements ValueObject {

    /**
     * 开始时间
     */
    private LocalDateTime beginDateTime;

    /**
     * 结束时间
     */
    private LocalDateTime endDateTime;

    /**
     * 转换时间范围
     *
     * @param unit 时间单位
     * @return 时间范围
     * @since 5.240702.0
     */
    public Range<ChronoLocalDateTime<?>> toRange(@NonNull TemporalUnit unit) {
        return Range.between(beginDateTime, endDateTime.minus(1, unit));
    }
}
