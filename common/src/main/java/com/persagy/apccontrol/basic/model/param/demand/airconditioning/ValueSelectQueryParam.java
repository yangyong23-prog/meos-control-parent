package com.persagy.apccontrol.basic.model.param.demand.airconditioning;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@ToString(callSuper = true)
public class ValueSelectQueryParam {

    @Schema(name = "项目id")
    @NotNull(message = "{key_d4be1b60bd32403287b0a47366af7092}")
    private String projectId;

    @Schema(name = "价值类型")
    private BasicsConfigTypeEnum type;

    @Schema(name = "日期")
    private LocalDate date;

    @Schema(name = "IOT时间点，yyyyMMddHHmmss")
    private String ipTime;

    @Schema(name = "IOT时间点，yyyyMMddHHmmss")
    private String endTime;

    public void fillIpTime(LocalDateTime ipTime) {
        this.ipTime = ipTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public void fillEndTime(LocalDateTime endTime) {
        this.endTime = endTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
