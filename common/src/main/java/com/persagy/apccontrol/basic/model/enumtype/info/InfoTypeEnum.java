package com.persagy.apccontrol.basic.model.enumtype.info;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.base.BizStaticTypeVO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Description: 信息点类型
 * @author: yancheng
 * @date: 2023/3/17
 * @version: V1.0
 */
@Getter
@AllArgsConstructor
public enum InfoTypeEnum implements BaseEnum<String>, IEnum<String> {

    /**
     *
     */
    GROUP("GROUP", "key_1349ce97fcdb487d8e9cfce019d6285a"),

    STRING("STRING", "key_3f1bc65c92594f7c8e46c96be2bf4cbf"),

    ENUM("ENUM", "key_217d9e5fb1004a9eaf1688c9f861199f");
    ;
    private final String code;
    private final String desc;


    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static InfoTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (InfoTypeEnum i : InfoTypeEnum.values()) {
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

        for (InfoTypeEnum i : InfoTypeEnum.values()) {
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
