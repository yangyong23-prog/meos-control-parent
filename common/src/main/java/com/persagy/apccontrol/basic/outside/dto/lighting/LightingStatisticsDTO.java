package com.persagy.apccontrol.basic.outside.dto.lighting;

import lombok.Data;

import java.util.List;

/**
 * 照明状态统计
 *
 * @author : wangchenghong
 * @date : 2024/12/5 15:46
 */
@Data
public class LightingStatisticsDTO {

    // 设备id列表
    private List<String> objectIds;

    // 查询时间点(yyyyMMddHHmmss)
    private String ipTime;

}
