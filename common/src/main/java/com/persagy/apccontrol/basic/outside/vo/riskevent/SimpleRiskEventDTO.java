package com.persagy.apccontrol.basic.outside.vo.riskevent;

import lombok.Data;

@Data
public class SimpleRiskEventDTO {

    /**
     * 风险事件id
     */
    private String riskEventBusinessId;

    /**
     * 风险事件名称
     */
    private String riskEventName;
}
