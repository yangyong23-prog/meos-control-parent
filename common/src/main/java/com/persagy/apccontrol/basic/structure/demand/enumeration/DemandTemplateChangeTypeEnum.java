package com.persagy.apccontrol.basic.structure.demand.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 需求模版变动类型
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Getter
@AllArgsConstructor
public enum DemandTemplateChangeTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 新增
     */
    INSERT("INSERT", "key_6cc5c1e16b074d669b45cdbd4f5a5d91"),

    /**
     * 编辑
     */
    UPDATE("UPDATE", "key_ddc82084c21541ea820840e9338664b9"),

    /**
     * 删除
     */
    REMOVE("REMOVE", "key_c6652b85ecbe4dd6ba39de79ecdee66a"),

    /**
     * 应用
     */
    APPLIED("APPLIED", "key_40785be414a34b61b050471b9bac1724");

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
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

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandTemplateChangeTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, DemandTemplateChangeTypeEnum.class);
    }
}
