package com.persagy.apccontrol.basic.model.terminal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalTime;
import java.util.TreeMap;

/**
 * 云端--异常空间列表--异常数据
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/6 16:38
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalResult {

    @Schema(description = "异常温度数据")
    private TreeMap<LocalTime, AbnormalResultDetail> tdb;

    @JsonProperty(value = "cO2")
    @Schema(description = "异常CO2数据")
    private TreeMap<LocalTime, AbnormalResultDetail> cO2;

}
