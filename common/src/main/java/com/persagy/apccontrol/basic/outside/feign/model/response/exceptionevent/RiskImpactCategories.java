package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.enums.CrucialEnum;
import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;

@Data
public class RiskImpactCategories extends BaseResponse {

    /**
     * 影响类型id
     */
    private String id;
    /**
     * 影响类型名称
     */
    private String name;
    /**
     * 关键性
     */
    private CrucialEnum crucial;
}
