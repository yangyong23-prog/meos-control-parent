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
public class ClientServerTendencyFigure {

    /**
     * 人数
     */
    private Long numberOfPeople;

    /**
     * 时间 yyyy-MM-dd HH:mm:ss
     */
    private LocalDateTime time;

}
