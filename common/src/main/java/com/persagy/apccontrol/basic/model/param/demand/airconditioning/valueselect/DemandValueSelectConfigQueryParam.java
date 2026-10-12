package com.persagy.apccontrol.basic.model.param.demand.airconditioning.valueselect;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 需求管理-价值选择-类型配置查询参数
 */
@Data
@ToString
@Schema(title = "需求管理-价值选择-类型配置查询参数")
public class DemandValueSelectConfigQueryParam {
    
    @Schema(description = "开始日期")
    @NotNull(message = "{key_6dc6cc33082c41d189980dd415b06083}")
    private LocalDate startDate;
    @Schema(description = "结束日期")
    @NotNull(message = "{key_fe10d89294244a90a5a3d488db40d202}")
    private LocalDate endDate;
    
}
