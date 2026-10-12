package com.persagy.apccontrol.basic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Description: 项目参数
 * @author: yancheng
 * @date: 2023/3/18
 * @version: V1.0
 */
@Data
@Schema(title = "项目参数")
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO {

    @Schema(description = "项目id")
    @NotBlank(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;
}
