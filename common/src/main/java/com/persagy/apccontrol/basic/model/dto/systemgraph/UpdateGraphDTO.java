package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumUpdateGraphType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateGraphDTO {

    /**
     * 对象id
     */
    @NotBlank(message = "{key_084485a601e5437688eeacf92ba08c45}")
    @Schema(description = "对象id")
    private String objectId;

    /**
     * 编码
     */
    @NotBlank(message = "{key_6e47cb14c3bd423793a3dcbce8ca4a15}")
    @Schema(description = "编码")
    private String code;

    /**
     * 数据
     */
    @Schema(description = "数据")
    private Object data;

    @Schema(description = "类型:0设备，1小附件，2设备组，3前端自定义数据")
    private EnumUpdateGraphType type;
}
