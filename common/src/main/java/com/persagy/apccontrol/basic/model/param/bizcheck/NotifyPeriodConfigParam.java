package com.persagy.apccontrol.basic.model.param.bizcheck;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 *
 * @description:
 * @author: wxf
 * @create: 2026-02-26 13:50
 **/
@Data
public class NotifyPeriodConfigParam {
    @Schema(description = "系统ID")
    private String id;
    @Schema(description = "系统名称")
    private String localName;
    @Schema(description = "系统编码")
    private String classCode;
    @Schema(description = "天数列表")
    private List<Integer> days;
}