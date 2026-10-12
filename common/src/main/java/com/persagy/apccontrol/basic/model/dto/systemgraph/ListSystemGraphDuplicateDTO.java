package com.persagy.apccontrol.basic.model.dto.systemgraph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ListSystemGraphDuplicateDTO {

//    /**
//     * 模板id
//     */
//    @NotBlank(message = "模板id不可为空")
//    @Schema(description = "模板id")
//    private String templateId;

    /**
     * 系统对象id
     */
    @NotBlank(message = "{key_7642c31e05f64f57b54b36a29f654927}")
    @Schema(description = "系统对象id")
    private String systemObjectId;

    @NotBlank(message = "{key_071450ec14774257a1ac83b8df9eb13a}")
    @Schema(description = "图类型")
    private String graphTypeCode;
}
