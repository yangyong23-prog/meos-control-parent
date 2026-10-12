package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * 图标锚点
 *
 * @author houyuanzhi
 * @date 2023/8/10 17:36
 */
@Data
public class LegendAnchorDto {

    /**
     * 锚点code，能指向位置
     */
    private String code;

    private boolean multiple;
}
