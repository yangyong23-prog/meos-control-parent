package com.persagy.apccontrol.basic.structure.demand.enumeration.lighting;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * 照明配置设置对象类型 类型枚举
 *
 * @author yuecaipu
 * @since 24Q1 T3S2-5.18.0
 */
@Getter
@AllArgsConstructor
public enum DemandLightingSettingObjectTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 照明功能类型
     */
    FUNC_TYPE("FUNC_TYPE", "key_f8788840f0f44c688a2a7d5af7d190e6",  0),

    /**
     * 照明灯具类型  v5.240402.0 废弃该层级
     */
    @Deprecated
    LAMP_FUNC_TYPE("LAMP_FUNC_TYPE", "key_d9ab8f5cf52a45ad8ec42ffc54a28d00", 1),

    /**
     * 回路
     */
    CIRCUIT("CIRCUIT", "key_c8628082d25847e3aaad095b72aa1731",  2);

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;


    /**
     * 配置等级 值越大等级越高
     */
    private final int level;

    /**
     * 创建当前实例
     *
     * @param source 数据源
     * @return 当前实例
     * @since v6.0.0
     */
    @Nullable
    public static DemandLightingSettingObjectTypeEnum create(@Nullable DemandLightingSettingObjectTypeEnum source) {
        if (Objects.isNull(source)) {
            return null;
        }
        switch (source) {
            case FUNC_TYPE:
                return FUNC_TYPE;
            case LAMP_FUNC_TYPE:
                return LAMP_FUNC_TYPE;
            case CIRCUIT:
                return CIRCUIT;
            default:
                return null;
        }
    }

    @Override
    public String value() {
        return value;
    }
    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }


    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandLightingSettingObjectTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandLightingSettingObjectTypeEnum.class);
    }


    /**
     * 比较配置级别
     * a. 结果>0 当前配置等级高于被比较配置
     * b. 结果=0 当前配置等级与被比较配置一样
     * c. 结果<0 当前配置等级低于被比较配置
     *
     * @param objectTypeEnum
     * @return int
     * @author yuecaipu
     * @date 2024/3/28 16:05
     */
    public int levelCompare(DemandLightingSettingObjectTypeEnum objectTypeEnum) {
        if (null == objectTypeEnum) {
            return 1;
        }
        return this.getLevel() - objectTypeEnum.getLevel();
    }

}
