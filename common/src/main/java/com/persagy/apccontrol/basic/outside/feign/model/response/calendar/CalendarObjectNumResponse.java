package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 工作历引用数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ToString(callSuper = true)
public class CalendarObjectNumResponse   {

    /**
     * 工作历id
     */
    private String workId;
    /**
     * 对象idd
     */
    private String objectId;
    /**
     * 工作历名称
     */
    private String name;
    /**
     * 引用数
     */
    private Integer num;
    /**
     * 引用对象ids
     */
    private List<String> objects;
}
