package com.persagy.apccontrol.basic.outside.feign.model.param.calendar;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

/**
 * 工作历查询参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@Accessors(chain = true)
public class MidCalendarQueryParam {

    /**
     * 工作历ID
     */
    @Nullable
    private String workId;

    /**
     * 对象实例ID
     */
    @Nullable
    private String objId;

}
