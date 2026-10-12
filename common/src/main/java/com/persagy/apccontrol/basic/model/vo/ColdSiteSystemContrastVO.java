package com.persagy.apccontrol.basic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 冷站系统数据对比
 *
 * @author yuyao
 */
@Data
public class ColdSiteSystemContrastVO {

    @Schema(description = "对象id")
    private String objectId;
    @Schema(description = "对象名称")
    private String localName;
    @Schema(description = "对象信息点")
    private String infoCode;

    @Schema(description = "对象信息点名称")
    private String infoCodeName;
    @Schema(description = "设定值")
    private Object value;

    @Schema(description = "实际值")
    private Object actualValue;

    @Schema(description = "人工干预截止时间")
    private LocalDateTime timeDuration;

    @Schema(description = "是否是人工 true人工 false 自动")
    private Boolean artificial;

    @Schema(description = "设备开启时间")
    private LocalTime openTime;

    @Schema(description = "设备关闭时间")
    private LocalTime closeTime;

}
