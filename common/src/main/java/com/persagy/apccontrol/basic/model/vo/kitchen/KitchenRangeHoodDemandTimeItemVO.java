package com.persagy.apccontrol.basic.model.vo.kitchen;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jspecify.annotations.NonNull;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 厨房排油烟需求时间时段VO
 *
 * <p>表示一个合并后的需求时段, 时间格式为 {@code HH:mm}</p>
 *
 * @author JiYinchuan
 * @since v7.261001.0
 */
@Data
@Schema(title = "厨房排油烟需求时间时段VO")
public class KitchenRangeHoodDemandTimeItemVO {

    /**
     * 时间格式 {@code HH:mm}
     *
     * @since v7.261001.0
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 开始时间, 格式 HH:mm
     *
     * @since v7.261001.0
     */
    @Schema(description = "开始时间, 格式 HH:mm")
    private String beginTime;

    /**
     * 结束时间, 格式 HH:mm, 24:00 表示当天结束
     *
     * @since v7.261001.0
     */
    @Schema(description = "结束时间, 格式 HH:mm, 24:00 表示当天结束")
    private String endTime;

    /**
     * 由需求时间区间构造时段项(开始/结束均须为 ABSOLUTE 类型的绝对时间)
     *
     * @param interval 绝对时间区间, 结束时间 00:00 表示 24:00(当天结束)
     * @return 时段项
     * @since v7.261001.0
     */
    public static @NonNull KitchenRangeHoodDemandTimeItemVO of(@NonNull DemandTimeIntervalValueObject interval) {
        KitchenRangeHoodDemandTimeItemVO item = new KitchenRangeHoodDemandTimeItemVO();
        item.setBeginTime(formatTime(interval.getBeginTime().getAbsoluteTime()));
        item.setEndTime(formatEndTime(interval.getEndTime().getAbsoluteTime()));
        return item;
    }

    /**
     * 格式化时间为 {@code HH:mm} 字符串
     *
     * @param time 时间
     * @return {@code HH:mm} 字符串
     * @since v7.261001.0
     */
    private static @NonNull String formatTime(@NonNull LocalTime time) {
        return TIME_FORMATTER.format(time);
    }

    /**
     * 格式化结束时间为 {@code HH:mm} 字符串, 00:00 输出 {@code 24:00}
     *
     * @param endTime 结束时间
     * @return {@code HH:mm} 字符串, 00:00 时返回 {@code 24:00}
     * @since v7.261001.0
     */
    private static @NonNull String formatEndTime(@NonNull LocalTime endTime) {
        if (LocalTime.MIN.equals(endTime)) {
            return "24:00";
        }
        return TIME_FORMATTER.format(endTime);
    }
}
