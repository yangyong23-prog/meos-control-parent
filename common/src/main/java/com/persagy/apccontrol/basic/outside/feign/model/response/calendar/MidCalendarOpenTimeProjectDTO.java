package com.persagy.apccontrol.basic.outside.feign.model.response.calendar;

import lombok.Data;
import org.apache.commons.lang3.Range;

import java.time.LocalTime;
import java.util.List;

/**
 * 项目下营业时间配置对象
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Data
public class MidCalendarOpenTimeProjectDTO {

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
     * 营业时间范围
     */
    private List<Range<LocalTime>> workTimeRanges;

}
