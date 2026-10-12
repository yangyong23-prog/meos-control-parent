package com.persagy.apccontrol.basic.resource.param.strategy;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;

/**
 * 策略计划查询参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Accessors(chain = true)
public class StrategyPlanCfgQueryDaoParam {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 策略ID
     */
    private Long strategyId;

    /**
     * 策略ID
     */
    private Collection<Long> strategyIds;

    /**
     * 开始时间
     */
    private LocalTime startTime;

    /**
     * 开始时间
     */
    private LocalDateTime startDateTime;

    /**
     * 结束时间
     */
    private LocalTime endTime;

    /**
     * 结束时间
     */
    private LocalDateTime endDateTime;

    /**
     * 状态
     */
    private StateTypeEnum status = StateTypeEnum.VALID;

}
