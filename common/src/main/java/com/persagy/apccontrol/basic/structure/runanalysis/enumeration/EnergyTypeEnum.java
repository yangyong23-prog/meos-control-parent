package com.persagy.apccontrol.basic.structure.runanalysis.enumeration;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/23 15:25
　　 * @description: 能耗类型枚举
*/
@Getter
@AllArgsConstructor
public enum EnergyTypeEnum implements BaseEnum<String>, IEnum<String> {

    ELECTRIC("1", "电"),

    WATER("2", "水"),
    ;

    /**
     * 枚举值
     */
    private final String value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;


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
        return reasonPhrase;
    }

    public static EnergyTypeEnum analyze(String value){
        return Arrays.stream(values()).filter(e -> e.getValue().equalsIgnoreCase(value)).findFirst().orElse(null);
    }

}
