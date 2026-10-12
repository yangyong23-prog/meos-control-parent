package com.persagy.apccontrol.basic.model.vo.airterminal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 空调末端-空调末端-温度环境品质统计
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Data
@Schema(title = "空调末端-温度环境品质统计")
public class AirTerminalSpaceTemperatureStatisticsVO {

    /** 过冷数量*/
    private Integer tooColdCount = 0;
    /**较冷数量 */
    private Integer coldCount = 0;
    /**合适数量 */
    private Integer appropriateCount = 0;
    /** 较热数量*/
    private Integer hotCount = 0;
    /**过热数量 */
    private Integer tooHotCount = 0;
    /** 无数据数量*/
    private Integer noDataCount = 0;
    /** 所有空间数量*/
    private Integer allSpaceCount = 0;
}
