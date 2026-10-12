package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物理世界Criteria符号枚举
 *
 * @author ShaoHongbo
 * @since v5.6.0
 */
@Getter
@AllArgsConstructor
public enum DtpRwdCriteriaSignEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 不等于
     */
    NOT_EQUALS("$ne", "key_073a957bc4f54525805b8563eb40c610"),

    /**
     * 包含
     */
    EQUALS_IN("$in", "key_9113fdd21c9d4f2bbef016ce5f63b6a1"),

    /**
     * 不包含
     */
    NOT_EQUALS_IN("$notIn", "key_b5edd32c00bd41eea5f9b93e44a57b89"),

    /**
     * 大于
     */
    GREATER_THAN("%gt", "key_1a34c38c36ad42dc8ef6644ff4c9a18a"),

    /**
     * 大于等于
     */
    GREATER_THAN_AND_EQUALS("$ge", "key_de83b24c99734240a0201c0c1e070037"),

    /**
     * 小于
     */
    LESS_THAN("$lt", "key_87a82dbf2018458c86c13e1d4d4da5f0"),

    /**
     * 小于等于
     */
    LESS_THAN_AND_EQUALS("$le", "key_03a18f64a3d849e28c54d2c95c9e2cc0"),

    /**
     * 模糊匹配
     */
    LIKE("$like", "key_05d8ee025b004df2997362fe54fcaa85"),

    /**
     * like '$sqlFragment'
     *
     * <p>例: local_id LIKE "$temp"
     */
    LIKE_LEFT("$likeLeft", "like '$sqlFragment'"),

    /**
     * like 'sqlFragment$'
     *
     * <p>例: local_id LIKE "temp$"
     */
    LIKE_RIGHT("$likeRight", "like 'sqlFragment$'"),

    /**
     * 模糊不匹配
     */
    NOT_LIKE("$notLike", "key_4305f46d41104ea39f2e0b96bff1867a"),

    /**
     * 是否为空
     */
    IS_NULL("$null", "key_1b1accc7c97649e7b03c76659e454713"),

    /**
     * and (sqlFragment or sqlFragment)
     *
     * <p>例: (local_id LIKE "$temp$" OR local_name LIKE "$temp$")
     */
    AND_OR("$andOr", "and (sqlFragment or sqlFragment)"),

    /**
     * json格式数据查询, 返回符合条件的并集数组元素
     */
    JSON_OVER_LAPS("$overLaps", "key_162c0e573a8144d095162fbadfd24446"),

    /**
     * json格式数据查询, 返回符合条件的交集数组元素
     */
    JSON_CONTAINS("$contains", "key_de06e591064d43c8afd9cd2ee579c0ca");

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
    public static DtpRwdCriteriaSignEnum analyze(String value) {
        return BaseEnum.analyze(value, DtpRwdCriteriaSignEnum.class);
    }
}
