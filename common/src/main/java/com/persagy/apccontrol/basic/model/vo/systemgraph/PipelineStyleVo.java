package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/6 21:18
 */
@Data
@ApiModel("管线样式")
public class PipelineStyleVo extends PipelineStyleConfig{

    @ApiModelProperty("线样式编码")
    @ExcelProperty(index = 0)
    private String pipelineStyleCode;

    @ApiModelProperty("类型")
    @ExcelProperty(index = 1)
    private int type;

    @ApiModelProperty("线芯宽度")
    @ExcelProperty(index = 2)
    private int wireCoreWidth;

    @ApiModelProperty("线芯颜色")
    @ExcelProperty(index = 3)
    private String wireCoreColor;

    @ApiModelProperty("线芯线型:1-实线 2-虚线 3-自定义")
    @ExcelProperty(index = 4)
    private int wireCoreType;

    @ApiModelProperty("线芯线型参数")
    @ExcelProperty(index = 5)
    private String wireCorePara;

    @ApiModelProperty("底色宽度")
    @ExcelProperty(index = 6)
    private int backgroundWidth;

    @ApiModelProperty("底色颜色")
    @ExcelProperty(index = 7)
    private String backgroundColor;

    @ApiModelProperty("描边宽度")
    @ExcelProperty(index = 8)
    private String strokeWidth;

    @ApiModelProperty("描边颜色")
    @ExcelProperty(index = 9)
    private String strokeColor;

}
