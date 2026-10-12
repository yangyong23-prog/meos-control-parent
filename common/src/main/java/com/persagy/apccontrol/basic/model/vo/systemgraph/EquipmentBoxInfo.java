package com.persagy.apccontrol.basic.model.vo.systemgraph;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author wangPing
 * @date 2023-08-28
 **/
@Data
public class EquipmentBoxInfo {

    @ApiModelProperty("格子位置")
    private Integer boxIndex;
    @ApiModelProperty("数量减半标识")
    private Boolean halveFlag;
}
