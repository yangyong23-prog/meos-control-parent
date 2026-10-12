package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * 文本框
 *
 * @author houyuanzhi
 * @date 2023/8/10 17:28
 */
@Data
public class LegendTextDto {

    /**
     * 文本框code，标识位置
     */
    private String code;

    /**
     * 方向
     */
    private String direction;

    /**
     * 垂直标识，false 水平
     */
    private boolean vertical;

    /**
     * 最大长度
     */
    private int max_length_px;

    private int interval;

    private CoordinateDto offset_px;
}
