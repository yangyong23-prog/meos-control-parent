package com.persagy.apccontrol.basic.model.vo.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 空间规则
 */
@Data
@Schema(title = "空间规则")
public class DemandBlacklistSpacesRuleVO {

    @Schema(description = "空间ID")
    @NotNull(message = "{key_c78c2ee89cbd491ea193306bff87f524}")
    private String spaceId;

    @Schema(description = "空间名称")
    private String spaceName;

}
