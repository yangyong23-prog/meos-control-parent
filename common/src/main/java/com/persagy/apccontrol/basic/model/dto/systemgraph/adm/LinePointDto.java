package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * 连线的起点或终点数据s
 *
 * @author houyuanzhi
 * @date 2023/6/20 17:10
 */
@Data
public class LinePointDto {

    /**
     * 类型：equipmentNode 设备图例、mainPipe 管
     */
    private String hostType;

    /**
     * 设备图例/管的id
     */
    private String hostId;

    /**
     * 锚点类型
     */
    private String anchorCode;
    private String key2;
    private String key;
    private CoordinateDto locationOnEdge;


}
