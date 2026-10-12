package com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * 信息点模版类型枚举
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Getter
@AllArgsConstructor
public enum InfoCodeTemplateTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 默认模版
     */
    DEFAULT("DEFAULT", "key_dd9177747eee498da20d1cecbb42f071"),

    /**
     * Hover模版
     */
    HOVER("HOVER", "key_3bc2a45c5eeb4a20ac49401a6caf72cd"),

    /**
     * DETAIL
     */
    DETAIL("DETAIL", "key_9142e3c1a56c4515bd54ec67caefbee0"),

    /**
     * 列表模版
     */
    TABLE_LIST("TABLE_LIST", "key_5effb871801b4ee4bf9eb43819ef6e6e"),

    /**
     * 网格图
     */
    GRID("GRID", "key_b424dedacb0c4851855558fa24a676ed"),

    /**
     * 模型图
     */
    MODEL_VIEW("MODEL_VIEW", "key_a4f54a35e1d545f3ac3abcfa64377a8c"),

    /**
     * 半刨图
     */
    HALF_VIEW("HALF_VIEW", "key_a4f54a35e1d545f3ac3abcfa64377a8d"),

    /**
     * 曲线图
     */
    CURVE("CURVE", "key_9d90de57da8d46099dea10444659b9f5"),

    /**
     * 关系图
     */
    RELATION_VIEW("RELATION_VIEW", "key_3cc1dda943fa4a5d919323e20b172f76"),
    
    /**
     * 自定义模版 通过传递对象信息点进行订阅不走配置表
     */
    CUSTOM_IOT("CUSTOM_IOT", "key_0dea27a6faad4b6bba873b62b4c00ce7"),

    /**
     * 前端配置
     */
    WEB_CONFIG("WEB_CONFIG", "key_0c901ea408cc44b2a7571cc710984115"),

    /**
     * 拓扑视图
     */
    TOPOLOGY_VIEW("TOPOLOGY_VIEW", "key_topology_view_template"),
    ;

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static InfoCodeTemplateTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, InfoCodeTemplateTypeEnum.class);
    }

    /**
     * 判断指定一级运行监测类型和模板类型是否允许使用对象缓存。
     *
     * <p>许可矩阵是代码固定契约，放在不依赖 Spring Bean 的模板类型枚举中，
     * 供只有枚举参数的读取入口复用；实例模型的 {@code allowCache()} 仅委托此规则。
     * 新对象模型的实际启用范围还必须叠加固定支持类型和 READY 状态。</p>
     *
     * @param runningMonitorType 一级运行监测类型，可为空
     * @param templateType       信息点模板类型，可为空
     * @return 模板是否允许使用对象缓存
     * @since v7.260701.0
     */
    public static boolean allowCache(
            @Nullable RunningMonitorTypeEnum runningMonitorType,
            @Nullable InfoCodeTemplateTypeEnum templateType) {
        if (runningMonitorType == RunningMonitorTypeEnum.LIGHTING) {
            return templateType == InfoCodeTemplateTypeEnum.DETAIL
                    || templateType == InfoCodeTemplateTypeEnum.TABLE_LIST;
        }
        if (runningMonitorType == RunningMonitorTypeEnum.AIR_TERMINAL
                || runningMonitorType == RunningMonitorTypeEnum.VENTILATION
                || runningMonitorType == RunningMonitorTypeEnum.COMPUTER_ROOM_SECURITY) {
            return templateType == InfoCodeTemplateTypeEnum.GRID
                    || templateType == InfoCodeTemplateTypeEnum.TABLE_LIST;
        }
        if (runningMonitorType == RunningMonitorTypeEnum.SECURITY) {
            return templateType == InfoCodeTemplateTypeEnum.TABLE_LIST;
        }
        return false;
    }
}
