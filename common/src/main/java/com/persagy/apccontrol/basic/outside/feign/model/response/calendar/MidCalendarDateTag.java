package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author zhangyanchang
 * @version 1.0
 * @title: MidCalendarDateTag
 * @description: Never give up
 * @date: 2023/12/4 10:26
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MidCalendarDateTag extends BaseResponse {

    private String groupCode;

    private String workId;

    private String projectId;

    private String labelDate;

    private String dictType;

    private String dictCode;

    private String diyLabel;
}
