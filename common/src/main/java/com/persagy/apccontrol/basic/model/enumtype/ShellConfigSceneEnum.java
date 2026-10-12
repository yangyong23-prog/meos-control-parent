package com.persagy.apccontrol.basic.model.enumtype;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ShellConfigSceneEnum {

    SCENE_ALG("alg", "自动化需求计算条件输入", "算法生成策略需要的参数，算法基于输入的壳范围进行策略的生成；修改后次日生效"),
    SCENE_CONTROL("control", "控制逻辑条件输入", "控制逻辑需要的参数，底层控制逻辑会基于输入的壳范围进行设备的控制；修改后即时生效"),
    SCENE_SPACE("space", "空间需求输入条件", "针对需求管理业务相关的需求输入的条件；修改后即时生效"),
    SCENE_POLICY("policy", "策略需求目标及控制参数前端输入条件", "针对在控制业务中，对各类前端需要输入参数的输入条件；修改后即时生效"),
    SCENE_MPC("mpc", "MPC智能体配置", "报告配置；修改后即时生效"),

    SCENE_WEATHER("weather", "室外天气数据来源输入", "冷站或者冷热站系统页面的室外天气数据取值来源切换；修改后即时生效"),
    SCENE_EFF("eff", "效率指标配置", "效率指标配置；修改后即时生效"),
    SCENE_GRAPH("graph", "关系图配置", "关系图配置；修改后即时生效"),
    SCENE_HSEC("hsec", "半剖图配置", "半剖图配置；修改后即时生效"),
    SCENE_REPORT("report", "报告配置", "报告配置；修改后即时生效"),
    SCENE_WHITELIST_SPACE("whitelist_space", "空间白名单配置", "白名单配置；修改后即时生效"),
    SCENE_PERSISTENT_DATA_RULE("persistent_data_rule", "云端持久化数据显示规则", "云端持久化数据的查询逻辑配置；修改后即时生效"),

    ;

    private String code;
    private String name;
    private String desc;

    public static ShellConfigSceneEnum getByCode(String code) {
        for (ShellConfigSceneEnum value : ShellConfigSceneEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据壳配置场景获取对应的基础配置类型
     *
     * @param scene
     * @return
     */
    public static BasicsConfigTypeEnum convertToBasicConfigTypeByScene(String scene) {
        ShellConfigSceneEnum sceneEnum = null;
        for (ShellConfigSceneEnum shellConfigSceneEnum : ShellConfigSceneEnum.values()) {
            if (shellConfigSceneEnum.code.equals(scene)) {
                sceneEnum = shellConfigSceneEnum;
            }
        }
        switch (sceneEnum) {
            case SCENE_ALG:
                return BasicsConfigTypeEnum.SHELL_CONFIG_ALG;
            case SCENE_CONTROL:
                return BasicsConfigTypeEnum.SHELL_CONFIG_CONTROL;
            case SCENE_SPACE:
                return BasicsConfigTypeEnum.SHELL_CONFIG_SPACE;
            case SCENE_POLICY:
                return BasicsConfigTypeEnum.SHELL_CONFIG_POLICE;
            case SCENE_WEATHER:
                return BasicsConfigTypeEnum.SHELL_CONFIG_WEATHER;
            case SCENE_EFF:
                return BasicsConfigTypeEnum.SHELL_CONFIG_EFF;
            case SCENE_GRAPH:
                return BasicsConfigTypeEnum.SHELL_CONFIG_GRAPH;
            case SCENE_HSEC:
                return BasicsConfigTypeEnum.SHELL_CONFIG_HSEC;
            case SCENE_REPORT:
                return BasicsConfigTypeEnum.SHELL_CONFIG_REPORT;
            case SCENE_MPC:
                return BasicsConfigTypeEnum.SHELL_CONFIG_MPC;
            case SCENE_WHITELIST_SPACE:
                return BasicsConfigTypeEnum.SCENE_CONFIG_WHITELIST_SPACE;
            case SCENE_PERSISTENT_DATA_RULE:
                return BasicsConfigTypeEnum.SHELL_CONFIG_PERSISTENT_DATA_RULE;
            default:
                return null;
        }
    }
}
