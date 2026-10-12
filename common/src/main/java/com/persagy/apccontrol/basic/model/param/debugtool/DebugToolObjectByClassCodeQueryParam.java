package com.persagy.apccontrol.basic.model.param.debugtool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 调试工具-根据设备类查询对象参数
 *
 * @author JiYinchuan
 * @since 7.250102.0
 */
@Data
@Schema(title = "调试工具-根据设备类查询对象参数")
public class DebugToolObjectByClassCodeQueryParam {

    @Schema(description = "设备类编码")
    @NotBlank(message = "{key_8ed7086a90aa4ce3be81d08bacfa7340}")
    private String classCode;

}
