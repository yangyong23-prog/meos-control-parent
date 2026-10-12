package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/1/29 15:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointInfoDTO {

    @Schema(description = "信息点")
    @NotBlank(message = "{key_461eddeb31ae47fdac633efa4cccd6e7}")
    private String infoCode;

    @Schema(description = "设定值")
    //@NotBlank(message = "设定值不可为空")
    private String value;

    @Schema(description = "操作原因")
    private String content;

    @Schema(description = "修改前内容")
    private String beforeValue;

    @Schema(description = "扩展信息")
    private ExtendSetting extendSetting;


}
