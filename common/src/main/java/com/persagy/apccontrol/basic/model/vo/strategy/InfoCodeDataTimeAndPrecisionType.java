package com.persagy.apccontrol.basic.model.vo.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 信息码数据时间和精度类型
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/11/15 10:35
 * @since v5.241101.0
 */
@Data
public class InfoCodeDataTimeAndPrecisionType {

    @Schema(description = "iot数据起始时间")
    private LocalDateTime startTime;

    @Schema(description = "iot数据结束时间")
    private LocalDateTime endTime;

    @Schema(description = "iot数据精度类型")
    private List<String> ipTypes;

}
