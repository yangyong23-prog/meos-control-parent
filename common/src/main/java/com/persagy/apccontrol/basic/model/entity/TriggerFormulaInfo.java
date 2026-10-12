package com.persagy.apccontrol.basic.model.entity;

import lombok.Data;

import java.util.List;

/**
 * 信息点配置中得扩展信息字段
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
@Data
public class TriggerFormulaInfo {

    private String triggerFormula;

    private String triggerInfoCode;

    // 计算类，0或null-控制指令进行中判断，1-信息点表达式的计算
    private Integer sourceIotType;

    private List<InfoCodeInfo> variables;

}
