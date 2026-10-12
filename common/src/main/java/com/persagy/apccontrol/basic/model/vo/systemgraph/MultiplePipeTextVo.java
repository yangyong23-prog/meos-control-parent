package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/10/30 11:33
 */
@Data
public class MultiplePipeTextVo extends PipelineStyleConfig{

    @ApiModelProperty("倍数")
    @ExcelProperty(index = 0)
    private Integer multiple;

    @ApiModelProperty("多倍下线芯宽度")
    @ExcelProperty(index = 1)
    private Integer wireCoreWidth;

    @ApiModelProperty("多倍下底色宽度")
    @ExcelProperty(index = 2)
    private Integer backgroundWidth;

    @ApiModelProperty("多倍下描边宽度")
    @ExcelProperty(index = 3)
    private String strokeWidth;

    @ApiModelProperty("多倍下文字大小")
    @ExcelProperty(index = 4)
    private String text;

    @ApiModelProperty("gap")
    @ExcelProperty(index = 5)
    private String gap;
}
