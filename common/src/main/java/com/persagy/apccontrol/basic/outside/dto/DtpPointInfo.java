package com.persagy.apccontrol.basic.outside.dto;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 中台响应数据结构
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/2/18 14:22
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtpPointInfo extends IotDataCommon {

    private String groupCode;

    private String projectId;

    /**
     * 对象ID
     */
    private String objectId;

    /**
     * 设备类编码，查询时，不会返回
     */
    private String classCode;

    /**
     * 信息点
     */
    private String infoCode;

    /**
     * 分精度类型 5min,15min,1h,1d,1m
     */
    private String ipType;

    /**
     * 当前时间点 yyyy-MM-dd HH:mm:ss
     */
    private String ipTime;

    private Date iotTime;

    /**
     * 当前时间点对应的IOT值，如果要参与计算，请使用 iotValue 值，因为涉及到单位转换，此字符串值，不允许进行数据变更
     */
    private String ipValue;

    private BigDecimal iotValue;

    /**
     * 当前时间点对应的IOT值，如果要参与计算，请使用 iotValue 值，因为涉及到单位转换，此字符串值，不允许进行数据变更
     */
    private String dataValue;

    /**
     * 上报时间 yyyyMMddHHmmss
     */
    private String dataTime;
    
    /**
     * 时间常量
     */
    private Integer dataIndate;
    
    public DtpPointInfo(String objectId, String infoCode, String dataValue, String dataTime, Integer dataIndate) {
        this.objectId = objectId;
        this.infoCode = infoCode;
        this.dataValue = dataValue;
        this.dataTime = dataTime;
        this.dataIndate = dataIndate;
    }

    public LocalDateTime toDataTime() {
        if (StringUtils.isBlank(ipTime)) {
            return null;
        }
        return LocalDateTime.parse(ipTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public BigDecimal getIotValue() {
        return StrUtil.isBlank(ipValue) ? null : new BigDecimal(ipValue);
    }

}
