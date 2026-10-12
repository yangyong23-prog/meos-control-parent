package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2024/1/8 17:13
 */
@Data
public class AreaStyleConfigVo extends PipelineStyleConfig{

    @ApiModelProperty("区域样式编码")
    @ExcelProperty(index = 0)
    private String areaStyleCode;

    @ApiModelProperty("类型")
    @ExcelProperty(index = 1)
    private int type;

    @ApiModelProperty("底色颜色")
    @ExcelProperty(index = 2)
    private String backgroundColor;

    @ApiModelProperty("底色透明度")
    @ExcelProperty(index = 3)
    private int opacity;

    @ApiModelProperty("线芯线型:1-实线 2-虚线 0-其他")
    @ExcelProperty(index = 4)
    private int lineType;

    @ApiModelProperty("线芯线型参数")
    @ExcelProperty(index = 5)
    private String linePara;

    @ApiModelProperty("轮廓线颜色")
    @ExcelProperty(index = 6)
    private String lineColor;

    @ApiModelProperty("轮廓线宽度")
    @ExcelProperty(index = 7)
    private int lineWidth;

    @ApiModelProperty("圆角")
    @ExcelProperty(index = 8)
    private int fillet;

    @ApiModelProperty("备注")
    @ExcelProperty(index = 9)
    private String notes;
}
