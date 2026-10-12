package com.persagy.apccontrol.basic.model.vo.airterminal.statistics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 末端首页设备统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirHomePageEquipmentVO {

    @Schema(description = "设备类")
    private String classCode;
    @Schema(description = "设备类名称")
    private String classCodeName;
    @Schema(description = "设备总数")
    private int equipmentTotal;

    @Schema(description = "开启数量")
    private int openTotal;
    @Schema(description = "关闭数据")
    private int closeTotal;
    @Schema(description = "无数据数量")
    private int noDataTotal;

    @Schema(description = "离线数量")
    private int offlineTotal;
    @Schema(description = "控反不一致数量")
    private int orderFailAlarmTotal;
    @Schema(description = "异常数量")
    private int exceptionTotal;
    @Schema(description = "本地数量")
    private int localityTotal;
    @Schema(description = "手动数量")
    private int manualTotal;
    @Schema(description = "自动数量")
    private int autoTotal;

}
