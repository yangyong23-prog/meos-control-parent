package com.persagy.apccontrol.basic.model.enumtype.system;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 数据操作类型
 * @author: yx
 * @date: 2023/5/17
 */
@Getter
@AllArgsConstructor
public enum DataTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     * 新增
     */
    ADD("add", "key_6cc5c1e16b074d669b45cdbd4f5a5d91"),

    /**
     * 编辑
     */
    EDIT("edit", "key_ddc82084c21541ea820840e9338664b9"),

    /**
     * 删除
     */
    DELETE("delete", "key_c6652b85ecbe4dd6ba39de79ecdee66a");

    private final String code;
    private final String desc;

    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static DataTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (DataTypeEnum i : DataTypeEnum.values()) {
            if (code.equals(i.getCode())) {
                return i;
            }
        }
        return null;
    }

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String value() {
        return this.code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(this.desc);
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }
}
