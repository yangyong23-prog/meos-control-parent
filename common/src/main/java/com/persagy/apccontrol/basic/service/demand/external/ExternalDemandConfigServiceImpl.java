package com.persagy.apccontrol.basic.service.demand.external;

import com.persagy.apccontrol.basic.model.param.external.demand.ExternalDemandConfigResultQueryParam;
import com.persagy.apccontrol.basic.model.vo.external.demand.ExternalDemandConfigResultVO;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 需求管理外部查询结果服务
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExternalDemandConfigServiceImpl implements ExternalDemandConfigService {
    
    @Override
    public List<? extends ExternalDemandConfigResultVO> listConfigResultsByInstance(ExternalDemandConfigResultQueryParam queryParam) {
        DemandStrategy demandStrategy = queryParam.getTemplateType().toDemandTypeEnum().demandStrategy();
        return demandStrategy.externalListConfigResultsByInstance(queryParam);
    }
}
