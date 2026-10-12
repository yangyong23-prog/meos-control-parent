package com.persagy.apccontrol.basic.resource.demand.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigService;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 需求配置服务实现
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigServiceImpl
        extends BaseServiceImpl<DemandConfigMapper, DemandConfig>
        implements DemandConfigService {

    @Override
    public int countExistsCustomizedInstance(@Nullable LocalDate date, @NonNull Long templateId) {
        return getBaseMapper().countExistsCustomizedInstance(ContextUtil.getProjectId(), date, templateId);
    }

    @Override
    public List<DemandConfig> listByInstances(@NonNull DemandConfigByInstanceRepositoryBatchQueryParam param) {
        return getBaseMapper().listByInstances(ContextUtil.getProjectId(), param, param.toSelectColumns());
    }

    @Override
    public void editConfig(@NonNull Collection<Long> configIds, @NonNull JsonNode configData) {
        lambdaUpdate().in(BaseAssignIdDO::getId, configIds).set(DemandConfig::getConfigData, configData);
    }
}
