package com.persagy.apccontrol.basic.service.demand.external;


import com.persagy.apccontrol.basic.model.param.external.demand.ExternalDemandConfigResultQueryParam;
import com.persagy.apccontrol.basic.model.vo.external.demand.ExternalDemandConfigResultVO;

import java.util.List;

/**
 * 需求管理外部查询结果服务
 */
public interface ExternalDemandConfigService {
    
    /**
     * 查询需求配置结果
     *
     * @param queryParam
     * @return
     */
    public List<? extends ExternalDemandConfigResultVO> listConfigResultsByInstance(ExternalDemandConfigResultQueryParam queryParam);
    
}
