package com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent;

import com.persagy.apccontrol.basic.outside.enums.ExceptionEventStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collection;

/**
 * 异常事件查询参数
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
public class ExceptionEventQueryParam {

    /**
     * 异常事件状态
     */
    private ExceptionEventStatus exceptionEventStatus;

    /**
     * 空间ID
     */
    private Collection<String> spaceIds;

    /**
     * 开始时间（查询历史的异常事件、即创建时间）
     */
    private LocalDateTime createStartTime;

    /**
     * 结束时间
     */
    private LocalDateTime createEndTime;

    /**
     * 对象类型
     */
    private String objectType;

}
