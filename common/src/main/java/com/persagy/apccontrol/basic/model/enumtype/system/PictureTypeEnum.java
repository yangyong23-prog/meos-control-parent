package com.persagy.apccontrol.basic.model.enumtype.system;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.base.BizStaticTypeVO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Description: 图片类型
 * @author: yancheng
 * @date: 2023/3/17
 * @version: V1.0
 */
@Getter
@AllArgsConstructor
public enum PictureTypeEnum implements BaseEnum<String>, IEnum<String> {
    /**
     * 2D
     */
    SYSTEM("system", "key_44154f4ee44547f49ace0d16e0fdec39"),

    /** 3D */
    MODEL("model", "key_e67d3d3d227c46baa5ecbfe03ce40e24"),
    EQUIPMENT("equipment", "key_e07ae51b0e014529a7842dfc263c090d"),
    ;
    private final String code;
    private final String desc;


    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static PictureTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (PictureTypeEnum i : PictureTypeEnum.values()) {
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

        for (PictureTypeEnum i : PictureTypeEnum.values()) {
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
