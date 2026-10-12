package com.persagy.apccontrol.basic.model.param.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 保存配置
 *
 * @author LiuYang
 * @since 5.240602.0
 */
@Data
@Accessors(chain = true)
public class CommonConfigSaveParam {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "配置")
    @NotBlank(message = "{key_52056cc794e44f7a8e17e74bde963cde}")
    private String config;

    @Schema(description = "标签")
    @NotBlank(message = "{key_ce3072abba2e4860b3f244f3f32f1194}")
    private String tag;

    @Schema(description = "备注")
    private String remark;

}
