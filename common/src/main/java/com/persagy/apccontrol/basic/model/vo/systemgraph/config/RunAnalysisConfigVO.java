package com.persagy.apccontrol.basic.model.vo.systemgraph.config;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.enumtype.runanalysis.AnalysisTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runanalysis.GroupTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class RunAnalysisConfigVO {

    //   "analysisType", "analysisTypeName", "firstGroup", "secondGroup", "infoCode", "name", "unit", "isSelect", "groupType", "byClassCode","equipInGroup","additionalInfoCode","condition"
    private AnalysisTypeEnum analysisType;
    private String analysisTypeName;
    private String firstGroup;
    private String secondGroup;
    private String infoCode;
    private String name;
    private String statisticalValue;
    private boolean newClassify;
    private String unit;
    // 是否默认选择
    private boolean isSelect;
    // 组类型
    private GroupTypeEnum groupType;
    // 根据ClassCode查询
    private List<String> byClassCode;
    // 组下设备的ClassCode
    private List<String> equipInGroup;
    //附加信息点，多个用,分隔
    private String additionalInfoCode;
    // 信息点条件
    private JSONObject condition;
}
