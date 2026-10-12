package com.persagy.apccontrol.basic.outside.feign.model.param.dtppersist.aggregate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 持久化服务泵组开启台数统计查询参数
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Data
public class DtpPersisWatchChillPumpStatisticsQueryParam {

    /**
     * 泵组ID
     */
    @NotEmpty(message = "{key_028261b7bd4d413489eef9643db781b2}")
    private Set<String> objectIds;

    /**
     * 查询时间点, 格式为yyyyMMddHHmmss
     */
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    @NotNull(message = "{key_99fd5c44fedd4cf8808456a6ae79f902}")
    private LocalDateTime ipTime;

}
