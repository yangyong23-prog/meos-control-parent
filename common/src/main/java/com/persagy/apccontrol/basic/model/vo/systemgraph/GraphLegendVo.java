package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.LegendSourceStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author houyuanzhi
 * @date 2023/6/21 14:08
 */
@Data
@ApiModel("图例")
public class GraphLegendVo {

    @ApiModelProperty("图例id")
    private String legendId;

    @ApiModelProperty("图例对应文件url")
    private String fileUrl;

    @ApiModelProperty("对象类code")
    private String classCode;

    @ApiModelProperty("图例code")
    private String legendCode;

    @ApiModelProperty("对象类类型")
    private String objType;

    @ApiModelProperty("图标名称")
    private String legendName;

    /**
     * 是否是小附件
     */
    @ApiModelProperty("是否是小附件")
    private boolean attachType;

    @ApiModelProperty("是否可用")
    private boolean valid;

    @ApiModelProperty("图标状态")
    private LegendSourceStatus status;

    /**
     * 文本框
     */
    @ApiModelProperty("文本框")
    private List<LegendTextVo> texts;

    /**
     * 尺寸
     */
    private CoordinateDto size;

    /**
     * 锚点
     */
    @ApiModelProperty("锚点")
    private List<LegendAnchorVo> anchors;


    @ApiModelProperty("图标列表")
    private List<GraphIconVo> iconVoList;


}
