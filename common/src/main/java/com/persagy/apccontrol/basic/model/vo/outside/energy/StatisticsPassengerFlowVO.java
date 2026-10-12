package com.persagy.apccontrol.basic.model.vo.outside.energy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客流数据统计--能源与碳
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/9/3 11:22
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsPassengerFlowVO {

    @Schema(description = "离线数量")
    private Integer offlineCount;

    @Schema(description = "高等级的异常事件数量")
    private Integer exceptionEventCount;

    @Schema(description = "实时客流人数")
    private Integer exsitPeopleNum;

    @Schema(description = "当日累计客流人数")
    private Integer accExsitPeopleNum;

    @Schema(description = "客流统计趋势图")
    private List<StatisticTrend> currentPassengerFlowStatisticTrend;

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticTrend {

        @Schema(description = "时间")
        private LocalDateTime dataTime;

        @Schema(description = "数值")
        private Integer dataValue;

    }

}
