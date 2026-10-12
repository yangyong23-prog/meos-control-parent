package com.persagy.apccontrol.basic.outside.dto.lighting;

import lombok.Data;

/**
 * 照明状态统计
 *
 * @author : wangchenghong
 * @date : 2024/12/5 15:46
 */
@Data
public class LightingStatisticsResponse {

    // 运行设备数量
    private Integer runTotal;

    // 停止设备数量
    private Integer stopTotal;

    // 离线设备数量
    private Integer offLineTotal;

    // 控反不一致设备数量
    private Integer alarmTotal;

    // 本地设备数量
    private Integer localTotal;

    // 手动状态设备数量
    private Integer manualTotal;

    // 自动状态设备数量
    private Integer autoTotal;

}
