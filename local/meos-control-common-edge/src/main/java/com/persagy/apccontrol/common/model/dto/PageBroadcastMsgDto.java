package com.persagy.apccontrol.common.model.dto;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.LogSourceEnum;
import com.persagy.framework.common.util.date.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Data
@Schema(title = "页面播报信息")
public class PageBroadcastMsgDto  implements Serializable {

    //--------------------公共参数------------------
    @Schema(description = "项目id")
    private String projectId;

    @Schema(description = "集团编码")
    private String groupCode;

    @Schema(description = "系统id")
    private String systemId;

    @Schema(description = "系统类型")
    private DtpRwdSystemClassCodeEnum systemType;

    @Schema(description = "日志来源")
    private LogSourceEnum logSource;

    @Schema(description = "操作类型")
    private BasicsConfigTypeEnum operateType;

    //--------------------播报内容------------------
    @Schema(description = "信息点")
    private RcInfoCodeCfg infoCodeCfg;

    @Schema(description = "对象id")
    private String objectId;

    @Schema(description = "对象本地id")
    private String localId;

    @Schema(description = "对象名称")
    private String localName;

    @Schema(description = "对象类型")
    private String classCode;

    @Schema(description = "操作时间")
    private LocalDateTime dateTime;

    @Schema(description = "设定值")
    private String dataValue;

    @Schema(description = "修改前的IOT值")
    private String beforeValue;

    @Schema(description = "操作人名称")
    private String userName;

    @Schema(description = "操作人id")
    private String userId;

    @Schema(description = "截止时间")
    private LocalDateTime endTime;

    @Schema(description = "调整原因")
    private String adjustReason;

    @Schema(description = "调整详情")
    private Map<String, Object> detail;


    /**
     * 将时间处理为yyyy-MM-dd HH:mm:ss格式
     * @return
     */
    public String getDateTime() {
        if (dateTime != null) {
            return dateTime.format(DateTimeFormatter.ofPattern(DateUtils.NORM_DATETIME_PATTERN));
        }
        return null;
    }

    /**
     * 将时间处理为yyyy-MM-dd HH:mm:ss格式
     * @return
     */
    public String getEndTime() {
        if (endTime != null) {
            return endTime.format(DateTimeFormatter.ofPattern(DateUtils.NORM_DATETIME_PATTERN));
        }
        return null;
    }
}
