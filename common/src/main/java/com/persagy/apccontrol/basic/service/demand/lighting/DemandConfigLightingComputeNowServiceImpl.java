package com.persagy.apccontrol.basic.service.demand.lighting;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingResultDataDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitFeedbackAdjustSettingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.model.param.common.EquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandConfigLightingComputeParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingResultService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInheritModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.DemandConfigLightingValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 照明回路需求计算
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandConfigLightingComputeNowServiceImpl implements DemandConfigLightingComputeNowService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemandConfigLightingComputeNowServiceImpl.class);
    private static final String LOG_TAG = "照明配置计算";
    
    private final DemandConfigGateway<DemandConfigLightingValueObject> demandConfigGateway;
    private final DemandConfigLightingResultService demandConfigLightingResultService;
    private final DemandLightingStrategy demandLightingStrategy;
    private final DtpRwdClientService dtpRwdClientService;
    private final DemandSceneGateway demandSceneGateway;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resultCompute(@Valid DemandConfigLightingComputeParam param) {
        StopWatch stopWatch = new StopWatch("照明配置结果计算 耗时统计" + ":" + param.getTemplateId() + ":" + param.getSceneId());
        Long templateId = param.getTemplateId();
        Long sceneId = param.getSceneId();
        
        ParameterCheck.isNull(templateId, LOG_TAG + "模版 ID 不能为空");
        ParameterCheck.isNull(sceneId, LOG_TAG + "场景 ID 不能为空");
        
        //查询模版 判断模版类型
        Optional<DemandSceneEntity> scene = demandSceneGateway.getScene(param.getSceneId());
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateId(param.getTemplateId());
        DemandTemplate template = templateParam.queryAndGetTemplate();
        
        if (!scene.isPresent() || template == null || template.getDemandTemplateType() == null) {
            //执行数据删除逻辑 如果模版或者场景不存在
            if (!scene.isPresent()) {
                sceneId = null;
            }
            demandConfigLightingResultService.deleteByTemplateAndScene(templateId, sceneId);
            return;
        }
        
        //只有照明类型才计算
        if (!DemandTypeEnum.LIGHTING.equals(template.getDemandType())) {
            return;
        }
        
        //回路配置结果
        Map<String, DemandConfigLightingResult> circuitIdResults = new HashMap<>();
        
        //查询回路 当前计算的全量回路
        List<DtpRwdCircuitDTO> circuitsBySpace = getDtpRwdCircuitDTOS(param, template, param.getSpaceIds());
        if (CollectionUtil.isEmpty(circuitsBySpace)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未查询到回路-templateId:" + template.getId());
            return;
        }
        stopWatch.start("照明-全量回路查询");
        //当前计算的全量回路
        List<DtpRwdCircuitDTO> circuits =
                circuitsBySpace.stream().filter(v -> StrUtil.isAllNotBlank(v.getFuncType(), v.getServiceAreaType())).collect(Collectors.toList());
        //状态初始化
        initSettingMode(param, circuitIdResults, circuits);
        stopWatch.stop();
        
        //1.室外照明直接去项目下的回路 保存结果
        if (template.getDemandTemplateType().isNightLighting()) {
            nightLighting(templateId, sceneId, circuitIdResults, stopWatch);
        } else {
            //2.室内照明通过空间获取配置计算结果
            indoorLighting(param, template, circuitIdResults, stopWatch, circuits);
        }
        
        if (MapUtil.isEmpty(circuitIdResults)) {
            LogExecutor.project().info(LOGGER, LOG_TAG,
                    "未取到回路,不更新回路-templateId:" + templateId + "-sceneId:" + sceneId);
            return;
        }
        
        //保存最终结果
        stopWatch.start("保存最终结果");
        List<DemandConfigLightingResult> newResults = new ArrayList<>(circuitIdResults.values());
        dataPersistence(param, newResults);
        stopWatch.stop();
        log.info(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    }
    
    @Override
    public List<DemandConfigAggregate<DemandConfigLightingValueObject>> queryCfg(DemandConfigLightingComputeParam param) {
        Map<String, List<DemandConfigAggregate<DemandConfigLightingValueObject>>> map = new HashMap<>();
        
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateId(param.getTemplateId());
        DemandTemplate template = templateParam.queryAndGetTemplate();
        //查询回路 当前计算的全量回路
        List<DtpRwdCircuitDTO> circuitsBySpace = getDtpRwdCircuitDTOS(param, template, param.getSpaceIds());
        //当前计算的全量回路
        List<DtpRwdCircuitDTO> circuits =
                circuitsBySpace.stream().filter(v -> StrUtil.isAllNotBlank(v.getFuncType(), v.getServiceAreaType())).collect(Collectors.toList());
        
        if (template.getDemandTemplateType().isNightLighting()) {
            return getDemandConfigAggregates(param.getTemplateId(),
                    param.getSceneId());
        }
        return getDemandConfigAggregates(param, template, circuits);
    }
    
    
    /**
     * 室内照明计算
     * 1.获取项目下的空间
     * 2.获取空间的照明需求配置
     * 3.计算空间下回路状态
     *
     * @param param
     * @param circuitIdResults
     */
    private void indoorLighting(DemandConfigLightingComputeParam param, DemandTemplate template,
                                Map<String, DemandConfigLightingResult> circuitIdResults,
                                StopWatch stopWatch, List<DtpRwdCircuitDTO> circuits) {
        //查询空间的需求配置
        stopWatch.start("室内照明-查询空间的需求配置");
        
        List<DemandConfigAggregate<DemandConfigLightingValueObject>> demandConfigAggregates = getDemandConfigAggregates(param, template,
                circuits);
        stopWatch.stop();
        if (CollectionUtil.isEmpty(demandConfigAggregates)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未查询到回路配置结果-templateId:" + template.getId());
            return;
        }
        stopWatch.start("室内照明-回路结果计算");

        demandConfigAggregates.forEach(finalConfig -> {
            // 默认来源为空间对象
            String sourceObjId = finalConfig.getIdentifier().getInstance().getInstanceId();
            DemandInheritModeEnum inheritMode = null;
            // 获取第一个元素
            for (Map.Entry<DemandInstanceValueObject, Set<DemandInheritModeEnum>> entry :
                    finalConfig.getInheritInstanceAndModes().entrySet()) {
                DemandInstanceValueObject k = entry.getKey();
                Set<DemandInheritModeEnum> v = entry.getValue();
                sourceObjId = k.getInstanceId();
                if (v == null || v.isEmpty()) {
                    continue;
                }
                inheritMode = v.iterator().next();
            }
            
            if (finalConfig.getFinalConfig() == null || finalConfig.getFinalConfig().getGlobalConfig() == null ||
                    CollectionUtil.isEmpty(finalConfig.getFinalConfig().getGlobalConfig().getGlobalSettings())) {
                LogExecutor.project().info(LOGGER, LOG_TAG,
                        "未查询到回路配置结果-templateId:" + finalConfig.getIdentifier().getTemplateId() + "-spaceId:" + finalConfig.getIdentifier().getInstance());
                return;
            }
            String finalSourceObjId = sourceObjId;
            DemandInheritModeEnum finalInheritMode = inheritMode;
            finalConfig.getFinalConfig().getGlobalConfig().getGlobalSettings().forEach(cfg -> {
                if (cfg.getCircuitSettings() == null) {
                    LogExecutor.project().info(LOGGER, LOG_TAG,
                            "未查询到回路配置结果-templateId:" + finalConfig.getIdentifier().getTemplateId() + "-spaceId:" + finalConfig.getIdentifier().getInstance());
                    return;
                }
                cfg.getCircuitSettings().forEach(circuitSettings -> putCircuitIdResults(circuitSettings.getCircuitId(), circuitSettings.getSettingMode(),
                        circuitSettings.getFeedbackAdjustSetting(), circuitIdResults, template.getId(), param.getSceneId(), finalInheritMode, finalSourceObjId));
            });
        });
        stopWatch.stop();
    }
    
    /**
     * 查询室内照明配置
     *
     * @param param
     * @param template
     * @param circuits
     * @return
     */
    private List<DemandConfigAggregate<DemandConfigLightingValueObject>> getDemandConfigAggregates(DemandConfigLightingComputeParam param
            , DemandTemplate template, List<DtpRwdCircuitDTO> circuits) {
        //空间 ID 集合
        Set<String> spaceId = getSpaceId(circuits);
        LogExecutor.project().info(LOGGER, LOG_TAG, "配置查询-查询的空间配置集合:" + spaceId);
        DemandConfigByInstanceBatchQueryParam demandConfigByInstanceBatchQueryParam =
                new DemandConfigByInstanceBatchQueryParam();
        demandConfigByInstanceBatchQueryParam.setDate(DateUtils.now().toLocalDate());
        demandConfigByInstanceBatchQueryParam.setTemplateId(template.getId());
        demandConfigByInstanceBatchQueryParam.setSceneId(param.getSceneId());
        demandConfigByInstanceBatchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE,
                spaceId));
        demandConfigByInstanceBatchQueryParam.setAutoFillEmptyInstanceIds(true);
        List<DemandConfigAggregate<DemandConfigLightingValueObject>> cfg =
                demandConfigGateway.listConfigsByInstances(demandConfigByInstanceBatchQueryParam);
        LogExecutor.project().info(LOGGER, LOG_TAG, "配置查询-查询的空间配置结果:" + cfg);
        return cfg;
    }
    
    /**
     * 获取空间 ID
     *
     * @param circuits 回路列表
     * @return 空间ID集合
     */
    private Set<String> getSpaceId(List<DtpRwdCircuitDTO> circuits) {
        if (CollectionUtil.isEmpty(circuits)) {
            return new HashSet<>();
        }
        return circuits.stream()
                       .filter(Objects::nonNull)
                       .map(DtpRwdCircuitDTO::getServiceSpaces)
                       .filter(CollectionUtil::isNotEmpty)
                       .flatMap(List::stream)
                       .filter(Objects::nonNull)
                       .map(DtpRwdSpaceDTO::getId)
                       .filter(StrUtil::isNotBlank)
                       .collect(Collectors.toSet());
    }
    
    /**
     * 初始化状态 为未配置
     *
     * @param param
     * @param circuitIdResults
     * @param circuits
     */
    private void initSettingMode(DemandConfigLightingComputeParam param,
                                 Map<String, DemandConfigLightingResult> circuitIdResults,
                                 List<DtpRwdCircuitDTO> circuits) {
        //初始化默认配置 都是未配置 再匹配 配置结果
        circuits.forEach(v -> putCircuitIdResults(v.getId(), DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED, null, circuitIdResults,
                param.getTemplateId(), param.getSceneId(), null, null));
    }
    
    /**
     * 查询回路 和关联的空间
     *
     * @param param
     * @param template
     * @param spaceIds
     * @return
     */
    private List<DtpRwdCircuitDTO> getDtpRwdCircuitDTOS(DemandConfigLightingComputeParam param, DemandTemplate template,
                                                        Set<String> spaceIds) {
        SpaceQueryParam spaceQueryParam;
        EquipmentQueryParam circuitQueryParam = new EquipmentQueryParam();
        circuitQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdCircuitDTO.Fields.funcType,
                DtpRwdCircuitDTO.Fields.serviceAreaType);
        //回路
        List<DtpRwdCircuitDTO> circuitsBySpace = null;
        //如果指定了空间就计算指定空间数据
        if (CollectionUtil.isNotEmpty(spaceIds)) {
            //指定了空间 查询空间下的回路 通过回路反向查询一次空间 处理回路服务多空间情况
            spaceQueryParam = new SpaceQueryParam();
            spaceQueryParam.setFilterObjectIds(param.getSpaceIds());
            EquipmentQueryParam cq = new EquipmentQueryParam();
            cq.addWithColumn(DtpRwdObjectDTO.Fields.id);
            circuitsBySpace =
                    demandLightingStrategy.listCircuits(Collections.singleton(template.getDemandTemplateType()), cq,
                            spaceQueryParam);
            if (CollectionUtil.isEmpty(circuitsBySpace)) {
                LogExecutor.project().info(LOGGER, LOG_TAG,
                        "未查询到回路-templateId:" + template.getId() + "-spaceIds:" + spaceIds);
                return null;
            }
            Set<String> ids = circuitsBySpace.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());
            List<DtpRwdSpaceDTO> spIds = dtpRwdClientService.listSpaceServiceByEquipmentId(ids,
                    new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id));
            if (CollectionUtil.isEmpty(spIds)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "未查询到回路服务空间-templateId:" + template.getId() + "-circuitIds:" + ids);
                return circuitsBySpace;
            }
            // 重新查询回路
            Set<String> serviceSpaceIds =
                    spIds.stream().map(DtpRwdSpaceDTO::getId).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
            
            if (CollUtil.isEmpty(serviceSpaceIds)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "服务空间ID集合为空-templateId:" + template.getId());
                return circuitsBySpace;
            }
            //重新设置空间 ID
            param.setSpaceIds(serviceSpaceIds);
            
            spaceQueryParam.setFilterObjectIds(serviceSpaceIds);
            circuitsBySpace = demandLightingStrategy.listCircuits(Collections.singleton(template.getDemandTemplateType()),
                    circuitQueryParam, spaceQueryParam);
            
        } else {
            circuitsBySpace = demandLightingStrategy.listCircuits(Collections.singleton(template.getDemandTemplateType()),
                    circuitQueryParam, null);
            
        }
        return circuitsBySpace;
    }
    
    
    /**
     * 夜景照明计算
     * 直接通过项目下的回路结果生成结果记录
     *
     * @param templateId
     * @param sceneId
     * @param circuitIdResults
     */
    private void nightLighting(Long templateId, Long sceneId,
                               Map<String, DemandConfigLightingResult> circuitIdResults,
                               StopWatch stopWatch) {
        stopWatch.start("夜景照明-配置查询");
        List<DemandConfigAggregate<DemandConfigLightingValueObject>> demandConfigAggregates = getDemandConfigAggregates(templateId,
                sceneId);
        stopWatch.stop();
        if (demandConfigAggregates == null) {
            return;
        }
        stopWatch.start("夜景照明-回路计算");
        demandConfigAggregates.forEach(demandConfigAggregate -> {
            DemandConfigValueObject<DemandConfigLightingValueObject> finalConfig =
                    demandConfigAggregate.getFinalConfig();
            if (finalConfig != null && finalConfig.getGlobalConfig() != null && finalConfig.getGlobalConfig().getGlobalSettings() != null) {
                finalConfig.getGlobalConfig().getGlobalSettings().forEach(demandConfigLightingFuncTypeValueObject -> {
                    if (CollectionUtil.isEmpty(demandConfigLightingFuncTypeValueObject.getCircuitSettings())) {
                        LogExecutor.project().info(LOGGER, LOG_TAG,
                                "回路配置结果为空-templateId:" + templateId + "-sceneId:" + sceneId);
                        return;
                    }
                    demandConfigLightingFuncTypeValueObject.getCircuitSettings().forEach(cfg -> {
                        putCircuitIdResults(cfg.getCircuitId(), cfg.getSettingMode(), cfg.getFeedbackAdjustSetting(), circuitIdResults, templateId,
                                sceneId, DemandInheritModeEnum.GLOBAL, ContextUtil.getProjectId());
                    });
                });
            } else {
                LogExecutor.project().info(LOGGER, LOG_TAG,
                        "回路配置结果为空-templateId:" + templateId + "-sceneId:" + sceneId);
            }
        });
        stopWatch.stop();
    }
    
    /**
     * 查询室外照明配置
     *
     * @param templateId
     * @param sceneId
     * @return
     */
    private List<DemandConfigAggregate<DemandConfigLightingValueObject>> getDemandConfigAggregates(Long templateId, Long sceneId) {
        DemandConfigByInstanceBatchQueryParam demandConfigByInstanceBatchQueryParam =
                new DemandConfigByInstanceBatchQueryParam();
        demandConfigByInstanceBatchQueryParam.setTemplateId(templateId);
        demandConfigByInstanceBatchQueryParam.setSceneId(sceneId);
        demandConfigByInstanceBatchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.PROJECT, null));
        
        return demandConfigGateway.listConfigsByInstances(demandConfigByInstanceBatchQueryParam);
    }
    
    /**
     * 数据持久化
     *
     * @param param
     * @param newResults
     */
    private void dataPersistence(DemandConfigLightingComputeParam param,
                                 List<DemandConfigLightingResult> newResults) {
        if (CollUtil.isEmpty(newResults)) {
            return;
        }
        //如果指定了空间 就只更新固定回路 注: 如果交付变更需要按照场景级别全量更新
        if (CollUtil.isNotEmpty(param.getSpaceIds())) {
            List<String> ids = newResults.stream().map(DemandConfigLightingResult::getCircuitId).collect(Collectors.toList());
            //-----直接覆盖配置-----
            demandConfigLightingResultService.deleteByCircuitId(param.getTemplateId(), param.getSceneId(), ids);
        } else {
            //-----按照场景全量覆盖-----
            demandConfigLightingResultService.deleteByTemplateAndScene(param.getTemplateId(), param.getSceneId());
        }
        //批量保存
        demandConfigLightingResultService.batchSave(newResults);
    }
    
    
    /**
     * 添加回路结果
     *
     * @param circuitId
     * @param settingMode
     * @param circuitIdResults
     * @param templateId
     * @param sceneId
     */
    private void putCircuitIdResults(String circuitId, DemandLightingCircuitSettingModeEnum settingMode,
                                     DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting, Map<String,
            DemandConfigLightingResult> circuitIdResults, Long templateId, Long sceneId,
                                     DemandInheritModeEnum demandInheritMode, String sourceObjId) {
        DemandConfigLightingResult circuitCfgResult = circuitIdResults.get(circuitId);
        
        // 检查是否应该更新配置（基于优先级）
        if (circuitCfgResult != null && circuitCfgResult.getConfigData() != null) {
            DemandLightingCircuitSettingModeEnum currentMode = circuitCfgResult.getConfigData().getCircuitSettingMode();
            // 如果新配置的优先级不高于当前配置，则不更新
            if (!DemandLightingCircuitSettingModeEnum.shouldUpdate(currentMode, settingMode)) {
                return;
            }
        }
        
        //如果是未配置 取消配置来源
        if (DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED.equals(settingMode)) {
            sourceObjId = null;
        }
        
        DemandConfigLightingResult demandConfigLightingResult = new DemandConfigLightingResult();
        demandConfigLightingResult.setProjectId(ContextUtil.getProjectId());
        demandConfigLightingResult.setCircuitId(circuitId);
        demandConfigLightingResult.setConfigSourceObjId(sourceObjId);
        demandConfigLightingResult.setConfigSourceType(demandInheritMode);
        demandConfigLightingResult.setRelDemandTemplateId(templateId);
        demandConfigLightingResult.setRelDemandLightingSceneId(sceneId);
        demandConfigLightingResult.setConfigData(new DemandConfigLightingResultDataDTO(settingMode, feedbackAdjustSetting));
        circuitIdResults.put(circuitId, demandConfigLightingResult);
    }
    
    
}
