package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.DemandInstanceGroupDetailMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceGroupDetailResource;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupDetailQueryDaoParam;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandInstanceGroupDetailResourceImpl
        extends BaseServiceImpl<DemandInstanceGroupDetailMapper, DemandInstanceGroupDetail>
        implements DemandInstanceGroupDetailResource {

    @Override
    public List<DemandInstanceGroupDetail> list(@NonNull DemandInstanceGroupDetailQueryDaoParam param) {
        return lambdaQuery()
                .select(param.toSelectFunctions())
                .in(CollectionUtils.isNotEmpty(param.getInstanceIds()), DemandInstanceGroupDetail::getInstanceId, param.getInstanceIds())
                .eq(Objects.nonNull(param.getRelGroupId()), DemandInstanceGroupDetail::getRelGroupId, param.getRelGroupId())
                .in(CollectionUtils.isNotEmpty(param.getRelGroupIds()), DemandInstanceGroupDetail::getRelGroupId, param.getRelGroupIds())
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(@NonNull Set<Long> ids) {
        return removeByIdIgnore(ids);
    }
}
