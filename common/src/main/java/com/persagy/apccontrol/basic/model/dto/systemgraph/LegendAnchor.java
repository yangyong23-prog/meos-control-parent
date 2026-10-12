package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/10 18:10
 */
@Data
public class LegendAnchor {

    /**
     * 锚点code，能指向位置
     */
    private String code;

    private boolean multiple;

    /**
     * 锚点偏移量坐标
     */
    private CoordinateDto coordinateDto = new CoordinateDto();
}
