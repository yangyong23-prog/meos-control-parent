package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.dto;

import com.persagy.apccontrol.basic.outside.enums.ExceptionEventStatus;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.ImportanceDegreeEnum;
import com.persagy.apccontrol.basic.outside.vo.riskevent.SolveProcessEnum;
import lombok.Data;

@Data
public class ExceptionEventDTO {

    /**
     * 异常事件id
     */
    private Long id;

    /**
     * 异常事件编号
     */
    private String eventNumber;

    /**
     * 持续时间
     */
    private Long duration;

    /**
     * 异常开始时间
     */
    private String exceptionStartTime;

    /**
     * 异常事件状态
     */
    private ExceptionEventStatus exceptionEventStatus;

    /**
     * 重要程度
     */
    private ImportanceDegreeEnum importanceDegree;

    /**
     * 是否逾期
     */
    private Boolean overdue;

    /**
     * 处理进程
     */
    private SolveProcessEnum solveProcess;

    /**
     * 最早需要解决时间
     */
    private Long minRequireSolveTime;

    /**
     * 最晚所需解决时间
     */
    private Long maxRequireSolveTime;

}
