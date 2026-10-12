package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/10 18:11
 */
@Data
public class LegendText {

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
    private int maxLengthPx;

    /**
     *
     */
    private int interval;

    private CoordinateDto offsetPx;
}
