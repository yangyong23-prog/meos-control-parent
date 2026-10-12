package com.persagy.apccontrol.basic.structure.demand.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * 需求实例类型枚举
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@AllArgsConstructor
public enum DemandInstanceTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 项目
     */
    PROJECT("PROJECT", "key_c8ff811cb5234f69b587f5dd1fa73141", "project", 0),

    /**
     * 建筑
     */
    BUILDING("BUILDING", "key_e977b930027f4fcfbf29bdf9ed47663f", "building", 1),

    /**
     * 楼层
     */
    FLOOR("FLOOR", "key_860f403627e4419496e3711477912a39", "floor", 2),

    /**
     * 空间
     */
    SPACE("SPACE", "key_93930e54e51b4dbda89417183881b172", "space", 3);

    /**
     * 枚举值
     */
    private final String value;


    /**
     * 枚举信息
     */
    private final String reasonPhrase;


    /**
     * 物理世界对象类型
     */
    @Getter
    private final String rwdType;


    /**
     * 配置等级 值越大等级越高
     */
    @Getter
    private final int level;

    /**
     * 创建当前实例
     *
     * @param source 数据源
     * @return 当前实例
     * @since v6.0.0
     */
    @Nullable
    public static DemandInstanceTypeEnum create(@Nullable DtpRwdClassCodeEnum source) {
        if (Objects.isNull(source)) {
            return null;
        }
        switch (source) {
            case PROJECT:
                return PROJECT;
            case BUILDING:
                return BUILDING;
            case FLOOR:
                return FLOOR;
            case SPACE:
                return SPACE;
            default:
                return null;
        }
    }

    public static DemandInstanceTypeEnum getByCode(String code) {
        DemandInstanceTypeEnum[] values = DemandInstanceTypeEnum.values();
        for (DemandInstanceTypeEnum demandInstanceTypeEnum : values) {
            if (demandInstanceTypeEnum.getValue().equals(code)) {
                return demandInstanceTypeEnum;
            }
        }
        return null;
    }

    /**
     * 获取上级对象实例类型
     *
     * @param instanceType
     * @return DemandInstanceTypeEnum
     * @author yuecaipu
     * @date 2024/3/15 14:24
     */
    public static DemandInstanceTypeEnum getParentInstanceType(DemandInstanceTypeEnum instanceType) {
        if (DemandInstanceTypeEnum.SPACE.equals(instanceType)) {
            return DemandInstanceTypeEnum.FLOOR;
        } else if (DemandInstanceTypeEnum.FLOOR.equals(instanceType)) {
            return DemandInstanceTypeEnum.BUILDING;
        } else if (DemandInstanceTypeEnum.BUILDING.equals(instanceType)) {
            return DemandInstanceTypeEnum.PROJECT;
        }
        return null;
    }

    /**
     * 转换所有实例类型和实例ID映射
     *
     * @return 实例类型 -> 实例ID
     * @since 7.250801.0
     */
    public static Map<DemandInstanceTypeEnum, Set<String>> toAllInstanceTypeAndInstanceIds() {
        Map<DemandInstanceTypeEnum, Set<String>> result = new LinkedHashMap<>();
        for (DemandInstanceTypeEnum instanceType : DemandInstanceTypeEnum.values()) {
            result.put(instanceType, null);
        }
        return result;
    }

    @Override
    public String getValue() {
        return value;
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
    public static DemandInstanceTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandInstanceTypeEnum.class);
    }

    /**
     * 比较配置级别
     * a. 结果>0 当前配置等级高于被比较配置
     * b. 结果=0 当前配置等级与被比较配置一样
     * c. 结果<0 当前配置等级低于被比较配置
     *
     * @param demandInstanceTypeEnum
     * @return int
     * @author yuecaipu
     * @date 2024/3/28 16:05
     */
    public int levelCompare(DemandInstanceTypeEnum demandInstanceTypeEnum) {
        if (null == demandInstanceTypeEnum) {
            return 1;
        }
        return this.getLevel() - demandInstanceTypeEnum.getLevel();
    }

}
