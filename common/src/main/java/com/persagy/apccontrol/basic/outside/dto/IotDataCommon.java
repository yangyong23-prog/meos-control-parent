package com.persagy.apccontrol.basic.outside.dto;

import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * iot孪生和持久化通用数据
 *
 * @author yuecaipu
 * @date 2024/12/5 14:03
 * @since v5.241201.0
 */
@Data
public class IotDataCommon {

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 项目ID
     */
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
     * 当前时间点对应的IOT值，如果要参与计算，请使用 iotValue 值，因为涉及到单位转换，此字符串值，不允许进行数据变更
     */
    private String dataValue;

    /**
     * 上报时间 yyyyMMddHHmmss
     */
    private String dataTime;

    public LocalDateTime toDataTime() {
        if (StringUtils.isBlank(dataTime)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern(DateUtils.SDFSECOND));
        }catch (Exception ex){
            System.out.println(ex);
            return null;
        }
        //  return LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern(DateUtils.SDFSECOND));
    }
}
