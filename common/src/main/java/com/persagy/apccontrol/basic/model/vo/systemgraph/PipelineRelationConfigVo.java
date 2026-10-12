package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/6 21:12
 */
@Data
@ApiModel("系统图管线样式配置")
public class PipelineRelationConfigVo extends PipelineStyleConfig{

    @ApiModelProperty("专业编码")
    @ExcelProperty(index = 0)
    private String majorCode;

    @ApiModelProperty("专业名称")
    @ExcelProperty(index = 1)
    private String majorName;

    @ApiModelProperty("管道类编码")
    @ExcelProperty(index = 2)
    private String pipelineCode;

    @ApiModelProperty("管道组编码2")
    @ExcelProperty(index = 3)
    private String pipelineCode2;

    @ApiModelProperty("管道组名称")
    @ExcelProperty(index = 4)
    private String pipelineName;

    @ApiModelProperty("图类型编码")
    @ExcelProperty(index = 5)
    private String graphCode;

    @ApiModelProperty("图类型名称")
    @ExcelProperty(index = 6)
    private String graphName;

    @ApiModelProperty("边类型编码")
    @ExcelProperty(index = 7)
    private String relCode;

    @ApiModelProperty("边类型名称")
    @ExcelProperty(index = 8)
    private String relName;

    @ApiModelProperty("是否是立管")
    @ExcelProperty(index = 9)
    private String riserMark;

    @ApiModelProperty("管线样式")
    @ExcelProperty(index = 10)
    private String pipelineStyleCode;

    @ApiModelProperty("主色")
    @ExcelProperty(index = 11)
    private String majorColor;

    @ApiModelProperty("管道名称")
    @ExcelProperty(index = 12)
    private String pipeName;


}
