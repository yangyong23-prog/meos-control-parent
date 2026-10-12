package com.persagy.apccontrol.common.model.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Schema(title = "外部接口设置-冷却塔出水温度配置")
@ToString(callSuper = true)
public class OutsideCoolWaterReturnTempCfgParam {
    
    
    @Schema(description = "系统ID")
    @NotBlank(message = "系统ID不可为空")
    private String systemId;
    
    @Schema(description = "操作原因")
    @NotBlank(message = "信息点不可为空")
    private String content;
    
    @Schema(description = "冷却塔散热趋近温差ΔT")
    @NotNull(message = "冷却塔散热趋近温差ΔT不可为空")
    private BigDecimal heatDissipationTemperatureDifference;
    
    @Schema(description = "冷却塔散热趋近温差ΔT信息点,传递更新指定信息点.没传递默认更新coolWaterReturnTempSet这个信息点")
    private String infoCode;
    
}

