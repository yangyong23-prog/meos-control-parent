package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigGroupMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigGroupService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandConfigConverter;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryQueryParam;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * 需求配置组服务实现
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigGroupServiceImpl extends
        BaseServiceImpl<DemandConfigGroupMapper, DemandConfigGroup>
        implements DemandConfigGroupService {

    private final DemandConfigConverter demandConfigConverter;

    @Override
    public Optional<DemandConfigGroup> getOpt(@NonNull DemandConfigGroupRepositoryQueryParam param) {
        return this.list(demandConfigConverter.toGroupBatchQueryParam(param)).stream().findFirst();
    }

    @Override
    public List<DemandConfigGroup> list(@NonNull DemandConfigGroupRepositoryBatchQueryParam param) {
        return getBaseMapper().list(ContextUtil.getProjectId(), param, param.toSelectColumns());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandConfigGroup add(@NonNull DemandConfigGroup configGroup) {
        saveValidate(configGroup);
        return configGroup;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DemandConfigGroup> add(@NonNull List<DemandConfigGroup> configGroups) {
        saveValidate(configGroups);
        return configGroups;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editGroupName(@NonNull Long configGroupId, @NonNull String groupName) {
        lambdaUpdate().eq(BaseAssignIdDO::getId, configGroupId).set(DemandConfigGroup::getGroupName, groupName).update();
    }
}
