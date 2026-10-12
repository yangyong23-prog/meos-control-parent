package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Schema(description = "需求价值选择配置")
@NoArgsConstructor
@AllArgsConstructor
public class DemandValueSelectConfigVO {
    
    @Schema(description = "日期")
    private LocalDate date;
    
    @Schema(description = "价值选择类型")
    private BasicsConfigTypeEnum type;
    
}
