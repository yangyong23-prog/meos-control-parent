package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandInstanceConfigMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.resource.demand.DemandInstanceConfigService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigUpdateDaoParam;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandInstanceConfigConverter;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求实例配置服务实现
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class DemandInstanceConfigServiceImpl
        extends BaseServiceImpl<DemandInstanceConfigMapper, DemandInstanceConfig>
        implements DemandInstanceConfigService {

    private final DemandInstanceConfigConverter demandInstanceConfigConverter;

    @Override
    public Optional<DemandInstanceConfig> getOpt(@NonNull DemandInstanceConfigQueryDaoParam param) {
        return list(param).stream().findFirst();
    }

    @Override
    public List<DemandInstanceConfig> list(@NonNull DemandInstanceConfigQueryDaoParam param) {
        return getBaseMapper().list(ContextUtil.getProjectId(), param, param.toSelectColumns());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(
            @NonNull String projectId,
            @NonNull DemandInstanceConfigSaveDaoParam param) {
        return saveValidate(demandInstanceConfigConverter.toDo(new DemandInstanceConfig(), projectId, param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(
            @NonNull String projectId,
            @Nullable Collection<DemandInstanceConfigSaveDaoParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return false;
        }
        return saveValidate(params.stream().map(item -> demandInstanceConfigConverter.toDo(
                new DemandInstanceConfig(), projectId, item)).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(
            @NonNull String projectId,
            @NonNull DemandInstanceConfigUpdateDaoParam param) {
        return updateByIdValidate(demandInstanceConfigConverter.toDo(getByIdValidate(param.getId()), projectId, param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(
            @NonNull String projectId,
            @Nullable Collection<DemandInstanceConfigUpdateDaoParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return false;
        }
        Map<Long, DemandInstanceConfigUpdateDaoParam> idAndParam = params.stream().collect(Collectors.toMap(
                DemandInstanceConfigUpdateDaoParam::getId, Function.identity(), (o1, o2) -> o1));
        List<DemandInstanceConfig> dbEntities = listByIdValidate(idAndParam.keySet());
        List<DemandInstanceConfig> editParams = dbEntities.stream()
                .map(item -> demandInstanceConfigConverter.toDo(item, projectId, idAndParam.get(item.getId())))
                .collect(Collectors.toList());
        return updateByIdValidate(editParams);
    }
}
