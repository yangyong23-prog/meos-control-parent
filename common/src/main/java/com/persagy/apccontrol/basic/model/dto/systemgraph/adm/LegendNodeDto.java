package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.persagy.apccontrol.basic.model.vo.systemgraph.LegendIconVO;
import lombok.Data;

/**
 * 设备图例信息
 *
 * @author houyuanzhi
 * @date 2023/6/1 15:47
 */
@Data
public class LegendNodeDto {
    private String oriId;

    private String name;

    private String compType;

    /**
     * 设备图例画布坐标，需要绘制
     */
    private CoordinateDto absoluteLocation;

    /**
     *
     */
    private CoordinateDto location;

    /**
     * 设备图例大小，需要绘制
     */
    private CoordinateDto size;

    private boolean hidden;

    private String props;

    /**
     * 图例信息
     */
    private LegendIconVO legendIcon;


}
