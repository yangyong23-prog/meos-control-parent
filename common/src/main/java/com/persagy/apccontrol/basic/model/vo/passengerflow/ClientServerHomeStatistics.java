package com.persagy.apccontrol.basic.model.vo.passengerflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 客流首页统计
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/21 17:44
 * @since v5.240802.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientServerHomeStatistics {

    /**
     * 今日客流人数
     */
    private Long todayTotalNumberOfPeople;

    /**
     * 平均人数
     */
    private Long averageNumberOfPeople;

    /**
     * 近30天人数图
     */
    private List<ClientServerTendencyFigure> graph;

}
