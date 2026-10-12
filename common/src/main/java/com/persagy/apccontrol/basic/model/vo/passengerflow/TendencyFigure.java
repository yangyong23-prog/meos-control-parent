package com.persagy.apccontrol.basic.model.vo.passengerflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 趋势图数据
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/21 17:45
 * @since v5.240802.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TendencyFigure {

    /**
     * 入
     */
    private Long spInPeopleFlow;

    /**
     * 出
     */
    private Long spOutPeopleFlow;

    /**
     * 客流人数（该小时点进入-离开，任一侧无数据为null）
     */
    private Long spNetPeopleFlow;

    /**
     * 时间 yyyy-MM-dd HH:mm:ss
     */
    private LocalDateTime time;

}
