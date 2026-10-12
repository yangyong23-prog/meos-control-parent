package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

import java.util.List;

/**
 * 图例详情数据
 *
 * @author houyuanzhi
 * @date 2023/8/10 17:27
 */
@Data
public class GraphLegendContentDto {

    /**
     * 文本框
     */
    private List<LegendTextDto> texts;

    /**
     * 尺寸
     */
    private CoordinateDto size;

    /**
     * 锚点
     */
    private List<LegendAnchorDto> anchors;
}
