package com.persagy.apccontrol.basic.model.enumtype.system;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.base.BizStaticTypeVO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Description: 操作类型
 * @author: yancheng
 * @date: 2023/3/16
 * @version: V1.0
 */
@Getter
@AllArgsConstructor
public enum OperateTypeEnum implements BaseEnum<String>, IEnum<String> {
    /** 运行监测 v5.6.0 */
    RUNNING_CHANGE_REASON_URGENCY("00", "key_35467067fbb14929967637819356aa3f"),
    RUNNING_CHANGE_REASON_WB("10", "key_7e76a6d17e3a4dad98db76f3606bbdf7"),
    RUNNING_CHANGE_REASON_NEED("15", "key_9e989a81f7774296915c87024d2f636e"),
    RUNNING_CHANGE_REASON_FAULT("20", "key_4ba124fd89e24741aea24dadb152a910"),
    RUNNING_CHANGE_REASON_COOL("30", "key_58008c5710d349daad3448af24cc2ce7"),
    RUNNING_CHANGE_REASON_EQUIP_ARGS("40", "key_2191a0074b694c47b528a410bb69b7e2"),
    RUNNING_CHANGE_REASON_OTHER("50", "key_7d6652fc005d47b3854cdc48b0b5d4f7"),

    RUNNING_LINK_SINGLE_CONTROL("running_link_single_control", "key_713663d22b7f4a1aaf072b8ca4f1aa98"),
    RUNNING_SINGLE_POINT_CONTROL("running_single_point_control", "key_870eafbde5ec457dbe2ce2e6f6c32292"),
    RUNNING_CHILLER_CONTROL("running_chiller_control", "key_e59734e41df04af6b9b53f25809f97b0"),
    RUNNING_LASTPUMP_CONTROL("running_lastpumb_control", "key_0610b94572644b40bd00f8eb81407ba4"),
    RUNNING_WEATHER_SOURCE_CHANGE("running_weather_source_change", "key_8d2edaa68e944231b1217fea4628b428"),
    PAGE_BROADCAST_ALARM("page_broadcast_alarm", "key_3e971987c1b449c48971c4b42adca708"),
    PAGE_BROADCAST_AUTO("page_broadcast_auto", "key_ea047cab9cb1441bb3eae8e921640f49"),
    PAGE_BROADCAST_MANUAL("page_broadcast_manual", "key_bb27ee4a341d4eadbd2fa113d4c2d6d4"),

    /**
     * 异步任务
     */
    DS("DS", "key_b6677c6a0c6a4272917d5ee70c5b6507"),

    /**
     * 系统运行设置
     */
    INO_ENVIRONMENT("ino_environment", "key_a64c908e313540bfa8129ae551382815"),
    RUN_TIME("run_time", "key_b78dbe1e1f61423582319a53eaa678d9"),
    EXPERT_RULES("expert_rules", "key_89178db6c30f490992b9be9ddc27a2c9"),
    /**
     * 运行调节记录
     */
    EQ_LOCK("eq_lock", "key_b0a18d5b661a460e89b08a6d6b1bca57"),
    EQ_LOCK_RELIEVE("eq_lock_relieve", "key_5f8074e7152d433ab7bfcbe57183fa38"),
    PARAMETER_ADJUST("parameter_adjust", "key_34d427081a134c0eba7b5234f63c38bf"),
    EQ_FORCED_OPEN("eq_forced_open", "key_1245f9ec07174255a2521f3c2a083970"),
    EQ_RELIEVE_FORCED_OPEN("eq_relieve_forced_open", "key_494200e9ac9d4ecc8d0cd96afd0ec9d6"),
    EQ_FORCED_CLOSE("eq_forced_close", "key_87198e454a8f4383bc37a865a7d3b346"),
    EQ_OPEN("eq_open", "key_33c26fbf646c4e668b689897c7918648"),
    EQ_CLOSE("eq_close", "key_ca6d321db9ca4781a14e8bf35d07677f"),
    /**
     * 需求管理变更
     */
    INDOOR_QUALITY_TARGET("indoor_quality_target", "key_de3df7bd18114c839ad9a2cd35c32bc4"),
    TEM_CONTROL_TARGET("tem_control_target", "key_2c41b3e8ea9e4809ad732e76b319d217"),
    EDIT_SPACE_EFFECT_SCOPE("edit_space_effect_scope", "key_474deff938574d5db648e98e85f5d11e");
    private final String code;
    private final String desc;


    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static OperateTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (OperateTypeEnum i : OperateTypeEnum.values()) {
            if (code.equals(i.getCode())) {
                return i;
            }
        }
        return null;
    }

    /**
     * 获取枚举值列表
     *
     * @return KEY VAL 数据列表
     */
    public static List<BizStaticTypeVO> getList() {

        List<BizStaticTypeVO> list = ListUtil.toList();

        for (OperateTypeEnum i : OperateTypeEnum.values()) {
            list.add(new BizStaticTypeVO(String.valueOf(i.getCode()), i.getDesc()));
        }
        return list;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OperateTypeEnum resolve(String value) {
        if (StrUtil.isEmpty(value)) {
            return null;
        }
        return BaseEnum.analyze(value, OperateTypeEnum.class);
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return this.getDesc();
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }
}
