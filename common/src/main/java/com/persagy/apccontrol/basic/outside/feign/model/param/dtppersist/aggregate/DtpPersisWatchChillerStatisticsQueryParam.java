package com.persagy.apccontrol.basic.outside.feign.model.param.dtppersist.aggregate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 持久化服务冷机开启台数统计查询参数
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Data
public class DtpPersisWatchChillerStatisticsQueryParam {

    /**
     * 系统ID
     */
    @NotBlank(message = "{key_6fc14063bc9143f19673d1d21b6c5e4d}")
    private String objectId;

    /**
     * 查询时间点, 格式为yyyyMMddHHmmss
     */
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    @NotNull(message = "{key_99fd5c44fedd4cf8808456a6ae79f902}")
    private LocalDateTime ipTime;

}
