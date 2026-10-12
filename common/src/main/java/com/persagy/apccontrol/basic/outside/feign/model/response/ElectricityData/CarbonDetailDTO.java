package com.persagy.apccontrol.basic.outside.feign.model.response.ElectricityData;

import com.persagy.validate.utility.annotation.NumberToScaleString;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CarbonDetailDTO {
    private String cityCode;

    private String cityName;

    @NumberToScaleString
    private BigDecimal value;

    private String unit;

    /**
     * 生效开始时间
     */
    private LocalDateTime effectiveBeginTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime effectiveEndTime;
}
