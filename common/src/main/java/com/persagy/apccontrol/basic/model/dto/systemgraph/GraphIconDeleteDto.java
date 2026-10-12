package com.persagy.apccontrol.basic.model.dto.systemgraph;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/11 16:08
 */
@Data
public class GraphIconDeleteDto {

    @ApiModelProperty(value = "图列id，可能包含多个图标")
    private String legendId;

    @ApiModelProperty("设备类编码")
    private String classCode;

    @ApiModelProperty("图标名称")
    private String legendName;



}
