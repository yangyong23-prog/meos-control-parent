package com.persagy.apccontrol.basic.model.dto.twin;

import lombok.Data;

/**
 * 值班首页twin聚合查询返回对象
 */
@Data
public class DutyHomePageDTO {

    /**
     * 实例总数
     */
    private int total;
    /**
     * 在线设备实例总数
     */
    private int runTotal;
    /**
     * 手动干预的设备实例数
     */
    private int manualTotal;

    /**
     * 多开数量 (照明的参数)
     */
    private int moreTotal;
    /**
     * 少开数量 (照明的参数)
     */
    private int lessTotal;

    /**
     * 离线设备实例总数
     */
    private int offLineTotal;
    /**
     * 控反不一致的设备实例总数
     */
    private int alarmTotal;

}
