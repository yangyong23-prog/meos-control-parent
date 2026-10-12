package com.persagy.apccontrol.basic.model.enumtype;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 判断室外空气状态的方式枚举
 * <p>
 * 用于通风控制逻辑条件输入中"判断室外空气状态的方式"参数的选项定义，
 * 同时作为 mulInstance 输出和边缘侧值匹配的统一权威来源。
 * <p>
 * 编码约定：1=干球温度判断（默认）、2=湿球温度判断、3=干球温度+湿球温度综合判断；
 * 编码一经写入壳配置存量数据即与项目数据耦合，不得随意变更。
 *
 * @author qoder
 * @since v7.260802.2
 */
@Getter
@AllArgsConstructor
public enum VentJudgeModeEnum {

    DRY("1", "干球温度判断"),
    WET("2", "湿球温度判断"),
    COMPREHENSIVE("3", "干球温度+湿球温度综合判断"),
    ;

    private final String code;
    private final String desc;

    /**
     * 根据 code 查找枚举，未匹配返回 null。
     */
    public static VentJudgeModeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (VentJudgeModeEnum mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return null;
    }
}
