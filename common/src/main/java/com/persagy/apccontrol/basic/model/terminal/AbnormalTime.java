package com.persagy.apccontrol.basic.model.terminal;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * 异常空间时间段
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/7 10:17
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalTime {

    @Schema(description = "异常温度-时间")
    private AbnormalTimeDetail tdb;

    @JsonProperty(value = "cO2")
    @Schema(description = "异常CO2-时间")
    private AbnormalTimeDetail cO2;

}
