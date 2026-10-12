package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandSceneMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandScene;
import com.persagy.apccontrol.basic.resource.demand.DemandSceneService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * 需求场景服务实现
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class DemandSceneServiceImpl
        extends BaseServiceImpl<DemandSceneMapper, DemandScene>
        implements DemandSceneService {

    @Override
    public Optional<DemandScene> getOpt(@NonNull DemandSceneQueryDaoParam param) {
        return list(param).stream().findFirst();
    }

    @Override
    public List<DemandScene> list(@NonNull DemandSceneQueryDaoParam param) {
        return getBaseMapper().list(ContextUtil.getProjectId(), param, param.toSelectColumns());
    }
}
