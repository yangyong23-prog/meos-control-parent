package com.persagy.apccontrol.basic.model.enumtype;

import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Description: 规则类型
 * @author: yancheng
 * @date: 2023/3/24
 * @version: V1.0
 */
@AllArgsConstructor
@Getter
public enum RuleTypeEnum  implements BaseEnum<String>, IEnum<String> {

    COOLER_STAND("cooler_stand","key_a42be229c5c942baa4d9a214dbaaf9a1"),
    COOLER_TEMPERATURE("cooler_temperature","key_7931bb5c6f7b4cdca0e539844364814b"),
    COOLER_SWITCH("cooler_switch","key_f5c24b17f29e4fd2bbfd2392b10da43a"),
    ;
    private final String code;
    private final String desc;


    /**
     * 通过CODE 获取枚举
     *
     * @param code
     * @return
     */
    public static RuleTypeEnum getEnumByCode(String code) {
        if (code == null) {
            return null;
        }
        for (RuleTypeEnum i : RuleTypeEnum.values()) {
            if (code.equals(i.getCode())) {
                return i;
            }
        }
        return null;
    }

    /**
     * 获取冷机规则code
     * @return
     */
    public static List<String> getCodeList(){
         return ListUtil.toList(RuleTypeEnum.COOLER_STAND.getCode(),
                 RuleTypeEnum.COOLER_TEMPERATURE.getCode(),
                 RuleTypeEnum.COOLER_SWITCH.getCode()
         );
    }

    /**
     * 获取冷机规则列表
     * @return
     */
    public static List<RuleTypeEnum> getList(){
        return ListUtil.toList(RuleTypeEnum.COOLER_STAND,
                RuleTypeEnum.COOLER_TEMPERATURE,
                RuleTypeEnum.COOLER_SWITCH
        );
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
