package com.persagy.apccontrol.basic.model.enumtype.security;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 撤布防状态
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 19:40
 * @since v5.240801.0
 */
@Getter
@AllArgsConstructor
public enum DeploymentStatusEnum implements BaseEnum<String>, IEnum<String> {


    /**
     * 布防
     */
    DEPLOY_A_DEFENCE("DEPLOY_A_DEFENCE", "key_a798cfa32a78488a97800c237a370b1f", "1"),

    /**
     * 撤防
     */
    WITHDRAW_A_GARRISON("WITHDRAW_A_GARRISON", "key_5f966a12d1fa40f3ad40585cb398dcc3", "2"),

    /**
     * 部分布防
     */
    PARTIAL_DEFENSE("PARTIAL_DEFENSE", "key_cfa336229de646dfbebf1df969ab04e8", "3"),

    /**
     * 未知
     */
    UNKNOWN("UNKNOWN", "key_08241d179eeb4c989389940b30544847", "99");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;

    /**
     * 条件值
     */
    private final String conditionValue;


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
        return LocaleMessageUtil.getMessage(this.reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DeploymentStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, DeploymentStatusEnum.class);
    }

    public static DeploymentStatusEnum objectToEnum(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        for (DeploymentStatusEnum deploymentStatusEnum : DeploymentStatusEnum.values()) {
            if (value.equals(deploymentStatusEnum.getConditionValue())) {
                return deploymentStatusEnum;
            }
        }
        return null;
    }
}

