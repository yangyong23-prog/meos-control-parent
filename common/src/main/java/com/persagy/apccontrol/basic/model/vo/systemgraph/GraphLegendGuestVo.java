package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/6/21 14:08
 */
@Data
@ApiModel("图例(对外)")
public class GraphLegendGuestVo {

    @ApiModelProperty("图例对应文件url")
    private String fileUrl;

    @ApiModelProperty("对象类code")
    private String classCode;

    @ApiModelProperty("图标名称")
    private String legendName;

}
