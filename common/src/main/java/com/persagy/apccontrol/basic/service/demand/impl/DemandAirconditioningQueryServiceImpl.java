package com.persagy.apccontrol.basic.service.demand.impl;

import com.persagy.apccontrol.basic.service.demand.airconditioning.DemandAirconditioningQueryService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 空调需求服务实现
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Service
@Validated
public class DemandAirconditioningQueryServiceImpl implements DemandAirconditioningQueryService {

    @Override
    public String logTag() {
        return "需求管理-空调需求";
    }
}
