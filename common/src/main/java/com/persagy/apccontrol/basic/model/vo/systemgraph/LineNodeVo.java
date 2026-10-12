package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管道节点
 * @author houyuanzhi
 * @date 2023/6/28 17:11
 */
@Data
@ApiModel(value = "管道节点（二通三通）")
public class LineNodeVo {

    /**
     * 节点坐标
     */
    @ApiModelProperty("节点坐标")
    private CoordinateDto coordinateDto;

    /**
     * 管道标记（上）
     */
    @ApiModelProperty("管道标记（上）")
    private boolean lineMarkUp = false;

    /**
     * 管道线条样式（上）
     */
    @ApiModelProperty("管道线条样式（上）")
    private String pipelineStyleCodeUp;

    /**
     * 管道标记（下）
     */
    @ApiModelProperty("管道标记（下）")
    private boolean lineMarkDown = false;

    /**
     * 管道线条样式（下）
     */
    @ApiModelProperty("管道线条样式（下）")
    private String pipelineStyleCodeDown;

    /**
     * 管道标记（左）
     */
    @ApiModelProperty("管道标记（左）")
    private boolean lineMarkLeft = false;

    /**
     * 管道线条样式（左）
     */
    @ApiModelProperty("管道线条样式（左）")
    private String pipelineStyleCodeLeft;

    /**
     * 管道标记（右）
     */
    @ApiModelProperty("管道标记（右）")
    private boolean lineMarkRight = false;

    /**
     * 管道线条样式（右）
     */
    @ApiModelProperty("管道线条样式（右）")
    private String pipelineStyleCodeRight;

}
