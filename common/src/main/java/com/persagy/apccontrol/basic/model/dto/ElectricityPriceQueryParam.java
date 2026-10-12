package com.persagy.apccontrol.basic.model.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 电价查询参数
 *
 * @author muanfu
 * @date 2025/03/27
 */
@Data
public class ElectricityPriceQueryParam {

    /**
     * 用电类型(1：单一制；2：两部制)
     */
    @NotNull
    private String electricityType;
    /**
     * 分精度类型 5min,15min,1h,1d,1m 默认：15min
     */
    @Nullable
    private String ipType;
    /**
     * 信息点编码
     * 默认值:elecPrice
     */
    @Nullable
    private String infoCode;
    /**
     * 开始时间  (yyyy-MM-dd HH:mm:ss)
     */
    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    /**
     * 结束时间 (yyyy-MM-dd HH:mm:ss)
     */
    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
