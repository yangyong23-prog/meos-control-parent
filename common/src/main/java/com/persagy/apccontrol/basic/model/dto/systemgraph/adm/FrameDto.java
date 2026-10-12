package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * FrameDto
 *
 * @author houyuanzhi
 * @date 2023/6/20 16:15
 */
@Data
public class FrameDto {

    /**
     * 位置
     */
    private CoordinateDto absoluteLocation;

    /**
     * 尺寸
     */
    private CoordinateDto size;
}
