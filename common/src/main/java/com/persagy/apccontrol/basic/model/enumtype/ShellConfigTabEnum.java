package com.persagy.apccontrol.basic.model.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/8 10:30 AM
 */
@Getter
@AllArgsConstructor
public enum ShellConfigTabEnum {

    ALG_ALG("alg", "alg_alg", "算法条件输入"),
    ALG_DEMAND("alg", "alg_demand", "空间品质判断及需求跃变计算的条件输入"),
    ALG_COLD("alg", "alg_cold", "冷站自动需求计算的条件输入"),
    ALG_AIR_COLD("alg", "alg_air_cold", "空调冷源自动需求计算的条件输入"),
    ALG_COLDHOT("alg", "alg_coldhot", "冷热站自动需求计算的条件输入"),
    ALG_HOT("alg", "alg_hot", "热站自动需求计算条件输入"),
    ALG_AIR_HOT("alg", "alg_air_hot", "空调热源自动需求计算条件输入"),

    CONTROL_LOGIC("control", "control_logic", "末端控制逻辑条件输入"),
    CONTROL_SELECT("control", "control_select", "控制逻辑选机条件输入"),
    CONTROL_VENTILATION("control", "control_ventilation", "通风控制逻辑条件输入"),

    POLICY_SITE("policy", "policy_site", "冷站策略干预"),
    POLICY_DEVICE("policy", "policy_device", "冷站系统及设备控制"),
    POLICY_SITE_cold_hot("policy", "policy_site_cold_hot", "冷热站策略干预"),
    POLICY_DEVICE_cold_hot("policy", "policy_device_cold_hot", "冷热站系统及设备控制参数"),
    POLICY_END("policy", "policy_end", "末端控制干预"),

    GRAPH_EFFECT("graph", "graph_effect", "关系图动画效果显隐控制"),
    ;

    private String scene;
    private String code;
    private String name;
}
