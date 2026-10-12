package com.persagy.apccontrol.basic.model.enumtype;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.base.BizStaticTypeVO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Description: 冷机规则类型
 * @author: yancheng
 * @date: 2023/3/25
 * @version: V1.0
 */
@AllArgsConstructor
@Getter
public enum CoolerRuleTypeEnum  implements BaseEnum<String>, IEnum<String> {

    LARGE_COOLER("large_cooler","key_4f6cfec1fda746999d99053ae532d303"),
    SMALL_COOLER("small_cooler","key_33fad44db990446bb1bd480d9fb753eb"),
    WATER_TEMPERATURE("water_temperature","key_0d7baf0b866749a286869e856b97a6a9"),
    POWER_UP("power_up","key_d196ab48969847ba99dc9176595c281f"),
    SHUTDOWN("shutdown","key_49789b1b32324f03a49a9d621a36fcb0"),
    ;
    private final String code;
    private final String desc;

    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static CoolerRuleTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (CoolerRuleTypeEnum i : CoolerRuleTypeEnum.values()) {
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

        java.util.List<BizStaticTypeVO> list = ListUtil.toList();

        for (RuleTypeEnum i : RuleTypeEnum.values()) {
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
        return LocaleMessageUtil.getMessage(this.desc);
    }
    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }
}
