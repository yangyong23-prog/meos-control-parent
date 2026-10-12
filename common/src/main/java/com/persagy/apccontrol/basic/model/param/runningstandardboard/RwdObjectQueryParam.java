package com.persagy.apccontrol.basic.model.param.runningstandardboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author : wangchenghong
 * @date : 2024/11/19 14:21
 */
@Data
public class RwdObjectQueryParam {

    @NotBlank(message = "{key_0caf479ba08342f29b3d3f9aa880a17c}")
    @Schema(description = "对象id")
    private String objectId;

    @NotBlank(message = "{key_66aa8ab195154d73b2fe5fe99b5c410a}")
    @Schema(description = "对象类型")
    private String objectType;



}
