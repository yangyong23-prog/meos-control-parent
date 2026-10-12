package com.persagy.apccontrol.basic.model.enumtype.lighting;


import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeControlDeviationStatusEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 运行偏差类型
 *
 * @author MAF
 * @date 2024/04/29
 */
@Getter
@AllArgsConstructor
public enum LightingRunningDeviationTypeEnum implements BaseEnum<String>, IEnum<String> {
    /*"MORE_OPEN": "key_a47ebf0adb8746798241d4ca75d4c82b",
            "LESS_OPEN": "key_bafc5219c3474688ac414c801e4ed335",
            "NO_DEVIATION": "无偏差"*/
    MORE_OPEN("MORE_OPEN", "key_a47ebf0adb8746798241d4ca75d4c82b"),
    LESS_OPEN("LESS_OPEN", "key_bafc5219c3474688ac414c801e4ed335");

    private final String value;

    private final String reasonPhrase;

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

    @Override
    public String toString() {
        return value;
    }

    /**
     * 创建当前实例
     *
     * @param source 数据源
     * @return 当前实例
     * @since 7.250402.0
     */
    @SuppressWarnings("DuplicatedCode")
    public static @Nullable LightingRunningDeviationTypeEnum of(@Nullable DtpKnowledgeControlDeviationStatusEnum source) {
        if (Objects.isNull(source)) {
            return null;
        }
        switch (source) {
            case OVER_OPENING:
                return MORE_OPEN;
            case UNDER_OPENING:
                return LESS_OPEN;
            default:
                return null;
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LightingRunningDeviationTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, LightingRunningDeviationTypeEnum.class);
    }
}
