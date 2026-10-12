package com.persagy.apccontrol.basic.model.param.demand.airconditioning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/12/18 7:40 PM
 */
@Data
@Accessors(chain = true)
public class SpaceInfoQueryParam {

    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    @Schema(description = "项目编码")
    private String projectId;

    @Schema(description = "设备ID")
    @NotBlank(message = "{key_0b15afcfe9324b49b1c30bf75971d459}")
    private String equipmentId;
}
