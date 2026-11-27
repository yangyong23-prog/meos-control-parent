package com.persagy.apccontrol.common.opt;

import java.time.ZoneId;

/**
 * 孪生数据源切换的转换参数：dataTime 渲染时区、时间常数（分钟）。读路径与 MQTT 推送共用。
 */
public class OptHubTwinSettings {

    private final ZoneId zone;
    private final int dataIndate;

    public OptHubTwinSettings(ZoneId zone, int dataIndate) {
        this.zone = zone;
        this.dataIndate = dataIndate;
    }

    public ZoneId getZone() {
        return zone;
    }

    public int getDataIndate() {
        return dataIndate;
    }
}
