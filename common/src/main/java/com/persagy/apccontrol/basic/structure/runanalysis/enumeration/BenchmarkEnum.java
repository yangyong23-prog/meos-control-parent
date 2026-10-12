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
　　 * @description: 能耗预算章节的配置数据枚举
*/
@Getter
@AllArgsConstructor
public enum BenchmarkEnum implements BaseEnum<String>, IEnum<String> {

    TARGET("TARGET", "目标值"),

    CONTROL("CONTROL", "管控制"),

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

    public static BenchmarkEnum analyze(String value){
        return Arrays.stream(values()).filter(e -> e.getValue().equalsIgnoreCase(value)).findFirst().orElse(null);
    }

}
