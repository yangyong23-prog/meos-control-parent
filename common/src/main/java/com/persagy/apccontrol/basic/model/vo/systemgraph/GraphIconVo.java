package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.enumtype.systemgraph.LegendSourceStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author houyuanzhi
 * @date 2023/6/21 14:08
 */
@Data
@ApiModel("图标库")
public class GraphIconVo {

    @ApiModelProperty("主键id")
    private String id;

    @ApiModelProperty("图例id")
    private String legendId;

    @ApiModelProperty("图片url")
    private String fileUrl;

    @ApiModelProperty("对象类code")
    private String classCode;

    @ApiModelProperty("图例code")
    private String legendCode;

    @ApiModelProperty("图标名称")
    private String legendName;

    @ApiModelProperty("图标状态")
    private LegendSourceStatus status;

    @ApiModelProperty("条件信息")
    private String condition;

}
