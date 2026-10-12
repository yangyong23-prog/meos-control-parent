package com.persagy.apccontrol.basic.model.vo.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 所在楼层规则
 */
@Data
@Schema(title = "所在楼层规则")
@NoArgsConstructor
@AllArgsConstructor
public class DemandBlacklistInFloorRuleVO {

    @Schema(description = "所在楼层ID")
    @NotNull(message = "{key_a0927fca963c4305b6d16293cb4084f4}")
    private String floorId;

    @Schema(description = "楼层名称")
    private String floorLocalName;

    @Schema(description = "建筑 ID")
    private String buildingId;

    @Schema(description = " 建筑名称")
    private String buildingLocalName;

}
