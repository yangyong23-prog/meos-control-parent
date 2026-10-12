package com.persagy.apccontrol.basic.model.base;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author yuyao
 * @Date 2022/3/2 2:05 PM
 * @Description 配置类型枚举
 */
@Getter
@AllArgsConstructor
public enum BasicsConfigTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 配置
     */
    EXPERT_RULES("expert_rules", "key_f6a92f267adb4bf18a84e50ce5d7cb07", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    HUMITURE_SHIELD("humiture_shield", "key_48e4f22fac2d472e8fbfd382dfed9b75", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    HUMITURE_TARGET("humiture_target", "key_322e973925eb456cb66dd786578b379a", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    RUNNING_MODE("running_mode", "key_58ca4a0381124985ab2dc9e815efe116", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    INO_ENVIRONMENT("ino_environment", "key_7181e2a4187144989fca416a0478e896", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    DEMAND_CONTROL_OBJECT("demand_control_object", "key_9ff2e74d597b47c79bf03d734703e286", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    DEMAND_LIGHTING_OBJECT_STRATEGY_TYPE("demand_lighting_object_strategy_type", "key_0d32d3a9f4c14b1587f52112999303d0", "key_d9d522edaab94869b33c1eab2e60b6a7"),

    //页面播报数模板配置类型
    PB_AUTO_ENUM("auto_set_enum_value", "key_1cb6146be84c413594950363af571c09", ""),
    PB_AUTO_NO_ENUM("auto_set_no_enum_value", "key_2b3103c7e7154db89d60c29c7e9077ec", ""),
    PB_MANUAL_ENUM("manual_set_enum_value", "key_d9515118e9184b33acbb90213ad799da", ""),
    PB_MANUAL_NO_ENUM("manual_set_no_enum_value", "key_765d449b92214deaa90f41c9beb84aac", ""),
    PB_MANUAL_CHILLER_ENUM("manual_chiller_set_enum_value", "key_9ba7241c010e48dbae46335578989f97", ""),
    PB_MANUAL_CHILLER_NO_ENUM("manual_chiller_set_no_enum_value", "key_e46aa8a01ad448ef88e2a505891b50c8", ""),
    PB_MANUAL_LASTPUMB_ENUM("manual_lastpumb_set_enum_value", "key_a65ed1affdc74b32b86812e334e8d5b9", ""),
    PB_MANUAL_LASTPUMB_NO_ENUM("manual_lastpumb_set_no_enum_value", "key_c2c8adb1f5f142c19dc0e9b48e5d7b3f", ""),
    PB_SWITCH_TO_MANUAL("switch_to_manual", "key_3a52b7061b704b4c98294dd741c77a04", ""),
    PB_SWITCH_TO_AUTO("switch_to_auto", "key_b38de5f5086847b69fe433066e4e8fa7", ""),
    PB_SWITCH_EQ_TO_MANUAL("switch_eq_to_manual", "key_2873b06c234540648caa6a922d2c329f", ""),
    PB_SWITCH_EQ_TO_AUTO("switch_eq_to_auto", "key_6d7d56cb35d042cca2167843779d2996", ""),

    //价值选择
    VALUE_SELECT_APPLY("value_select_apply", "key_6d2028c1ae3d459d9c9ae7a205568b2f", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    VALUE_SELECT_LOWEST_CAST("value_select_lowest_cast", "key_0806c1f458ad431f8757869fef33c38f", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    VALUE_SELECT_LOWEST_CARBON_EMISSIONS("value_select_Lowest_carbon_emissions", "key_3b2847eabd07485989125de7f39d7c7d", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    VALUE_SELECT_LOWEST_ENERGY("value_select_lowest_energy", "key_7574d8e1423c467388d269b965635720", "key_d9d522edaab94869b33c1eab2e60b6a7"),
    VALUE_SELECT_QUALITY_FIRST("value_select_quality_first", "key_f37e4b313e4f4b698380f7f87b04ed56", "key_d9d522edaab94869b33c1eab2e60b6a7"),

    //壳范围配置
    SHELL_CONFIG_ALG("shell_config_alg","key_693ef1a9b9184c069198b33c96a25fcd", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_CONTROL("shell_config_control","key_13ee4ad457e54de4a6665def2420998f", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_SPACE("shell_config_space","key_bb1d999bf14f4afc8b01e8390c97e5fa", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_POLICE("shell_config_policy","key_7c544e53ee7b4acfa5d306b61acf6737", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_WEATHER("shell_config_weather","key_1fb292f314fd4f548dbaed9cc2edccf6", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_EFF("shell_config_eff","key_caf91bf8364b42ec9531611bd2e403b1", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_GRAPH("shell_config_graph","key_shell_config_graph", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_HSEC("shell_config_hsec","key_shell_config_hsec", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_REPORT("shell_config_report","key_shell_config_report", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_MPC("shell_config_mpc","key_shell_config_mpc", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SCENE_CONFIG_WHITELIST_SPACE("shell_whitelist_space", "key_shell_whitelist_space", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    SHELL_CONFIG_PERSISTENT_DATA_RULE("shell_config_persistent_data_rule", "key_shell_config_persistent_data_rule", "key_f64a1fbe41b54eb891c43d52e1ff6355"),


    ROOM_TEMP_RANGE("room_temp_range","key_55049936691f4a0dbd9e915b980ef386", "key_f64a1fbe41b54eb891c43d52e1ff6355"),

    STRATEGY_TYPE("strategy_type","key_87660a2717474b2d85b1137932c0b9f7", "key_5dbd81a6ba5c4853bbd1ce63d9b6f3d3"),
    
    MPC_AGENT_CFG("MPC_AGENT_CFG", "MPC智能体配置", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    //空间运行偏差
    SPACE_DEFICIENCY_CONFIG("space_deficiency_config", "key_5a74769414224b16b39e3cbb47171c35", ""),

    REGISTER_ALGORITHM_CONFIG("register_algorithm_config","key_676021d5aaed4d78875fca4e86f15775", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    ENABLE_ALGORITHM_CONFIG("enable_algorithm_config","key_6e981a754cfe4f448abed12912159409", "key_f64a1fbe41b54eb891c43d52e1ff6355"),

    //时序大模型配置
    ENABLE_LTSM_SPACE_CONFIG("enable_ltsm_space_config","key_9834fbb066544598bcd09c2682ba0818", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    ENABLE_LTSM_COOLING_CONFIG("enable_ltsm_cooling_config","key_15c9b48a33914741a7b107105f069382", "key_f64a1fbe41b54eb891c43d52e1ff6355"),
    ENABLE_LTSM_PUMP_CONFIG("enable_ltsm_pump_config","key_2f92ca1d76e24a9e9327b0d0d539f7e9", "key_f64a1fbe41b54eb891c43d52e1ff6355"),

    // 冷热站临时测试用
    SYSTEM_SHELL_CONFIG_TEST_TEMP("system_shell_config_test_temp","key_e784c36f703d44c3b3c941acc9b74e6a", "key_f64a1fbe41b54eb891c43d52e1ff6355"),

    // 强控状态
    FORCE_CONTROL_STATUS("forceControlStatus","key_3c080ada63d14d67b61a24e6efe4b21a", "key_93665b7a2f6b4feab10bd965dc287d88"),

    /**
     * 缓存对象模型状态（内部配置，不对普通基础配置入口展示）。
     *
     * @since v7.260701.0
     */
    CACHE_OBJECT_MODEL_STATUS("CACHE_OBJECT_MODEL_STATUS", "", ""),
    ;

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String explain;
    /**
     * 作用边界 系统级别还是集团级别
     */
    private final String boundary;

    /**
     * 获取价值选择类型
     *
     * @return 价值选择类型
     * @since 7.250102.0
     */
    public static List<BasicsConfigTypeEnum> listValueSelectTypes() {
        return Lists.newArrayList(
                VALUE_SELECT_LOWEST_CAST,
                VALUE_SELECT_LOWEST_CARBON_EMISSIONS,
                VALUE_SELECT_LOWEST_ENERGY,
                VALUE_SELECT_QUALITY_FIRST);
    }

    public static BasicsConfigTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (BasicsConfigTypeEnum i : BasicsConfigTypeEnum.values()) {
            if (code.equals(i.getCode())) {
                return i;
            }
        }
        return null;
    }


    public static List<BizStaticTypeVO> getList() {

        List<BizStaticTypeVO> list = ListUtil.toList();

        for (BasicsConfigTypeEnum i : BasicsConfigTypeEnum.values()) {
            if (BasicsConfigTypeEnum.CACHE_OBJECT_MODEL_STATUS.equals(i)) {
                continue;
            }
            list.add(new BizStaticTypeVO(String.valueOf(i.getCode()), i.getExplain()));
        }
        return list;
    }

    @Override
    public String getValue() {
        return code;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return null;
    }

    public String getExplain() {
        return LocaleMessageUtil.getMessage(explain);
    }

    public String getBoundary() {
        return LocaleMessageUtil.getMessage(boundary);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static BasicsConfigTypeEnum resolve(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return BaseEnum.analyze(value, BasicsConfigTypeEnum.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
