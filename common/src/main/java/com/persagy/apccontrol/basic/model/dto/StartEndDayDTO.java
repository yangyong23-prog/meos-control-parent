package com.persagy.apccontrol.basic.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-26 13:46
 **/
@Data
public class StartEndDayDTO {
    @Schema(description = "开始日期")
    private String startDay;
    @Schema(description = "结束日期")
    private String endDay;
}