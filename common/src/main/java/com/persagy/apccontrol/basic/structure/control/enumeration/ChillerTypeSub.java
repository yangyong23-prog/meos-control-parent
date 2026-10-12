package com.persagy.apccontrol.basic.structure.control.enumeration;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Getter;

/**
 * 制冷设备子类型
 *
 * @author yuecaipu
 * @date 2025/2/13 13:51
 * @since v7.250201.00
 */
@Getter
public enum ChillerTypeSub implements BaseEnum<String> {

    /**
     * 泵组
     */
    PUMP_GROUP_BASE("PUMP_GROUP_BASE", "key_55029dc2889541408ac9286d75938e53","PUMP_GROUP_BASE", 0),

    /**
     * 锅炉
     */
    ACCHXB("ACCHXB", "key_boiler", "BOILER", 0),

    /**
     * 串联冷机组
     */
    ACSCG("ACSCG", "key_672682f898d7461a98ff3ef7ffffa291", "ACSCG", 1),

    /**
     * 常规冷机 离心式冷水机组与螺杆机组ACCCCC/ACCCSC 非双工况isDualCondition=否
     */
    BASE("BASE", "key_1960ef757f7a45a299899df9e00e41f9", "BASE", 2),

    /**
     * 常规冷机 离心式冷水机组 双工况isDualCondition=否
     */
    ACCCCC("ACCCCC", "key_1702bc423e6a432cb9e06d1fd0747fd1", "BASE", 3),


    /**
     * 常规冷机 基载螺杆式冷水机组 双工况isDualCondition=是
     */
    ACCCSC("ACCCSC", "key_696f3af5ae974c7fb31e040e8a5d9e03", "BASE", 4),

    /**
     * 常规双工况冷机 离心机与螺杆机组ACCCCC/ACCCSC 双工况isDualCondition=是
     */
    BASE_DUAL("BASE_DUAL", "key_dfd5d564241348b290fed4cff144fa0a", "BASE_DUAL", 5),

    /**
     * 常规冷机 离心式冷水机组 双工况isDualCondition=否
     */
    ACCCCC_DUAL("ACCCCC_DUAL", "key_f94a96a60a3b4be09a60de66cd1380b9", "BASE_DUAL", 6),

    /**
     * 常规冷机 双工况螺杆式冷水机组 双工况isDualCondition=是
     */
    ACCCSC_DUAL("ACCCSC_DUAL", "key_5219979be223460698c8fa14e3eb9e71", "BASE_DUAL", 7),

    /**
     * 风冷热泵机组
     */
    // ACCCAH("ACCCAH", "风冷热泵机组"),
    ACAHC("ACAHC", "key_c1a021cbdd9349dcaab876979c5607b5", "ACAHC", 8),

    /**
     * 地源热泵机组 非双工况isDualCondition=否
     */
    ACCCGH("ACCCGH", "key_85b1ec9bdb1d4fe09df2af2a6f2b64e4", "ACCCGH", 9),

    /**
     * 双工况地源热泵机组 双工况isDualCondition=是
     */
    ACCCGH_DUAL("ACCCGH_DUAL", "key_c8e19307673b49499ea0cdec25d9893d", "ACCCGH_DUAL", 10),

    /**
     * 水源热泵机组 非双工况isDualConditiong=否
     */
    ACCCWH("ACCCWH", "key_2e4981cf0dd14ea38f07fced72595a28", "ACCCWH", 11),

    /**
     * 双工况水源热泵机组 双工况isDualCondition=是
     */
    ACCCWH_DUAL("ACCCWH_DUAL", "key_bce26fef22af46429ea4d57a0a90ec81", "ACCCWH_DUAL", 12),

    /**
     * 吸收式冷水机组 动力来源-燃气
     */
    ACCCAC("ACCCAC", "key_8a91347e56504548aa1f16a6645d0fe7", "ACCCAC", 13),

    ;


    /**
     * code
     */
    private final String code;

    /**
     * name
     */
    private final String name;

    public String getName() {
        return LocaleMessageUtil.getMessage(name);
    }


    /**
     * 冷机类型，相同类型实际属于同一类，在计算时统一处理
     */
    private final String chillerType;

    private final int order;

    ChillerTypeSub(String code, String name, String chillerType, int order) {
        this.code = code;
        this.name = name;
        this.chillerType = chillerType;
        this.order = order;
    }

    /**
     * 是否属于双工况主机
     *
     * @return
     */
    public boolean belongDualObject() {
        return this == BASE_DUAL || this == ACCCCC_DUAL || this == ACCCSC_DUAL || this == ACCCGH_DUAL || this == ACCCWH_DUAL;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return this.name;
    }
}
