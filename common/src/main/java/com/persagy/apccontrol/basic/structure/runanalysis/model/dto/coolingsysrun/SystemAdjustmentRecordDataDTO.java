package com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun;

import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 系统调节记录数据DTO
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
public class SystemAdjustmentRecordDataDTO {

    /**
     * 数据类型
     */
    private DataType dataType;

    /**
     * 信息点编码
     */
    private String infoCode;

    /**
     * 信息点值，仅当数据类型为IOT时有效
     */
    private BigDecimal infoCodeValue;

    /**
     * 冷机大机数量，仅当数据类型为CHILLER时有效
     */
    private Integer chillerLargeNumber;

    /**
     * 冷机中机数量，仅当数据类型为CHILLER时有效
     */
    private Integer chillerMediumNumber;

    /**
     * 冷机小机数量，仅当数据类型为CHILLER时有效
     */
    private Integer chillerSmallNumber;

    @Getter
    @AllArgsConstructor
    public enum DataType implements BaseEnum<String> {

        /**
         * IOT
         */
        IOT("iot", "IOT"),

        /**
         * 冷机
         */
        CHILLER("chiller", "冷机");

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
    }
}
