package com.persagy.apccontrol.service.external.impl;

import com.persagy.apccontrol.basic.model.param.demand.airconditioning.DemandAirconditioningObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.airconditioning.DemandAirconditioningObjectVO;
import com.persagy.apccontrol.service.external.DemandAirconditioningExternalService;
import org.apache.commons.compress.utils.Lists;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 需求管理外部服务实现
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>listControlledObject</b> - 查询管控对象列表</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Service
public class DemandAirconditioningExternalServiceImpl implements DemandAirconditioningExternalService {

    @Override
    public List<DemandAirconditioningObjectVO> listControlledObject(
            @NonNull DemandAirconditioningObjectQueryParam param) {
        param.checkAndSet();
        return Lists.newArrayList();
    }
}
