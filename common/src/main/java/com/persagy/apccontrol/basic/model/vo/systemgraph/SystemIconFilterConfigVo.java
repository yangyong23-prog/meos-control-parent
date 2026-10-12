package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/5 22:06
 */
@Data
public class SystemIconFilterConfigVo {

    @ApiModelProperty("滤镜编码")
    private String filterCode;

    @ApiModelProperty("滤镜名称")
    private String filterName;

    @ApiModelProperty("滤镜参数")
    private String filterParameter;

    @ApiModelProperty("备注")
    private String remarks;
}
