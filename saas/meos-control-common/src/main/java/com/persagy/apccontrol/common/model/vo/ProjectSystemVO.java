package com.persagy.apccontrol.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * @Description: 项目
 * @author: yancheng
 * @date: 2023/3/25
 * @version: V1.0
 */
@Data
@Schema(title = "项目")
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSystemVO {

    @Schema(description = "项目id" )
    @NotEmpty(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(description = "系统id" )
    @NotEmpty(message = "{key_6f0b847a0a014b648f42bd3f35360678}")
    private String systemId;
}
