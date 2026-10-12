package com.persagy.apccontrol.basic.outside.feign.model.param.ElectricityData;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ElectrovalenceDataParam {

    @NotNull(message = "{key_58b520e3ff2349e695bc1ccf0d3f1f7a}")
    private String cityCode;

    @NotNull(message = "{key_242157c0c95c45b2a7682f0944857bd2}")
    private String electricityType;

    /**
     * 开始时间, 格式yyyyMMddHHmmss
     */
    @NotNull(message = "{key_8ceaf15b76524876a9b10eea2e267297}")
    @JsonFormat(pattern = DatePattern.PURE_DATETIME_PATTERN)
    private LocalDateTime beginTime;

    /**
     * 结束时间, 格式yyyyMMddHHmmss
     */
    @NotNull(message = "{key_3313241b85ef48d9a3d2c43cd5e7930d}")
    @JsonFormat(pattern = DatePattern.PURE_DATETIME_PATTERN)
    private LocalDateTime endTime;

    /**
     * AppId
     */
    private String appId = "meos";
}
