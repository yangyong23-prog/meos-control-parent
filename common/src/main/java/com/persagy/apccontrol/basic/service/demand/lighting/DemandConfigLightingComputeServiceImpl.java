package com.persagy.apccontrol.basic.service.demand.lighting;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.*;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandTreeQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingTreeObjectVO;
import com.persagy.apccontrol.basic.resource.intervention.InterventionConfigQueryResourceService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInheritModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingSettingObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingSettingScopeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.structure.intervention.strategy.impl.InterventionLightingConfigAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 照明配置计算
 *
 * @author yuecaipu
 * @version 1.0
 * @date 2024/3/27 14:37
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigLightingComputeServiceImpl implements DemandConfigLightingComputeService {
    
    private final DemandLightingQueryService demandLightingQueryService;
    private final DemandLightingStrategy demandLightingStrategy;
    private final InterventionConfigQueryResourceService interventionConfigQueryResourceService;
    private final InterventionLightingConfigAnalyzeStrategy interventionLightingConfigAnalyzeStrategy;
    
    @Override
    public Map<String, DemandLightingCircuitSettingModeEnum> mapCircuitIdAndSettingMode(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType) {
        // 计算结果
        Collection<DemandConfigLightingResultDTO> interventionResult = interventionResultCompute(projectId, runningMonitorSecondType, DateUtils.now());
        // 结果map映射
        return interventionResult.stream().collect(Collectors.toMap(DemandConfigLightingResultDTO::getCircuitId, o -> o.getConfigData().getCircuitSettingMode()));
    }
    
    @Override
    public Collection<DemandConfigLightingResultDTO> interventionResultCompute(String projectId, RunningMonitorSecondTypeEnum runningMonitorSecondType, LocalDateTime localDateTime) {
        DemandTemplateTypeEnum demandTemplateType = runningMonitorSecondType.toDemandTemplateTypeEnum();
        // =====1、获取当前时刻干预需求配置=====
        List<InterventionConfig> interventionConfigs = interventionConfigQueryResourceService.listActiveByTime(projectId, runningMonitorSecondType, localDateTime);
        if (CollUtil.isEmpty(interventionConfigs)) {
            return Collections.emptyList();
        }
        // 转换实例id 和 干预需求 map映射
        Map<String, InterventionConfig> instanceId2InterventionConfigMap = interventionConfigs.stream()
                                                                                              .collect(Collectors.toMap(InterventionConfig::getInstanceId, o -> o));
        // 转换实例id 和 配置 map映射
        Map<String, DemandConfigLightingDataDTO> instanceId2ConfigMap = interventionConfigs.stream()
                                                                                           .collect(Collectors.toMap(InterventionConfig::getInstanceId,
                                                                                                   o -> interventionLightingConfigAnalyzeStrategy.analyzeConfigObject(o.getConfigData())));
        // =====2、获取项目管控对象对应的项目-建筑-楼层-空间树=====
        DemandLightingTreeObjectVO treeObjectVO = treeControlledObject(projectId, demandTemplateType);
        
        // =====3、获取顶层视角下的所有回路=====
        Collection<DtpRwdCircuitDTO> dtpRwdCircuitList = listAllCircuits(projectId, treeObjectVO, demandTemplateType, instanceId2InterventionConfigMap);
        
        // =====4、回路配置结果构建=====
        Map<String, DemandConfigLightingResultDTO> circuitId2ResultMap = baseResultMapBuilding(projectId, null, null,
                dtpRwdCircuitList, instanceId2ConfigMap);
        configBuilding(projectId, demandTemplateType, circuitId2ResultMap, Collections.singletonList(treeObjectVO),
                instanceId2ConfigMap, false);
        
        return circuitId2ResultMap.values();
    }
    
    @NotNull
    private Collection<DtpRwdCircuitDTO> listCircuitsByInterventionConfig(String projectId, DemandTemplateTypeEnum templateType, InterventionConfig interventionConfig) {
        Collection<DtpRwdCircuitDTO> dtpRwdCircuitList;
        DemandConfigLightingDataDTO interventionConfigData = interventionLightingConfigAnalyzeStrategy
                .analyzeConfigObject(interventionConfig.getConfigData());
        // 全局配置为空，则查询回路
        if (CollUtil.isEmpty(interventionConfigData.getGlobalSettings())) {
            dtpRwdCircuitList = listCircuitsByInstance(
                    projectId,
                    templateType,
                    interventionConfig.getInstanceType(),
                    interventionConfig.getInstanceId());
        } else {
            // 全局配置不为空，直接组装回路集合
            dtpRwdCircuitList = new HashSet<>(8);
            for (DemandLightingFuncTypeSettingDTO globalSetting : interventionConfigData.getGlobalSettings()) {
                List<DemandLightingCircuitSettingDTO> circuitSettings = globalSetting.getCircuitSettings();
                DtpRwdCircuitFuncTypeEnum funcType = globalSetting.getFuncType();
                for (DemandLightingCircuitSettingDTO circuitSetting : circuitSettings) {
                    DtpRwdCircuitDTO dtpRwdCircuitDTO = new DtpRwdCircuitDTO();
                    dtpRwdCircuitDTO.setId(circuitSetting.getCircuitId());
                    dtpRwdCircuitDTO.setFuncType(funcType.getValue());
                    dtpRwdCircuitList.add(dtpRwdCircuitDTO);
                }
            }
        }
        return dtpRwdCircuitList;
    }
    
    private Set<DtpRwdCircuitDTO> listAllCircuits(String projectId,
                                                  DemandLightingTreeObjectVO treeObjectVO,
                                                  DemandTemplateTypeEnum templateType,
                                                  Map<String, InterventionConfig> instanceId2ConfigMap) {
        String instanceId = treeObjectVO.getId();
        Set<DtpRwdCircuitDTO> result = new HashSet<>(16);
        if (instanceId2ConfigMap.containsKey(instanceId)) {
            InterventionConfig interventionConfig = instanceId2ConfigMap.get(instanceId);
            Collection<DtpRwdCircuitDTO> dtpRwdCircuitList = listCircuitsByInterventionConfig(projectId, templateType, interventionConfig);
            result.addAll(dtpRwdCircuitList);
        } else if (CollUtil.isNotEmpty(treeObjectVO.getChildren())) {
            for (DemandLightingTreeObjectVO child : treeObjectVO.getChildren()) {
                Set<DtpRwdCircuitDTO> dtpRwdCircuitDTOS = listAllCircuits(projectId, child, templateType, instanceId2ConfigMap);
                result.addAll(dtpRwdCircuitDTOS);
            }
        }
        return result;
    }
    
    private List<DtpRwdCircuitDTO> listCircuitsByInstance(String projectId, DemandTemplateTypeEnum templateType, DemandInstanceTypeEnum instanceType, String instanceId) {
        
        
        return demandLightingStrategy.listCircuitsByInstance(projectId, templateType, instanceType, instanceId)
                                     .stream().filter(v -> StrUtil.isAllNotBlank(v.getFuncType(),
                        v.getServiceAreaType()))
                                     .collect(Collectors.toList());
    }
    
    
    /**
     * 构建基础结果回路和结果映射集
     * 反馈条件不需要继承在构建基础结果时候把反馈条件配置设置上
     * @param projectId  项目ID
     * @param templateId 模板ID
     * @param sceneId    场景ID
     * @param circuits   回路对象
     * @param instanceId2ConfigMap   实例配置 实例 ID -> 实例配置
     * @return 回路和结果映射
     * @author yuecaipu
     * @date 2024/3/28 15:33
     * @since 24Q1 T3S2-5.18.0
     */
    private Map<String, DemandConfigLightingResultDTO> baseResultMapBuilding(String projectId, Long templateId,
                                                                             Long sceneId,
                                                                             Collection<DtpRwdCircuitDTO> circuits
            , Map<String, DemandConfigLightingDataDTO> instanceId2ConfigMap) {
        Map<String, DemandConfigLightingResultDTO> map = new HashMap<>(16);
        for (DtpRwdCircuitDTO circuit : circuits) {
            DemandConfigLightingResultDTO demandConfigLightingResult = new DemandConfigLightingResultDTO();
            map.put(circuit.getId(), demandConfigLightingResult);
            demandConfigLightingResult.setProjectId(projectId);
            demandConfigLightingResult.setCircuitId(circuit.getId());
            // 默认配置来源项目
            demandConfigLightingResult.setConfigSourceType(DemandInheritModeEnum.GLOBAL);
            demandConfigLightingResult.setConfigSourceObjId(projectId);
            demandConfigLightingResult.setRelDemandTemplateId(templateId);
            demandConfigLightingResult.setRelDemandLightingSceneId(sceneId);
            // 默认回路未配置
            demandConfigLightingResult.setConfigData(DemandConfigLightingResultDataDTO.newInstance());
        }
        return map;
    }
    
    
    private void configBuilding(String projectId,
                                DemandTemplateTypeEnum demandTemplateType,
                                Map<String, DemandConfigLightingResultDTO> resultMap,
                                List<DemandLightingTreeObjectVO> treeObjectList,
                                Map<String, DemandConfigLightingDataDTO> instanceId2ConfigMap,
                                boolean flag) {
        
        if (CollUtil.isEmpty(treeObjectList)) {
            return;
        }
        for (DemandLightingTreeObjectVO treeObjectVO : treeObjectList) {
            DemandConfigLightingDataDTO sceneObjectSetting = getBaseSceneObjectSettings(projectId, demandTemplateType, treeObjectVO, instanceId2ConfigMap, flag);
            if (null != sceneObjectSetting) {
                //-----全局配置设定-----
                configBuilding(resultMap, treeObjectVO, sceneObjectSetting.getGlobalSettings(),
                        DemandLightingSettingScopeTypeEnum.GLOBAL);
                
                //-----空间配置设定 空间配置覆盖全局配置-----
                if (CollUtil.isNotEmpty(sceneObjectSetting.getSpaceSettings())) {
                    for (DemandLightingSpaceFuncTypeSettingDTO setting : sceneObjectSetting.getSpaceSettings()) {
                        configBuilding(resultMap, treeObjectVO, setting.getFuncTypeSettings(),
                                DemandLightingSettingScopeTypeEnum.SPACE);
                    }
                }
            }
            
            //-----下级设置 下级配置覆盖上级-----
            configBuilding(projectId, demandTemplateType, resultMap, treeObjectVO.getChildren(), instanceId2ConfigMap
                    , flag);
        }
    }
    
    
    /**
     * 设置回路配置状态
     * 回路单独配置优先级大于全量配置
     * a. 都是到回路的配置：有一个配置为开则开
     * b. 都是全量的配置：有一个空间的配置为开则开
     *
     * @param map              回路ID和配置结果映射
     * @param treeObjectVO     项目建筑楼层空间树节点
     * @param funcTypeSettings 配置
     * @param scopeType        配置范围类型 全局配置、空间配置
     * @author yuecaipu
     * @date 2024/3/29 11:46
     * @since 24Q1 T3S2-5.18.0
     */
    private void configBuilding(Map<String, DemandConfigLightingResultDTO> map,
                                DemandLightingTreeObjectVO treeObjectVO,
                                List<DemandLightingFuncTypeSettingDTO> funcTypeSettings,
                                DemandLightingSettingScopeTypeEnum scopeType) {
        if (CollUtil.isEmpty(funcTypeSettings)) {
            return;
        }
        // 功能类型
        for (DemandLightingFuncTypeSettingDTO funcTypeSetting : funcTypeSettings) {
            if (CollUtil.isEmpty(funcTypeSetting.getCircuitSettings())) {
                continue;
            }
            // 回路
            for (DemandLightingCircuitSettingDTO circuitSetting : funcTypeSetting.getCircuitSettings()) {
                DemandConfigLightingResultDTO lightingResult = map.get(circuitSetting.getCircuitId());
                if (null == lightingResult) {
                    continue;
                }
                //-----设置配置状态和配置来源-----
                
                // 当前配置对应的回路配置状态
                DemandLightingCircuitSettingModeEnum circuitSettingMode = null == circuitSetting.getSettingMode()
                        ? DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED : circuitSetting.getSettingMode();
                
                
                // 照明配置设置类型 : 功能类型、回路
                DemandLightingSettingObjectTypeEnum settingType;
                if (DemandLightingTypeSettingModeEnum.PARTIAL_OPEN.equals(funcTypeSetting.getSettingMode())) {
                    settingType = DemandLightingSettingObjectTypeEnum.CIRCUIT;
                } else {
                    settingType = DemandLightingSettingObjectTypeEnum.FUNC_TYPE;
                }
                // 配置等级一样，比较优先级
                // 都是到回路的配置：有一个配置为开则开
                // 都是全量的配置：有一个空间的配置为开则开
                if (scopeType.levelCompare(lightingResult.getScopeType()) < 0) {
                    // 空间配置覆盖全局配置
                    continue;
                } else if (scopeType.levelCompare(lightingResult.getScopeType()) == 0) {
                    if (settingType.levelCompare(lightingResult.getSettingObject()) == 0) {
                        if (DemandLightingCircuitSettingModeEnum.OPEN.equals(lightingResult.getConfigData().getCircuitSettingMode())
                                || DemandLightingCircuitSettingModeEnum.OPEN.equals(circuitSettingMode)) {
                            circuitSettingMode = DemandLightingCircuitSettingModeEnum.OPEN;
                        }
                    } else if (DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED.equals(circuitSettingMode)) {
                        // 有一个没配置，则使用另一个
                        circuitSettingMode = lightingResult.getConfigData().getCircuitSettingMode();
                    }
                }
                lightingResult.setConfigSourceObjId(treeObjectVO.getId());
                lightingResult.setSettingObject(settingType);
                lightingResult.setConfigData(lightingResult.getConfigData().setCircuitSettingMode(circuitSettingMode));
                lightingResult.setScopeType(scopeType);
            }
        }
        
    }
    
    
    
    private DemandConfigLightingDataDTO getBaseSceneObjectSettings(String projectId,
                                                                   DemandTemplateTypeEnum demandTemplateType,
                                                                   DemandLightingTreeObjectVO treeObject,
                                                                   Map<String, DemandConfigLightingDataDTO> instanceId2ConfigMap,
                                                                   boolean flag) {
        DemandConfigLightingDataDTO configData = instanceId2ConfigMap.get(treeObject.getId());
        // 根据最新的物理世界交付数据 补全回路
        if (null == configData) {
            return null;
        }
        if (flag || CollUtil.isEmpty(configData.getGlobalSettings())) {
            List<DtpRwdCircuitDTO> dtpRwdCircuitList = demandLightingStrategy.listCircuitsByInstance(projectId, demandTemplateType, treeObject.getInstanceType(), treeObject.getId())
                                                                             .stream().filter(node -> StrUtil.isAllNotBlank(node.getServiceAreaType(), node.getFuncType())).collect(Collectors.toList());
            
            if (CollUtil.isEmpty(dtpRwdCircuitList)) {
                // 已无回路 清空配置
                configData.setGlobalSettings(new ArrayList<>());
                configData.setSpaceSettings(new ArrayList<>());
                return configData;
            }
            
            // 全局配置
            if (CollUtil.isNotEmpty(configData.getGlobalSettings()) ||
                    (treeObject.getIsCfg() != null && treeObject.getIsCfg())) {
                configData.setGlobalSettings(merge(dtpRwdCircuitList, configData.getGlobalSettings()));
            }
            
            if (CollUtil.isNotEmpty(configData.getSpaceSettings())) {
                List<String> rwdCircuitIds = dtpRwdCircuitList.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toList());
                // 回路和空间类型对应关系
                Map<String, List<String>> circuitId2spaceFuncTypeMap = demandLightingStrategy
                        .mapCircuitIdAndSpaceFuncTypes(projectId, demandTemplateType, rwdCircuitIds);
                
                for (int i = 0; i < configData.getSpaceSettings().size(); i++) {
                    // 过滤出空间类型对应的回路
                    DemandLightingSpaceFuncTypeSettingDTO funcTypeSettingDTO = configData.getSpaceSettings().get(i);
                    List<String> spaceFuncTypes = funcTypeSettingDTO.getSpaceFuncTypes().stream().map(v -> v.getKey().toString()).collect(Collectors.toList());
                    List<DtpRwdCircuitDTO> collect = dtpRwdCircuitList.stream()
                                                                      .filter(node -> {
                                                                          List<String> list =
                                                                                  circuitId2spaceFuncTypeMap.get(node.getId());
                                                                          return CollUtil.isNotEmpty(CollUtil.intersection(list, spaceFuncTypes));
                                                                      })
                                                                      .collect(Collectors.toList());
                    
                    funcTypeSettingDTO.setFuncTypeSettings(merge(collect, funcTypeSettingDTO.getFuncTypeSettings()));
                }
            }
        }
        return configData;
    }
    
    /**
     * 根据最新物理世界交付合并回路配置信息
     *
     * @param dtpRwdCircuitList 物理世界回路对象列表
     * @param funcTypeSettings  回路配置
     * @author yuecaipu
     * @date 2024/4/2 16:30
     * @since 24Q1 T3S2-5.18.0
     */
    private List<DemandLightingFuncTypeSettingDTO> merge(List<DtpRwdCircuitDTO> dtpRwdCircuitList,
                                                         List<DemandLightingFuncTypeSettingDTO> funcTypeSettings) {
        ArrayList<DemandLightingFuncTypeSettingDTO> list = new ArrayList<>();
        if (CollUtil.isEmpty(dtpRwdCircuitList)) {
            return new ArrayList<>();
        }
        
        Map<String, List<DtpRwdCircuitDTO>> funcType2circuits = dtpRwdCircuitList.stream()
                                                                                 .collect(Collectors.groupingBy(DtpRwdCircuitDTO::getFuncType));
        
        Map<DtpRwdCircuitFuncTypeEnum, DemandLightingFuncTypeSettingDTO> funcType2setting = null == funcTypeSettings ? new HashMap<>() : funcTypeSettings.stream()
                                                                                                                                                         .collect(Collectors.toMap(DemandLightingFuncTypeSettingDTO::getFuncType, o -> o));
        
        // =====功能类型配置=====
        for (String funcType : funcType2circuits.keySet()) {
            // 原始功能类型配置
            DemandLightingFuncTypeSettingDTO originalFuncTypeSetting = funcType2setting.get(DtpRwdCircuitFuncTypeEnum.analyze(funcType));
            // 根据物理世界交付最新配置
            DemandLightingFuncTypeSettingDTO funcTypeSetting = new DemandLightingFuncTypeSettingDTO();
            list.add(funcTypeSetting);
            funcTypeSetting.setFuncType(DtpRwdCircuitFuncTypeEnum.analyze(funcType));
            funcTypeSetting.setSettingMode(null == originalFuncTypeSetting ? DemandLightingTypeSettingModeEnum.NOT_CONFIGURED : originalFuncTypeSetting.getSettingMode());
            funcTypeSetting.setCircuitSettings(new ArrayList<>());
            
            Map<String, DemandLightingCircuitSettingModeEnum> circuit2setting = null;
            if (null != originalFuncTypeSetting && CollUtil.isNotEmpty(originalFuncTypeSetting.getCircuitSettings())) {
                List<DemandLightingCircuitSettingDTO> circuitSettings = originalFuncTypeSetting.getCircuitSettings();
                circuit2setting = circuitSettings.stream().collect(Collectors.toMap(DemandLightingCircuitSettingDTO::getCircuitId, DemandLightingCircuitSettingDTO::getSettingMode));
            }
            List<DtpRwdCircuitDTO> rwdCircuits = funcType2circuits.get(funcType);
            // =====回路配置=====
            for (DtpRwdCircuitDTO rwdCircuit : rwdCircuits) {
                DemandLightingCircuitSettingDTO circuitSetting = new DemandLightingCircuitSettingDTO();
                circuitSetting.setCircuitId(rwdCircuit.getId());
                circuitSetting.setSettingMode(DemandLightingCircuitSettingModeEnum
                        .getSettingMode(funcTypeSetting.getSettingMode(), null == circuit2setting ? null : circuit2setting.get(rwdCircuit.getId())));
                funcTypeSetting.getCircuitSettings().add(circuitSetting);
            }
        }
        return list;
    }
    
    /**
     * 获取项目管控对象对应的项目-建筑-楼层-空间树
     *
     * @param projectId        项目ID
     * @param templateTypeEnum 模板类型
     * @return {@link DemandLightingTreeObjectVO}
     * @author yuecaipu
     * @date 2024/3/27 15:02
     * @since 24Q1 T3S2-5.18.0
     */
    private DemandLightingTreeObjectVO treeControlledObject(String projectId, DemandTemplateTypeEnum templateTypeEnum) {
        
        DemandTreeQueryParam demandTreeQueryParam = new DemandTreeQueryParam();
        demandTreeQueryParam.setProjectId(projectId);
        demandTreeQueryParam.setTemplateType(templateTypeEnum);
        return demandLightingQueryService.treeObject(demandTreeQueryParam);

/*
        String key = "treeControlledObject:" + projectId + ":" + templateTypeEnum.getValue();
        try {
            return (DemandLightingTreeObjectVO) LOCAL_CACHE.get(key, () -> {
                DemandTreeQueryParam demandTreeQueryParam = new DemandTreeQueryParam();
                demandTreeQueryParam.setProjectId(projectId);
                demandTreeQueryParam.setTemplateType(templateTypeEnum);
                DemandLightingTreeObjectVO demandLightingTreeObjectVO = demandLightingQueryService.treeObject
                (demandTreeQueryParam);

                LOCAL_CACHE.put(key, demandLightingTreeObjectVO);
                return demandLightingTreeObjectVO;
            });
        } catch (Exception e) {
            log.error("treeControlledObject 执行异常", e);
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), "treeControlledObject 执行异常");
        }*/
    }
    
}
