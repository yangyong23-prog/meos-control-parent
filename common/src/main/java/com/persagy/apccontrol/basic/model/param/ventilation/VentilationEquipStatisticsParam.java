package com.persagy.apccontrol.basic.model.param.ventilation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

/**
 * 通风设备统计参数
 * 无参数查询项目级别的数据
 */
@Data
public class VentilationEquipStatisticsParam {
    
    @Schema(description = " 过滤所在楼层ID")
    private Set<String> filterFloorIds;
    
}
