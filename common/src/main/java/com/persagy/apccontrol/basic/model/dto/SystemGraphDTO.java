package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/31 10:18
 */
@Getter
@Setter
@ToString
public class SystemGraphDTO {

    @NotBlank(message = "{key_61bdc1ef7fa249738155e48dcb89f5c8}")
    @Schema(description = "项目编码")
    private String projectId;

    @Schema(description = "图纸ID")
    @NotBlank(message = "{key_1f290d98c1c848ccb5fb615c8336f801}")
    private String systemObjectId;

    @Schema(description = "图纸编码")
    @NotBlank(message = "{key_5cf8dcee611c450f926f0a183df2914f}")
    private String classCode;

    @Schema(description = "图纸类型编码")
    private String graphTypeCode;

}
