package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.LineDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author houyuanzhi
 * @date 2023/8/8 11:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LineVo extends LineDto {

    @ApiModelProperty("管线样式编码")
    private String pipelineStyleCode = "line00";

    @ApiModelProperty("管线名称")
    private String lineName;
}
