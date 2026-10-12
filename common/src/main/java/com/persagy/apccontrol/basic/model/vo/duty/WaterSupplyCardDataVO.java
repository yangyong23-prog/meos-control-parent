package com.persagy.apccontrol.basic.model.vo.duty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 给排水卡片数据
 * @author xiaofengwang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WaterSupplyCardDataVO extends ChildCardDataVO {
    @Schema(description = "液位报警信号")
    private Integer levelAlarm;

    @Schema(description = "类型编码")
    private String classCode;
}
