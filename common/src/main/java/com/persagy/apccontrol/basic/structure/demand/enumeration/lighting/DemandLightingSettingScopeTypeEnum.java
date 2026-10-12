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
 * 照明配置范围类型 类型枚举
 *
 * @author yuecaipu
 * @since 24Q1 T3S2-5.18.0
 */
@Getter
@AllArgsConstructor
public enum DemandLightingSettingScopeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 全局配置
     */
    GLOBAL("GLOBAL", "key_8bfa8caa2bb04b339abbd5434091243d",  0),

    /**
     * 空间配置
     */
    SPACE("SPACE_FUNC_TYPE", "key_c48b6b95c70c400cb3060e7df5eebc61", 1);

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
    public static DemandLightingSettingScopeTypeEnum create(@Nullable DemandLightingSettingScopeTypeEnum source) {
        if (Objects.isNull(source)) {
            return null;
        }
        switch (source) {
            case GLOBAL:
                return GLOBAL;
            case SPACE:
                return SPACE;
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
    public static DemandLightingSettingScopeTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandLightingSettingScopeTypeEnum.class);
    }


    /**
     * 比较配置级别
     * a. 结果>0 当前配置等级高于被比较配置
     * b. 结果=0 当前配置等级与被比较配置一样
     * c. 结果<0 当前配置等级低于被比较配置
     *
     * @param scopeTypeEnum
     * @return int
     * @author yuecaipu
     * @date 2024/3/28 16:05
     */
    public int levelCompare(DemandLightingSettingScopeTypeEnum scopeTypeEnum) {
        if (null == scopeTypeEnum) {
            return 1;
        }
        return this.getLevel() - scopeTypeEnum.getLevel();
    }

}
