package com.persagy.apccontrol.basic.outside.dto;


import lombok.Data;

/**
 * 聚合接口统计返回数据
 */
@Data
public class ExhaustFumeStatisticsVo {


    /**
     * 自动数量
     */
    private int autoTotal;
    /**
     * 本地数量
     */
    private int localTotal;
    /**
     * 手动的数量
     */
    private int manualTotal;
    /**
     * 离线数量
     */
    private int offLineTotal;
    /**
     * 控反不一致数量
     */
    private int alarmTotal;
    /**
     * 运行关的数量
     */
    private int stopTotal;
    /**
     * 运行开的数量
     */
    private int runTotal;
    /**
     * 无数据的数量
     */
    private int unknown;

    /**
     * 设备总数
     */
    private int total;

    public Integer getUnknown() {
        return total - runTotal - stopTotal;
    }
}
