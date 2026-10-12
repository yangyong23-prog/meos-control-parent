package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import lombok.Data;

/**
 * 项目下营业时间
 */
@Data
public class MidCalendarOpenTimeProjectResponse {

    /**
     * 工作历 ID
     */
    private String workId;

    /**
     * 对象 id
     */
    private String objectId;

    /**
     * 工作历名称
     */
    private String name;

    /**
     * 营业时间 "10:00-12:00,14:00-20:00"
     */
    private String workTime;

}
