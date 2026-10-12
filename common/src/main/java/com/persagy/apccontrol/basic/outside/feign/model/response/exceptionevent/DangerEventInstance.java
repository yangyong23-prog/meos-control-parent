package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.model.enumtype.dangerevent.StatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 风险事件响应
 *
 * @author wxf
 * @since 7.250201.0
 */
@Data
public class DangerEventInstance {

    /**
     * 风险事件ID
     */
    private String id;

    /**
     * 风险事件名
     */
    private String name;

    /**
     * 风险事件状态
     */
    private StatusEnum currentStatus;

    /**
     * 状态更新时间
     */
    private LocalDateTime statusUpdateTime;

    /**
     * 是否关注
     */
    private boolean followMark;

    /**
     * 对象实例id
     */
    private String rwdInstanceId;

}
