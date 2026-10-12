package com.persagy.apccontrol.basic.model.param.weather;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Data
public class QueryWeatherParam {

    /**
     * 结束时间，不传默认查当前时间前24小时
     */
    @Nullable
    private LocalDateTime endTime;
    /**
     * true查询未来24小时天气，默认false
     */
    private Boolean queryFuture = false;

    /**
     * 查询壳配置,默认查询
     */
    private Boolean queryShellConfig = true;
    /**
     * 开始时间，不传默认查当前时间前24小时
     */
    @Nullable
    private LocalDateTime startTime;
    /**
     * 查询时间精度，默认1小时
     */
    @Nullable
    private DtpRwdPersistenceTimeTypeEnum ipType;
}
