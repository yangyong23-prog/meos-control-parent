package com.persagy.apccontrol.basic.model.base;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;

/**
 * 实例类型枚举
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@AllArgsConstructor
public enum InstanceTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 项目
     */
    PROJECT("PROJECT", "key_c8ff811cb5234f69b587f5dd1fa73141"),

    /**
     * 系统
     */
    SYSTEM("SYSTEM", "key_97300a9731294466884c1933c2a6cb49"),

    /**
     * 建筑
     */
    BUILDING("BUILDING", "key_e977b930027f4fcfbf29bdf9ed47663f"),

    /**
     * 楼层
     */
    FLOOR("FLOOR", "key_860f403627e4419496e3711477912a39"),

    /**
     * 空间
     */
    SPACE("SPACE", "key_93930e54e51b4dbda89417183881b172"),

    /**
     * 照明回路
     */
    CIRCUIT("CIRCUIT", "key_10829ce5750c42f099af7dc60ca0205d"),

    /**
     * 设备
     */
    EQUIPMENT("EQUIPMENT", "key_b057517385f34aca901cb69c4f64433d");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 转换日志对象分类枚举
     *
     * @return 日志对象分类枚举
     * @since v5.8.0
     */
    public RunningLogObjectCategpryEnum toLogObjectCategoryEnum() {
        switch (this) {
            case PROJECT:
                return RunningLogObjectCategpryEnum.PROJECT;
            case BUILDING:
                return RunningLogObjectCategpryEnum.BUILDING;
            case FLOOR:
                return RunningLogObjectCategpryEnum.FLOOR;
            case SPACE:
                return RunningLogObjectCategpryEnum.SPACE;
            default:
        }
        return null;
    }

    /**
     * 转换需求实例类型枚举
     *
     * @return 需求实例类型枚举
     * @since 5.240702.0
     */
    public DemandInstanceTypeEnum toDemandTemplateTypeEnum() {
        switch (this) {
            case PROJECT:
                return DemandInstanceTypeEnum.PROJECT;
            case BUILDING:
                return DemandInstanceTypeEnum.BUILDING;
            case FLOOR:
                return DemandInstanceTypeEnum.FLOOR;
            case SPACE:
                return DemandInstanceTypeEnum.SPACE;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(this.getClass().getSimpleName(), this);
        }
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InstanceTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InstanceTypeEnum.class);
    }
}
