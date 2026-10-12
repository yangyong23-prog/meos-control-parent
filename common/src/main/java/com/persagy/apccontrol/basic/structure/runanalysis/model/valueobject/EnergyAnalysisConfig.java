package com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject;

import lombok.Data;

import java.util.List;

/**
 * @Author weiyizhong
 * @Date 2026/2/3 11:20
 * @Company yushu.biz
 * @Description: 能耗-分析配置
 */
@Data
public class EnergyAnalysisConfig extends RunAnalysisReportConfig{

    /**
     * 模型ID
     */
    private String selectedModelId;

    /**
     * 选中的树节点ID
     */
    private List<String> selectedTreeIds;

    /**
     * 配置树数据
     */
    private List<EnergyAnalysisTreeConfigTree> treeData;

}
