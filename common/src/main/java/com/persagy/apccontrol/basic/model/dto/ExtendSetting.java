package com.persagy.apccontrol.basic.model.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.persagy.apccontrol.basic.model.enumtype.ManualAutoSetEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 信息点扩展配置信息
 *
 * @author yuecaipu
 * @date 2024/8/5 18:01
 * @since v5.240801.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtendSetting {

    @Schema(description = "手自动状态 0-人工 1-自动")
    @JSONField(serializeUsing  = ManualAutoSetEnum.ManualAutoSetSerializer.class, deserializeUsing  = ManualAutoSetEnum.ManualAutoSetDeserializer.class)
    private ManualAutoSetEnum manualAutoSet;

    @Schema(description = "冷却塔散热趋近温差ΔT")
    private BigDecimal heatDissipationTemperatureDifference;

    @Schema(description = "冷却塔出水温度最小值 默认值 22")
    private BigDecimal coolWaterReturnTempMinValue = BigDecimal.valueOf(22);
    
    //推送相关字段信息
    @Schema(description = "干预结束时间")
    private String interfereEndTime;
    
    @Schema(description = "空调能力设定")
    private String airConditioningCapacitySet;

    @Schema(description = "运行模式设定")
    private String runModeSet;

}
