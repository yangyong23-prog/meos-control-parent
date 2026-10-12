package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.dto.EEProblemDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.dto.ExceptionEventDTO;
import lombok.Data;

@Data
public class ExceptionEventOriginalProblemResponse {

    /**
     * 异常事件信息
     */
    private ExceptionEventDTO exceptionEvent;

    /**
     * 异常问题信息
     */
    private EEProblemDTO eeProblem;
}
