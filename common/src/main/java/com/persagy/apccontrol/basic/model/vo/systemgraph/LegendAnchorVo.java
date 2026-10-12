package com.persagy.apccontrol.basic.model.vo.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/10 18:10
 */
@Data
public class LegendAnchorVo {

    /**
     * 锚点code，能指向位置
     */
    @ApiModelProperty("锚点code")
    private String code;

    private boolean multiple;

    @ApiModelProperty("锚点偏移量坐标")
    private CoordinateDto coordinateDto;
}
