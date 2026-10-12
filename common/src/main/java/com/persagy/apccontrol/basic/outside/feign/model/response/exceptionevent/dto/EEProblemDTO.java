package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.dto;

import com.persagy.apccontrol.basic.outside.vo.riskevent.SimpleRiskEventDTO;
import lombok.Data;

import java.util.List;

@Data
public class EEProblemDTO {

    /** 问题id */
    private Long id;
    /** 问题描述(名称) */
    private String problemDesc;
    /** 问题实例id */
    private String rwdInstanceId;
    /** 关联风险事件 */
    private List<SimpleRiskEventDTO> riskEventList;
}
