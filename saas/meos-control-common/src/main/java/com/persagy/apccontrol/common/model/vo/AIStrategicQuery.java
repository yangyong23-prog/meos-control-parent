package com.persagy.apccontrol.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * @Description: AI策略查询参数
 * @author: yancheng
 * @date: 2023/3/18
 * @version: V1.0
 */
@Data
@Schema(title ="AI策略查询参数")
@NoArgsConstructor
@AllArgsConstructor
public class AIStrategicQuery {

    @Schema(description = "项目id" )
    @NotEmpty(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(description = "系统编号" )
    @NotEmpty(message = "{key_19d7dc7042934ab9a93f77d2ade9956a}")
    private String  systemId;
}
