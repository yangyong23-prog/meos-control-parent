package com.persagy.apccontrol.basic.model.dto.systemgraph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateTemplateNameDTO {

    /**
     * 图类型
     */
    @NotBlank(message = "{key_071450ec14774257a1ac83b8df9eb13a}")
    @Schema(description = "图类型")
    private String graphCodeType;

    /**
     * 系统对象id
     */
    @NotBlank(message = "{key_084485a601e5437688eeacf92ba08c45}")
    @Schema(description = "对象id")
    private String objectId;

    @Schema(description = "名称")
    private String name;
}
