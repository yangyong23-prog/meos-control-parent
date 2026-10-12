package com.persagy.apccontrol.common.structure.demand.migrate;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.*;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigGroupNameCheckParam;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingConfigMigrateMessageVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.DemandSpaceFuncTypeConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingCircuitValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children.DemandConfigLightingFuncTypeValueObject;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigGroupRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigRepository;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigAddOrEditParam;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryRemoveParam;
import com.persagy.apccontrol.basic.task.BaseTask;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 需求迁移照明场景任务
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandMigrateLightingConfigTask extends BaseTask {

    private final DemandConfigLightingService demandConfigLightingService;
    private final DemandConfigGroupRepository demandConfigGroupRepository;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final DemandConfigService demandConfigService;
    private final DemandTemplateService demandTemplateService;
    private final DemandConfigRepository demandConfigRepository;
    private final InstanceService instanceService;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 迁移
     *
     * @param templateType 模版类型, 为空时迁移所有模版
     * @since 7.250801.0
     */
    @SneakyThrows
    public void migrate(@Nullable DemandTemplateTypeEnum templateType, @Nullable Boolean forceMigrate) {
        handlerProjectByContext(project -> {
            String projectId = ContextUtil.getProjectId();
            DemandLightingConfigMigrateMessageVO result = new DemandLightingConfigMigrateMessageVO();
            List<DemandConfigGroup> configGroups = new ArrayList<>();
            List<DemandConfigGroup> roomFuncTypeConfigGroups = new ArrayList<>();
            List<DemandLightingConfigMigrateMessageVO.Detail> details = new ArrayList<>();
            try {
                transactionTemplate.execute(status -> {
                    // 强制迁移时删除照明相关所有的配置和关联的配置分组
                    if (BooleanUtils.isTrue(forceMigrate)) {
                        clearLightingConfig(templateType);
                    }
                    Map<String, String> roomFuncTypeAndName = new HashMap<>();
                    DtpKnowledgeInfoCodeDTO roomFuncTypeDefine = dtpKnowledgeClientService.getInfoCode(
                            DtpRwdClassCodeEnum.SPACE.value(), DtpRwdSpaceDTO.Fields.roomFuncType);
                    if (Objects.nonNull(roomFuncTypeDefine)) {
                        roomFuncTypeAndName = DtpKnowledgeInfoCodeDTO.toDataSourceCodeAndName(roomFuncTypeDefine.getDataSources());
                    }
                    int pageNo = 1;
                    while (true) {
                        List<DemandConfigLighting> lightingConfigs = demandConfigLightingService.lambdaQuery()
                                .eq(StringUtils.isNotBlank(projectId), DemandConfigLighting::getProjectId, ContextUtil.getProjectId())
                                .eq(Objects.nonNull(templateType), DemandConfigLighting::getDemandTemplateType, templateType)
                                .page(new Page<>(pageNo++, 1000, false)).getRecords();
                        if (CollectionUtils.isEmpty(lightingConfigs)) {
                            break;
                        }
                        Map<String, String> objectIdAndGroupName = mapObjectIdAndGroupName(lightingConfigs);
                        List<DemandConfig> configs = new ArrayList<>();
                        for (DemandConfigLighting lightingConfig : lightingConfigs) {
                            // 新增配置分组
                            AtomicInteger groupNameRepeatCount = new AtomicInteger(1);
                            DemandConfigGroup configGroup = addDemandConfigGroupByMigrate(
                                    lightingConfig.getProjectId(),
                                    objectIdAndGroupName.get(lightingConfig.getDemandInstanceId()),
                                    lightingConfig.getRelDemandTemplateId(),
                                    lightingConfig.getRelDemandLightingSceneId(),
                                    repeatGroupName -> repeatGroupName + "-" + groupNameRepeatCount.getAndIncrement()
                            );
                            configGroups.add(configGroup);
                            configGroup = demandConfigGroupRepository.add(configGroup);
                            DemandConfigValueObject<DemandConfigLightingValueObject> configData = convertConfig(lightingConfig);
                            // 无效配置时跳过
                            if (Objects.isNull(configData)) {
                                continue;
                            }
                            // 存在空间功能类型配置时, 添加空间功能类型配置分组
                            if (CollectionUtils.isNotEmpty(configData.getSpaceFuncTypeConfigs())) {
                                for (DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject> spaceFuncTypeConfig
                                        : configData.getSpaceFuncTypeConfigs()) {
                                    List<DtpKnowledgeInfoCodeDataSourceDTO> funcTypes = spaceFuncTypeConfig.getFuncTypes();
                                    StringJoiner roomFuncTypeGroupNameJoiner = new StringJoiner("、");
                                    for (DtpKnowledgeInfoCodeDataSourceDTO funcType : funcTypes) {
                                        roomFuncTypeGroupNameJoiner.add(roomFuncTypeAndName.get(funcType.getCode()));
                                    }
                                    DemandConfigGroup roomFuncTypeConfigGroup = addDemandConfigGroupByMigrate(
                                            lightingConfig.getProjectId(),
                                            roomFuncTypeGroupNameJoiner.toString(),
                                            lightingConfig.getRelDemandTemplateId(),
                                            lightingConfig.getRelDemandLightingSceneId(),
                                            repeatGroupName -> repeatGroupName + "-" + groupNameRepeatCount.getAndIncrement()
                                    );
                                    roomFuncTypeConfigGroups.add(roomFuncTypeConfigGroup);
                                    roomFuncTypeConfigGroup = demandConfigGroupRepository.add(roomFuncTypeConfigGroup);
                                    spaceFuncTypeConfig.setRelConfigGroupId(roomFuncTypeConfigGroup.getId());
                                }
                            }
                            DemandConfig demandConfig = DemandConfig.of(lightingConfig, configData, configGroup.getId());
                            DemandLightingConfigMigrateMessageVO.Detail detail =
                                    new DemandLightingConfigMigrateMessageVO.Detail();
                            detail.setOldConfig(lightingConfig);
                            detail.setNewConfig(demandConfig);
                            details.add(detail);
                            configs.add(demandConfig);
                        }
                        if (CollectionUtils.isNotEmpty(configs)) {
                            for (DemandConfig config : configs) {
                                DemandConfigAddOrEditParam addParam = new DemandConfigAddOrEditParam();
                                addParam.setPublishEvent(false);
                                addParam.setTemplateId(config.getRelTemplateId());
                                addParam.setSceneId(config.getRelSceneId());
                                addParam.setConfigGroupId(config.getRelGroupId());
                                addParam.setInstanceType(config.getDemandInstanceType());
                                addParam.setInstanceIds(Sets.newHashSet(config.getDemandInstanceId()));
                                addParam.setConfigData(objectMapper.convertValue(config.getConfigData(),
                                        new TypeReference<DemandConfigValueObject<DemandConfigLightingValueObject>>() {
                                        }));
                                demandConfigRepository.addOrEditConfig(addParam);
                            }
                        }
                    }
                    return null;
                });
            } catch (Exception e) {
                logProjectInstance().error("迁移数据失败", e);
                result.setErrorMessage(e.getLocalizedMessage());
            }
            result.setConfigGroups(configGroups);
            result.setRoomFuncTypeConfigGroups(roomFuncTypeConfigGroups);
            result.setDetails(details);
            if (StringUtils.isNotBlank(result.getErrorMessage())) {
                logProjectInstance().error("迁移数据失败", Pair.of("result", result));
            } else {
                logProjectInstance().info("输出结果", Pair.of("result", result));
            }
        });
    }

    /**
     * 创建配置实例
     *
     * @param lightingConfig 照明配置
     * @return 配置实例
     * @since 7.250801.0
     */
    public DemandConfigValueObject<DemandConfigLightingValueObject> convertConfig(
            @NonNull DemandConfigLighting lightingConfig) {
        DemandConfigLightingDataDTO configData = lightingConfig.getConfigData();
        if (Objects.isNull(configData)) {
            return null;
        }
        DemandInstanceTypeEnum instanceType = lightingConfig.getDemandInstanceType();
        String instanceId = lightingConfig.getDemandInstanceId();
        List<DemandConfigLightingFuncTypeValueObject> globalSettings =
                convertGlobalSettings(configData.getGlobalSettings());
        DemandConfigValueObject<DemandConfigLightingValueObject> result = new DemandConfigValueObject<>();
        DemandConfigLightingValueObject config = new DemandConfigLightingValueObject();
        if (CollectionUtils.isNotEmpty(globalSettings)) {
            config.setGlobalSettings(globalSettings);
            result.setGlobalConfig(config);
        }
        // 为项目时, 添加空间功能类型配置
        if (instanceType == DemandInstanceTypeEnum.PROJECT) {
            List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> spaceFuncTypeConfigs =
                    convertSpaceSettings(configData.getSpaceSettings());
            if (CollectionUtils.isNotEmpty(spaceFuncTypeConfigs)) {
                result.setSpaceFuncTypeConfigs(spaceFuncTypeConfigs);
            }
        }
        return result;
    }

    /**
     * 转换空间功能类型配置
     *
     * @param spaceSettings 空间功能类型配置
     * @return 转换后的空间功能类型配置
     * @since 7.250801.0
     */
    public @Nullable List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> convertSpaceSettings(
            @NonNull List<DemandLightingSpaceFuncTypeSettingDTO> spaceSettings) {
        if (CollectionUtils.isEmpty(spaceSettings)) {
            return null;
        }
        List<DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject>> finalSpaceConfigs = new ArrayList<>();
        for (DemandLightingSpaceFuncTypeSettingDTO spaceSetting : spaceSettings) {
            List<BaseEnumVO> spaceFuncTypes = spaceSetting.getSpaceFuncTypes();
            List<DemandConfigLightingFuncTypeValueObject> finalFuncTypeSettings =
                    convertGlobalSettings(spaceSetting.getFuncTypeSettings());
            if (CollectionUtils.isEmpty(finalFuncTypeSettings)) {
                continue;
            }
            DemandConfigLightingValueObject finalFuncTypeConfig = new DemandConfigLightingValueObject();
            finalFuncTypeConfig.setGlobalSettings(finalFuncTypeSettings);
            DemandSpaceFuncTypeConfig<DemandConfigLightingValueObject> finalSpaceConfig = new DemandSpaceFuncTypeConfig<>();
            finalSpaceConfig.setFuncTypes(spaceFuncTypes.stream()
                    .map(item -> new DtpKnowledgeInfoCodeDataSourceDTO(String.valueOf(item.getKey()),
                            String.valueOf(item.getValue())))
                    .collect(Collectors.toList()));
            finalSpaceConfig.setConfig(finalFuncTypeConfig);
            finalSpaceConfigs.add(finalSpaceConfig);
        }
        return finalSpaceConfigs;
    }

    /**
     * 转换全局配置
     *
     * @param globalSettings 全局配置
     * @return 转换后的全局配置
     * @since 7.250702.0
     */
    private @Nullable List<@NonNull DemandConfigLightingFuncTypeValueObject> convertGlobalSettings(
            @Nullable List<DemandLightingFuncTypeSettingDTO> globalSettings) {
        if (CollectionUtils.isEmpty(globalSettings)) {
            return null;
        }
        List<DemandConfigLightingFuncTypeValueObject> finalGlobalSettings = new ArrayList<>();
        for (DemandLightingFuncTypeSettingDTO globalSetting : globalSettings) {
            DemandLightingTypeSettingModeEnum settingMode = globalSetting.getSettingMode();
            List<DemandLightingCircuitSettingDTO> circuitSettings = globalSetting.getCircuitSettings();
            List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings = globalSetting.getCircuitGroupSettings();
            if (settingMode == DemandLightingTypeSettingModeEnum.NOT_CONFIGURED) {
                continue;
            }
            DemandLightingTypeSettingModeEnum finalSettingMode = settingMode;
            List<DemandConfigLightingCircuitValueObject> finalCircuitSettings = null;
            // 处理配置模式
            if (settingMode == DemandLightingTypeSettingModeEnum.PARTIAL_OPEN
                    && CollectionUtils.isNotEmpty(circuitGroupSettings)) {
                finalSettingMode = DemandLightingTypeSettingModeEnum.INSTANCE_GROUP;
            }
            // 处理照明回路配置
            if (CollectionUtils.isNotEmpty(circuitSettings)) {
                finalCircuitSettings = new ArrayList<>();
                for (DemandLightingCircuitSettingDTO circuitSetting : circuitSettings) {
                    DemandLightingCircuitSettingModeEnum circuitSettingMode = circuitSetting.getSettingMode();
                    if (circuitSettingMode == DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED) {
                        continue;
                    }
                    DemandConfigLightingCircuitValueObject finalCircuitSetting = new DemandConfigLightingCircuitValueObject();
                    finalCircuitSetting.setCircuitId(circuitSetting.getCircuitId());
                    finalCircuitSetting.setSettingMode(circuitSetting.getSettingMode());
                    finalCircuitSettings.add(finalCircuitSetting);
                }
                finalCircuitSettings = CollectionUtils.isEmpty(finalCircuitSettings) ? null : finalCircuitSettings;
            }
            if (finalSettingMode == DemandLightingTypeSettingModeEnum.PARTIAL_OPEN
                    && CollectionUtils.isEmpty(finalCircuitSettings)) {
                continue;
            }
            DemandConfigLightingFuncTypeValueObject finalGlobalSetting = new DemandConfigLightingFuncTypeValueObject();
            finalGlobalSetting.setFuncType(globalSetting.getFuncType().value());
            finalGlobalSetting.setSettingMode(finalSettingMode);
            if (finalSettingMode == DemandLightingTypeSettingModeEnum.PARTIAL_OPEN
                    && CollectionUtils.isNotEmpty(finalCircuitSettings)) {
                finalGlobalSetting.setCircuitSettings(finalCircuitSettings);
            }
            if (finalSettingMode == DemandLightingTypeSettingModeEnum.INSTANCE_GROUP
                    && CollectionUtils.isNotEmpty(circuitGroupSettings)) {
                finalGlobalSetting.setCircuitGroupSettings(circuitGroupSettings);
            }
            finalGlobalSettings.add(finalGlobalSetting);
        }
        return CollectionUtils.isNotEmpty(finalGlobalSettings) ? finalGlobalSettings : null;
    }

    private Map<String, String> mapObjectIdAndGroupName(@NonNull List<DemandConfigLighting> lightingConfigs) {
        if (CollectionUtils.isEmpty(lightingConfigs)) {
            return Collections.emptyMap();
        }
        Map<String, DemandInstanceTypeEnum> instanceIdAndInstanceType = new HashMap<>();
        Map<DemandInstanceTypeEnum, Collection<String>> instanceTypeAndIds = new HashMap<>();
        for (DemandConfigLighting lightingConfig : lightingConfigs) {
            instanceIdAndInstanceType.put(lightingConfig.getDemandInstanceId(), lightingConfig.getDemandInstanceType());
            MapExecutor.dynamicPut(instanceTypeAndIds, lightingConfig.getDemandInstanceType(),
                    lightingConfig.getDemandInstanceId(), HashSet::new);
        }
        int buildingCount = dtpRwdClientService.countObject(new DtpRwdObjectQueryParam()
                .setClassCode(DtpRwdClassCodeEnum.BUILDING));
        Map<String, String> objectIdAndLocalName = dtpRwdClientService.mapObjectIdAndLocalName(
                lightingConfigs.stream().map(DemandConfigLighting::getDemandInstanceId).collect(Collectors.toList()));
        Map<String, List<DemandInstanceValueObject>> instanceIdAndAllParents =
                instanceService.mapInstanceIdAndAllParents(instanceTypeAndIds);
        if (MapUtils.isEmpty(instanceIdAndAllParents)) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, List<DemandInstanceValueObject>> entry : instanceIdAndAllParents.entrySet()) {
            String instanceId = entry.getKey();
            Map<DemandInstanceTypeEnum, String> parentInstanceTypeAndLocalName = entry.getValue().stream().collect(
                    Collectors.toMap(DemandInstanceValueObject::getInstanceType, DemandInstanceValueObject::getInstanceName));

            DemandInstanceTypeEnum instanceType = instanceIdAndInstanceType.get(instanceId);
            if (Objects.isNull(instanceType)) {
                continue;
            }
            String instanceName = objectIdAndLocalName.get(instanceId);
            String floorName = "(" + parentInstanceTypeAndLocalName.get(DemandInstanceTypeEnum.FLOOR) + ")";
            String buildingName = buildingCount > 1
                    ? "(" + parentInstanceTypeAndLocalName.get(DemandInstanceTypeEnum.BUILDING) + ")"
                    : "";
            String finalInstanceName = instanceName;
            switch (instanceType) {
                case PROJECT:
                case BUILDING:
                    break;
                case FLOOR:
                    finalInstanceName = instanceName + buildingName;
                    break;
                case SPACE:
                    finalInstanceName = instanceName + floorName + buildingName;
            }
            result.put(instanceId, finalInstanceName);
        }
        return result;
    }

    /**
     * 根据迁移新增配置分组
     *
     * @param projectId         项目ID
     * @param groupName         分组名称
     * @param templateId        模版ID
     * @param sceneId           场景ID
     * @param groupNameIfExists 当分组名称已存在时处理
     * @return 新增的配置分组
     * @since 7.250801.0
     */
    private DemandConfigGroup addDemandConfigGroupByMigrate(
            @NonNull String projectId,
            @NonNull String groupName,
            @NonNull Long templateId,
            @NonNull Long sceneId,
            @NonNull Function<String, String> groupNameIfExists) {
        DemandConfigGroup configGroup = new DemandConfigGroup();
        configGroup.setProjectId(projectId);
        configGroup.setGroupName(StringUtils.defaultIfBlank(groupName, DemandConfigGroupRepository.FICTITIOUS_GROUP_NAME));
        configGroup.setRelTemplateId(templateId);
        configGroup.setRelSceneId(sceneId);
        DemandConfigGroupNameCheckParam groupNameCheckParam = new DemandConfigGroupNameCheckParam();
        groupNameCheckParam.setConfigGroupName(configGroup.getGroupName());
        groupNameCheckParam.setTemplateId(configGroup.getRelTemplateId());
        groupNameCheckParam.setSceneId(configGroup.getRelSceneId());
        while (demandConfigGroupRepository.checkConfigGroupNameExists(groupNameCheckParam)) {
            configGroup.setGroupName(groupNameIfExists.apply(configGroup.getGroupName()));
            groupNameCheckParam.setConfigGroupName(configGroup.getGroupName());
        }
        return configGroup;
    }

    /**
     * 清空照明配置
     *
     * @param templateType 模版类型, 为空时清空所有模版
     * @since 7.250801.0
     */
    private void clearLightingConfig(@org.springframework.lang.Nullable DemandTemplateTypeEnum templateType) {
        String projectId = ContextUtil.getProjectId();
        List<Long> templateIds = demandTemplateService.lambdaQuery().select(BaseAssignIdDO::getId)
                .eq(StringUtils.isNotBlank(projectId), DemandTemplate::getProjectId, ContextUtil.getProjectId())
                .eq(DemandTemplate::getDemandType, DemandTypeEnum.LIGHTING)
                .eq(Objects.nonNull(templateType), DemandTemplate::getDemandTemplateType, templateType)
                .list().stream()
                .map(BaseAssignIdDO::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(templateIds)) {
            Set<Long> configIds = demandConfigService.lambdaQuery()
                    .select(BaseAssignIdDO::getId)
                    .in(DemandConfig::getRelTemplateId, templateIds)
                    .list().stream()
                    .map(BaseAssignIdDO::getId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(configIds)) {
                DemandConfigByInstanceRepositoryRemoveParam removeParam =
                        new DemandConfigByInstanceRepositoryRemoveParam();
                removeParam.setPublishEvent(false);
                removeParam.setConfigIds(configIds);
                demandConfigRepository.delConfigByInstance(removeParam);
            }
        }
    }

    @Override
    public String logTag() {
        return "需求管理-照明配置迁移";
    }
}
