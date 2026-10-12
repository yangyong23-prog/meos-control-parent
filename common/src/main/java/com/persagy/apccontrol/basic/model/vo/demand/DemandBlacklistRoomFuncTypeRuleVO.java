package com.persagy.apccontrol.basic.model.vo.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 空间功能类型规则
 */
@Data
@Schema(title = "空间功能类型规则")
public class DemandBlacklistRoomFuncTypeRuleVO {

    @Schema(description = "空间功能类型")
    @NotNull(message = "{key_da2f29612b404d2d9503d93e5ecbadeb}")
    private String roomFuncType;

    @Schema(description = "空间功能类型名称")
    private String roomFuncTypeName;

}
