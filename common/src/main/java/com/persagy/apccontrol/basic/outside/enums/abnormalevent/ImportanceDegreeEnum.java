package com.persagy.apccontrol.basic.outside.enums.abnormalevent;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

/**
 * 异常事件严重程度
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/7/19 14:34
 * @since v5.240502.0
 */
@AllArgsConstructor
@Getter
public enum ImportanceDegreeEnum implements BaseEnum<String> {
    HIGH(1, "key_c1d48ef3ef854ec6a9cb55a562fac506", "key_038fbf4b75694d58a61e72f712952fa2") {
        public Set<ImportanceDegreeEnum> higherDegree() {
            return Sets.newHashSet();
        }
    },
    // v5.241002.0异常事件移除中高状态
    MEDIUM_HIGH(2, "key_f2b98e777eb54bad938bb96b5bd35bc0", "key_a1edd5a090f24e34b7a0362af7480013") {
        public Set<ImportanceDegreeEnum> higherDegree() {
            return Sets.union(HIGH.higherDegree(), Collections.singleton(HIGH));
        }
    },
    MEDIUM(3, "key_dd429e2689764f25aea3445bbfc4d07d", "key_0caa2c1120234e129a96a517d044fa0b") {
        public Set<ImportanceDegreeEnum> higherDegree() {
            return Sets.union(MEDIUM_HIGH.higherDegree(), Collections.singleton(MEDIUM_HIGH));
        }
    },
    LOW(4, "key_f1fb860fb2e1440c8c7467b2fb7a9aae", "key_47186563f4d74cf2a345a6fd3354af2d") {
        public Set<ImportanceDegreeEnum> higherDegree() {
            return Sets.union(MEDIUM.higherDegree(), Collections.singleton(MEDIUM));
        }
    },
    EXTREMELY_LOW(5, "key_d6856ce845734d9b9656cb794ded5722", "key_a3269626bc2f41e5acfb80f4491ea998") {
        public Set<ImportanceDegreeEnum> higherDegree() {
            return Sets.union(LOW.higherDegree(), Collections.singleton(LOW));
        }
    },
    UNKNOWN(6, "key_08241d179eeb4c989389940b30544847", "key_468cc301bb074ca683d8664aa9615378") {
        public Set<ImportanceDegreeEnum> higherDegree() {
            return Sets.union(MEDIUM.higherDegree(), Collections.singleton(MEDIUM));
        }
    };

    @EnumValue
    private final int value;
    private final String desc;
    private final String remark;

    public static ImportanceDegreeEnum init(ImportanceDegreeEnum alertExceptionEventImportanceDegree) {
        if (alertExceptionEventImportanceDegree == null) {
            return EXTREMELY_LOW;
        }
        return alertExceptionEventImportanceDegree;
    }

    // 状态机定义 比自己等级更高的数据
    public abstract Set<ImportanceDegreeEnum> higherDegree();

    @Override
    public String value() {
        return name();
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }
    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public String getRemark() {
        return LocaleMessageUtil.getMessage(remark);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ImportanceDegreeEnum analyze(String value) {
        return BaseEnum.analyze(value, ImportanceDegreeEnum.class);
    }
}
