package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.MainPipeDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author houyuanzhi
 * @date 2024/1/8 17:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MainPipeVo extends MainPipeDto {

    @ApiModelProperty("管线样式编码")
    private String pipelineStyleCode;

    @ApiModelProperty("管线名称")
    private String lineName;
}
