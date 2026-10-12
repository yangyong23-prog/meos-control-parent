package com.persagy.apccontrol.basic.model.enumtype.task;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.base.BizStaticTypeVO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Description: 系统类型
 * @author: yancheng
 * @date: 2023/3/16
 * @version: V1.0
 */
@Getter
@AllArgsConstructor
public enum SystemEnum implements BaseEnum<String>, IEnum<String> {
    /**
     *
     */
    SAAS("saas", "key_7d8de2e9f4614e68a6d2861e08ed59bd"),
    LOCALITY("locality", "key_0496c23144324b83b80df317faf857d6");
    private final String code;
    private final String desc;


    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static SystemEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (SystemEnum i : SystemEnum.values()) {
            if (code.equals(i.getCode())) {
                return i;
            }
        }
        return null;
    }

    /**
     * 获取枚举值列表
     *
     * @return KEY VAL 数据列表
     */
    public static List<BizStaticTypeVO> getList() {

        List<BizStaticTypeVO> list = ListUtil.toList();

        for (SystemEnum i : SystemEnum.values()) {
            list.add(new BizStaticTypeVO(String.valueOf(i.getCode()), i.getDesc()));
        }
        return list;
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
        return this.getDesc();
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }
}
