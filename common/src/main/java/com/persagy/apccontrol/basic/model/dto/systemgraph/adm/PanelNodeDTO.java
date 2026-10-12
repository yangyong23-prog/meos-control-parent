package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import lombok.Data;

import java.util.List;

@Data
public class PanelNodeDTO {

    /**
     * 面板间隙
     */
    private Integer panelGap;
    /**
     * 大小
     */
    private CoordinateDto size;
    /**
     * 面板边缘
     */
    private String panelEdge;
    /**
     * 面板排列
     */
    private String panelAlign;
    /**
     * 绝对位置
     */
    private CoordinateDto absoluteLocation;

    /**
     * 信息点列表
     */
    private List<RcInfoCodeCfg> infoCodes;

}
