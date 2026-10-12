package com.persagy.apccontrol.basic.model.dto.twin;

import lombok.Data;

/**
 * 末端聚合查询返回对象
 */
@Data
public class StatisManageTerminalDTO {

    /**
     * 实例总数
     */
    private int total;
    /**
     * 运行设备实例总数
     */
    private int runTotal;
    /**
     * 关闭设备实例总数
     */
    private int stopTotal;
    /**
     * 离线设备实例总数
     */
    private int offLineTotal;

    /**
     * 控反不一致的设备实例总数
     */
    private int alarmTotal;
    /**
     * 本地设备实例总数
     */
    private int localTotal;
    /**
     * 自动的设备实例数
     */
    private int autoTotal;
    /**
     * 手动干预的设备实例数
     */
    private int manualTotal;


    public Integer getNoDataTotal() {
        return total - runTotal - stopTotal;
    }

}
