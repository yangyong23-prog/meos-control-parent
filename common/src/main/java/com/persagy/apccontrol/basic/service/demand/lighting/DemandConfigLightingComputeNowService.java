package com.persagy.apccontrol.basic.service.demand.lighting;

import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandConfigLightingComputeParam;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;

import javax.validation.Valid;
import java.util.List;

/**
 * 照明配置结果计算
 *
 * @author yuecaipu
 * @version 1.0
 * @date 2024/3/27 14:37
 */
public interface DemandConfigLightingComputeNowService {
    
    /**
     * 照明配置结果计算（自动需求）
     * 1.获取场景
     * 2.获取照明所有的空间 和回路
     * 3.按照空间获取照明场景的需求配置
     * 4. 通过空间的需求计算最终的回路结果
     * @param demandConfigLightingComputeParam 计算参数
     */
    void resultCompute(@Valid DemandConfigLightingComputeParam demandConfigLightingComputeParam);
    
    
    /**
     * 查询配置
     * 用于测试 配置获取是否正确
     */
    List<DemandConfigAggregate<DemandConfigLightingValueObject>> queryCfg(@Valid DemandConfigLightingComputeParam demandConfigLightingComputeParam);
    
    
}
