package com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Author weiyizhong
 * @Date 2026/2/3 11:27
 * @Company yushu.biz
 * @Description: 能耗分析配置树
 */
@Data
public class EnergyAnalysisTreeConfigTree {

    /**
     * 是否选中
     */
    private String checked;

    /**
     * 能耗分项id
     */
    private String id;

    /**
     * 能耗分项名称
     */
    private String itemName;

    /**
     * 能耗分项编码
     */
    private String itemCode;

    /**
     * bdtp实例id
     */
    private String rwdId;


    @Schema(description ="子节点")
    private List<EnergyAnalysisTreeConfigTree> treeSourceNodeChildren;


}
