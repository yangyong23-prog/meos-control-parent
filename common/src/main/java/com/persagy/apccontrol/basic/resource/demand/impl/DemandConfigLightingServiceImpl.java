package com.persagy.apccontrol.basic.resource.demand.impl;

import com.persagy.apccontrol.basic.config.dao.BaseServiceImpl;
import com.persagy.apccontrol.basic.mapper.demand.DemandConfigLightingMapper;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingDataDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingFuncTypeSettingDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingSpaceFuncTypeSettingDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.*;
import com.persagy.apccontrol.basic.structure.demand.converter.lighting.DemandConfigLightingConverter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-配置服务实现
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Service
@Validated
public class DemandConfigLightingServiceImpl
        extends BaseServiceImpl<DemandConfigLightingMapper, DemandConfigLighting>
        implements DemandConfigLightingService {

    @Autowired
    private DemandConfigLightingConverter demandConfigLightingConverter;
    @Autowired
    private DemandTemplateService demandTemplateService;

    @Override
    public DemandConfigLighting get(@NonNull String projectId, @NonNull DemandConfigLightingQueryDaoParam param) {
        param.setProjectId(projectId);
        return getBaseMapper().get(param);
    }

    @Override
    public List<DemandConfigLighting> list(
            @NonNull String projectId,
            @NonNull DemandConfigLightingQueryDaoParam param) {
        param.setProjectId(projectId);
        return getBaseMapper().list(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(@NonNull String projectId, @NonNull DemandConfigLightingSaveDaoParam param) {
        DemandTemplate dbTemplate = demandTemplateService.getByIdValidate(param.getRelDemandTemplateId());
        boolean isCustomizedSetting = analyzeIsCustomizedSetting(param.getConfigData());
        return saveValidate(demandConfigLightingConverter.toDo(
                new DemandConfigLighting(), projectId, isCustomizedSetting, dbTemplate, param));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean edit(@NonNull String projectId, @NonNull DemandConfigLightingUpdateDaoParam param) {
        DemandConfigLighting originConfig = getByIdValidate(param.getId());
        boolean isCustomizedSetting = analyzeIsCustomizedSetting(param.getGlobalSettings(), param.getSpaceSettings());
        DemandConfigLightingDataDTO dataDto = toDataDto(originConfig, param);
        return updateByIdValidate(demandConfigLightingConverter.toDo(
                originConfig, projectId, isCustomizedSetting, dataDto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editDomain(@NonNull Collection<DemandConfigLighting> domains) {
        if (CollectionUtils.isEmpty(domains)) {
            return false;
        }
        return updateByIdIgnore(domains);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearSetting(@NonNull String projectId, @NonNull DemandConfigLightingClearSettingDaoParam param) {
        DemandConfigLightingQueryDaoParam queryDaoParam = demandConfigLightingConverter.toQueryDaoParam(param);
        List<DemandConfigLighting> originConfigs = list(projectId, queryDaoParam);
        if (CollectionUtils.isEmpty(originConfigs)) {
            return false;
        }
        originConfigs = originConfigs.stream().map(item -> {
            DemandConfigLighting editConfig = new DemandConfigLighting();
            editConfig.setId(item.getId());
            editConfig.setConfigData(DemandConfigLightingDataDTO.newInstance());
            return editConfig;
        }).collect(Collectors.toList());
        return updateByIdIgnore(originConfigs);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean del(@NonNull String projectId, @NonNull DemandConfigLightingRemoveDaoParam param) {
        param.checkAndSet();
        param.setProjectId(projectId);
        Set<Long> toBeDelIds = list(projectId, demandConfigLightingConverter.toQueryDaoParam(param))
                .stream().map(BaseAssignIdDO::getId).collect(Collectors.toSet());
        return removeByIdIgnore(toBeDelIds);
    }

    /**
     * 转换配置数据DTO
     *
     * @param target 目标源
     * @param source 数据源
     * @return 配置数据DTO
     * @since 24Q1 T3S1-5.17.0
     */
    private DemandConfigLightingDataDTO toDataDto(
            @NonNull DemandConfigLighting target,
            @NonNull DemandConfigLightingUpdateDaoParam source) {
        DemandConfigLightingDataDTO configData = Optional.ofNullable(target.getConfigData())
                .orElseGet(DemandConfigLightingDataDTO::newInstance);
        if (Objects.nonNull(source.getGlobalSettings())) {
            configData.setGlobalSettings(source.getGlobalSettings());
        }
        if (Objects.nonNull(source.getSpaceSettings())) {
            configData.setSpaceSettings(source.getSpaceSettings());
        }
        return configData;
    }

    /**
     * 解析是否存在自定义配置
     *
     * @param configData 配置数据
     * @return 是否存在自定义配置
     * @since 24Q1 T3S1-5.17.0
     */
    @SneakyThrows
    private boolean analyzeIsCustomizedSetting(@NonNull DemandConfigLightingDataDTO configData) {
        return analyzeIsCustomizedSetting(configData.getGlobalSettings(), configData.getSpaceSettings());
    }

    /**
     * 解析是否存在自定义配置
     *
     * @param globalSettings 全局配置
     * @param spaceSettings  空间配置
     * @return 是否存在自定义配置
     * @since 24Q1 T3S1-5.17.0
     */
    @SneakyThrows
    private boolean analyzeIsCustomizedSetting(
            @Nullable List<DemandLightingFuncTypeSettingDTO> globalSettings,
            @Nullable List<DemandLightingSpaceFuncTypeSettingDTO> spaceSettings) {
        return null != globalSettings || CollectionUtils.isNotEmpty(spaceSettings);
    }
}
