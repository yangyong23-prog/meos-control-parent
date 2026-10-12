package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 信息点需求目标取值逻辑模型
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InfoCodeTackLogicDemandTargetModel extends BaseInfoCodeTackValueLogicModel {

    /**
     * 类型
     */
    @NonNull
    private TargetTypeEnum code;

    /**
     * 过滤类型, 为空时直接取当前时间的类型
     */
    @Nullable
    private String filterType;

    @Override
    public String logTag() {
        return "信息点配置-取值逻辑-需求目标";
    }

    @AllArgsConstructor
    public enum TargetTypeEnum implements BaseEnum<String>, IEnum<String> {

        /**
         * 空气（干球）温度
         */
        TDB("tdb", "key_2f64fe72781c484fb5d6f6daaf084759"),

        /**
         * 二氧化碳浓度值
         */
        CO2("cO2", "key_7bf1f0452c4f4cc5a8c6b29bf1c96894"),
        
        cO("cO", "key_1f688e990578432c803ba2da443498ed"),
        
        o2("o2", "key_31eec4bcb31842d7872e261bbf97328c"),
        
        nH3("nH3", "key_1c8b7305a2784c18b7a9393b47f21577"),
        
        h2S("h2S", "key_08c69ea7470b42bba7010a9f99d996ee"),
        
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
        public String getValue() {
            return this.value;
        }

        @Override
        public String value() {
            return this.value;
        }

        @Override
        public String getReasonPhrase() {
            return LocaleMessageUtil.getMessage(reasonPhrase);
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public static TargetTypeEnum analyze(String value) {
            return BaseEnum.analyze(value, TargetTypeEnum.class);
        }
    }
}
