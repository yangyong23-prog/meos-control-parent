package com.persagy.apccontrol.basic.model.vo.duty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 大卡片标签数据
 */
@Data
public class CardTagVO {


    @Schema(description = "异常事件总数")
    private int errorEventTotal;

    @Schema(description = "离线设备总数")
    private int offlineTotal;

    @Schema(description = "控返不一致总数")
    private int orderFailAlarmTotal;

}
