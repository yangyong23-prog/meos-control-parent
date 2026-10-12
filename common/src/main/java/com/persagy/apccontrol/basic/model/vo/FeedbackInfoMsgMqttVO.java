package com.persagy.apccontrol.basic.model.vo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.enumtype.EnumIotDataType;
import com.persagy.apccontrol.basic.model.enumtype.websocket.WebSocketTypeEnum;
import com.persagy.apccontrol.basic.outside.dto.IotDataCommon;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 反馈值
 * 信息点数据对象
 *
 * @author yuyao
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedbackInfoMsgMqttVO extends IotDataCommon implements Comparable<FeedbackInfoMsgMqttVO> {

    @JsonAlias({"projectId"})
    @Schema(description = "项目id")
    private String projectId;

    @JsonAlias({"groupCode"})
    @Schema(description = "集团编码")
    private String groupCode;

    private String id;
    @Schema(description = "追踪 id")
    private String traceData;

    @Schema(description = "系统Id，非IOT数据，再接收IOT数据的时候写入，方便记录日志时使用")
    private String systemId;

    @Schema(description = "对象实例Id")
    private String objectId;

    @Schema(description = "对象本地编码，非IOT数据，再接收IOT数据的时候写入，方便记录日志时使用")
    private String localId;

    @Schema(description = "对象本地名称，非IOT数据，再接收IOT数据的时候写入，方便记录日志时使用")
    private String localName;

    @Schema(description = "对象类型")
    private String objType;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = " 操作人id 人工主动下发得时候传得参数")
    @JsonAlias({"user"})
    private String user;

    @Schema(description = "对象类编码")
    private String classCode;

    @Schema(description = "消息点编码")
    private String infoCode;

    @Schema(description = "上报时间 yyyyMMddHHmmss")
    private String dataTime;

    @Schema(description = "孪生值")
    @JsonAlias({"dateValue", "dataValue", "value"})
    private String dataValue;

    @Schema(description = "枚举类的值对应的名称")
    private String dataValueName;

    @Schema(description = "控制值 开始时间")
    private String startTime;

    @Schema(description = "控制值 结束时间(执行未完成为空) ")
    private String endTime;

    @Schema(description = "数据状态 反馈值：数据状态 1-正常，2-异常   控制值状态：  0:开始执行，1:执行中，2完成，3:保护期 ")
    @JsonAlias({"dataStatus", "status"})
    private Integer dataStatus;

    @Schema(description = "反馈值  效标志 0-有效 1-超时无效")
    @JsonAlias({"invalid", "valid"})
    private Integer invalid;

    @Schema(description = "控制点强制状态0-正常 1-强制")
    @JsonAlias({"forced"})
    private Integer forced;

    @Schema(description = "控制值状态  0:未完成，1:成功,2:失败，3:超时，4:强制终止，5:控制异常")
    @JsonAlias({"result"})
    private Integer result;

    @Schema(description = "时间常量")
    @JsonAlias({"dataIndate"})
    private Integer dataIndate;


    @JsonAlias({"dateTimeFromDate"})
    private String dateTimeFromDate;

    /**
     * timeRange是为false的和dataFlag是一个意思
     */
    @JsonAlias({"timeRange"})
    private String timeRange;

    @JsonAlias({"feedback"})
    private String feedback;

    @JsonAlias({"cmdLevel"})
    private String cmdLevel;

    private String formatMinuteTimeStr;

    private Date formatMinuteTime;


    @Schema(description = "型 0 反馈值 1 设置值")
    private Integer type;

    @Schema(description = "页面播报内容")
    private List<PageBroadcastContent> pageBroadcastContents;

    @Schema(description = "页面传输数据类型")
    private WebSocketTypeEnum webType;

    @Schema(description = "当前对象所属模块")
    private String modelType;

    private JSONObject changeContent;

    //扩展设置
    private Object extendSetting;

    public FeedbackInfoMsgMqttVO(String objectId, String classCode, String infoCode, String dataTime, String dataValue, Integer dataStatus) {
        this.objectId = objectId;
        this.classCode = classCode;
        this.infoCode = infoCode;
        this.dataTime = dataTime;
        this.dataValue = dataValue;
        this.dataStatus = dataStatus;
    }

    @Data
    public static class PageBroadcastContent {
        private String operateType;
        private String content;
    }

    @Override
    public int compareTo(@NotNull FeedbackInfoMsgMqttVO o) {

        Date a = null;
        Date b = null;
        if (StringUtils.isNotBlank(dataTime)) {
            a = DateUtils.str2Date(dataTime, DateUtils.SDFSECOND);
            b = DateUtils.str2Date(o.getDataTime(), DateUtils.SDFSECOND);
        }

        if (a == null || b == null) {
            return 0;
        }

        return (int) (b.getTime() - a.getTime());
    }

    public LocalDateTime toDataTime() {
        if (StringUtils.isBlank(dataTime)) {
            return null;
        }
        try {
            return LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern(DateUtils.SDFSECOND));
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
        //  return LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern(DateUtils.SDFSECOND));
    }
    
    /**
     * 把开始时间 时间挫格式转换为  LocalDateTime
     *
     * @return
     */
    public LocalDateTime toStartTime() {
        if (startTime == null) {
            return null;
        }
        Instant instant = Instant.ofEpochMilli(Long.parseLong(startTime));
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * 过期时间转换
     *
     * @return 过期时间可以为空
     */
    public LocalDateTime toDataIndicateTime() {
        if (StringUtils.isBlank(dataTime) || Objects.isNull(dataIndate) || dataIndate.equals(0)) {
            return null;
        }
        try {
            LocalDateTime parse = LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern(DateUtils.SDFSECOND));
            return parse.plusMinutes(dataIndate);
        } catch (Exception ex) {
            return null;
        }
    }


    /**
     * 判断是否为有效数据，当数据为控制值时，判断控制值状态等级不等于8则为无效值
     *
     * @return boolean
     * @author shaohongbo
     * @date 2023/5/25 16:23
     */
    public boolean checkValidData() {
        return type.equals(EnumIotDataType.CTRL.getCode()) && ((dataStatus == 0 || dataStatus == 1 || dataStatus == 2) && "8".equals(cmdLevel));
    }

    public boolean checkProcessData() {
        return type == 1 && result == 0 && (dataStatus == 1) && ("8".equals(cmdLevel) || "12".equals(cmdLevel));
    }

    public void bulidAlarmInfoCode() {
        this.setInfoCode("Csgc_" + this.getClassCode() + "_" + this.getInfoCode());
    }

    /**
     * 获取原始信息点
     */
    public String getOriginalInfoCode() {
        if (StringUtils.isEmpty(infoCode)) {
            return infoCode;
        }

        if (infoCode.contains(CommonConst.UNDERLINE)) {
            String[] r = infoCode.split(CommonConst.UNDERLINE);
            return r[r.length - 1];
        }
        return infoCode;
    }

    @Nullable
    private Date formatTimeToMinute(String dataTime) {
        Date t = DateUtils.str2Date(dataTime, DateUtils.SDFSECOND);
        // 时间格式化为分钟纬度
        t = DateUtils.getShortDate(t, DateUtils.SDF_MINUTE);
        return t;
    }

    public void formatAlarmMsg() {
        bulidAlarmInfoCode();
        Date dataTime = formatTimeToMinute(this.getDataTime());
        this.setFormatMinuteTimeStr(DateUtils.date2Str(dataTime, DateUtils.SDFSECOND));
        this.setFormatMinuteTime(dataTime);
    }
}
