package com.persagy.apccontrol.basic.model.vo.runanalysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataValueVO {
    /**
     * 数据值
     */
    private BigDecimal dataValue;
    /**
     * 单位
     */
    private String unit;
    /**
     * 数据时间
     */
    private LocalDateTime dataTime;
}
