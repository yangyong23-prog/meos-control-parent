package com.persagy.apccontrol.basic.resource.param.strategy;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Collection;

/**
 * 策略日期查询参数
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@Accessors(chain = true)
public class StrategyDateCfgQueryDaoParam {

    /**
     * 策略ID
     */
    private Long strategyId;

    /**
     * 策略ID
     */
    private Collection<Long> strategyIds;

    /**
     * 策略日期
     */
    private LocalDate date;

    /**
     * 策略开始日期
     */
    private LocalDate beginDate;

    /**
     * 策略结束日期
     */
    private LocalDate endDate;

    /**
     * 策略日期
     */
    private Collection<LocalDate> dates;

    /**
     * 状态
     */
    private StateTypeEnum status = StateTypeEnum.VALID;

}
