package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 工作历日期标签查询参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@Accessors(chain = true)
public class CalendarObjectNumQueryParam {

    @Schema(description = "对象id")
    private String objectId;

    @Schema(description = "对象类型")
    private String objType;

}
