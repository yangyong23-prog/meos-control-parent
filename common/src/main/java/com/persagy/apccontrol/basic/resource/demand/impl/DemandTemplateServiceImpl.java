package com.persagy.apccontrol.basic.resource.demand.impl;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandTemplateMapper;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateSaveDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateUpdateDaoParam;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandTemplateConverter;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求模版服务实现
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class DemandTemplateServiceImpl
        extends BaseServiceImpl<DemandTemplateMapper, DemandTemplate>
        implements DemandTemplateService {

    private final IdentifierGenerator identifierGenerator;
    private final DemandTemplateConverter demandTemplateConverter;

    @Override
    public Optional<DemandTemplate> get(@NonNull DemandTemplateQueryDaoParam param) {
        return list(param).stream().findFirst();
    }

    @Override
    public DemandTemplate getEnabledByDemandType(@NonNull Long templateId, @NonNull DemandTypeEnum demandType) {
        DemandTemplate result = getByIdValidate(templateId);
        return Objects.nonNull(result) && result.getSt() == StateTypeEnum.VALID
                && BooleanUtils.isTrue(result.getIsEnabled()) ? result : null;
    }

    @Override
    public List<DemandTemplate> list(@NonNull DemandTemplateQueryDaoParam param) {
        List<DemandTemplate> result = getBaseMapper().list(ContextUtil.getProjectId(), param, param.toSelectColumns());
        result.sort(Comparator.comparingInt(o -> o.toDemandTemplateTagEnum().getOrdered()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(@NonNull String projectId, @NonNull DemandTemplateSaveDaoParam param) {
        return saveValidate(demandTemplateConverter.toDo(new DemandTemplate(), projectId, param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(@NonNull String projectId, @NonNull Collection<DemandTemplateSaveDaoParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return false;
        }
        List<DemandTemplate> addParams = params.stream()
                .map(item -> demandTemplateConverter.toDo(new DemandTemplate(), projectId, item))
                .collect(Collectors.toList());
        return saveBatch(addParams);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DemandTemplate> addCustom(@NonNull String projectId, @NonNull Collection<DemandTemplateSaveDaoParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Lists.newArrayList();
        }
        List<DemandTemplate> addParams = params.stream()
                .map(item -> demandTemplateConverter.toDo(new DemandTemplate(), projectId, item))
                .collect(Collectors.toList());
        addParams.forEach(this::fillCustomId);
        saveBatch(addParams);
        return addParams;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(@NonNull String projectId, @NonNull DemandTemplateUpdateDaoParam param) {
        return SqlHelper.retBool(getBaseMapper().edit(param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DemandTemplate> edit(
            @NonNull String projectId,
            @NonNull Collection<DemandTemplateUpdateDaoParam> params) {
        if (CollectionUtils.isEmpty(params)) {
            return Lists.newArrayList();
        }
        params.forEach(item -> getBaseMapper().edit(item));
        return listByIdIgnore(params.stream().map(DemandTemplateUpdateDaoParam::getId).collect(Collectors.toSet()));
    }

    /**
     * 填充自定义模版ID
     *
     * @param entity 实体
     * @return 实体
     * @since v6.0.0
     */
    private DemandTemplate fillCustomId(@NonNull DemandTemplate entity) {
        entity.setId(identifierGenerator.nextId(entity).longValue());
        entity.setDemandTemplateTag(entity.getId().toString());
        return entity;
    }
}
