package com.persagy.apccontrol.basic.service.demand.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.*;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroupDetail;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingScene;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.lighting.*;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.*;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSpaceFeedbackAdjustSettingVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.resource.demand.*;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.instance.group.DemandInstanceGroupQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.DemandConfigLightingQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingResultQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingSceneQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.resource.intervention.InterventionConfigQueryResourceService;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingHandler;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingQueryService;
import com.persagy.apccontrol.basic.structure.demand.converter.DemandCalendarConverter;
import com.persagy.apccontrol.basic.structure.demand.converter.lighting.DemandLightingConverter;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.framework.common.util.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 照明需求查询服务
 *
 * @author yuyao
 * @since 24Q1 T3S1-5.17.0
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandLightingQueryServiceImpl implements DemandLightingQueryService, BizComponent<DemandLightingQueryService> {

    private static final String LIGHTING_SCENE_NOT_FOUND_MESSAGE = LocaleMessageUtil.getMessage("key_7c8d283d60d542c2b24050c33d0af406");

    private final DemandLightingStrategy demandLightingStrategy;
    private final DemandCalendarConverter demandCalendarConverter;
    private final DemandLightingConverter demandLightingConverter;
    private final MidPermissionClientService midPermissionClientService;
    private final DemandCalendarQueryService demandCalendarQueryService;
    private final DemandTemplateService demandTemplateService;
    private final DemandConfigLightingService demandConfigLightingService;
    private final DemandConfigLightingSceneService demandConfigLightingSceneService;
    private final DemandConfigLightingResultService demandConfigLightingResultService;
    private final DemandConfigLightingHandler demandConfigLightingHandler;
    private final InterventionConfigQueryResourceService interventionConfigQueryResourceService;
    private final ObjectMapper objectMapper;
    private final DtpRwdClientService dtpRwdClientService;
    private final DemandInstanceGroupResource demandInstanceGroupResource;

    @Override
    public CircuitObjCfgStatisticsVO countCircuitObjCfgStatistics(CircuitObjCfgStatisticsQueryParam param) {
        CircuitObjCfgStatisticsVO vo = new CircuitObjCfgStatisticsVO();

        List<DtpRwdSpaceDTO> spaces = null;
        if (!isProjectCfg(param.getTemplateType())) {
            // 通过空间查询 空间下的负载id
            spaces = demandLightingStrategy.listSpacesByCircuit(param.getProjectId(), param.getTemplateType(), null, param.getSpaceIds());
            if (CollectionUtils.isEmpty(spaces)) {
                return vo;
            }
        }
        // 获取回路的配置状态
        DemandConfigLightingResultQueryDaoParam qp = getDemandConfigLightingResultQueryDaoParam(param, spaces);

        // 开关计数器
        int totality = demandConfigLightingResultService.cfgCount(param.getProjectId(), qp);
        qp.setCircuitSettingMode(DemandLightingCircuitSettingModeEnum.OPEN);
        int open = demandConfigLightingResultService.cfgCount(param.getProjectId(), qp);
        qp.setCircuitSettingMode(DemandLightingCircuitSettingModeEnum.CLOSE);
        int close = demandConfigLightingResultService.cfgCount(param.getProjectId(), qp);
        qp.setCircuitSettingMode(DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT);
        int feedbackAdjustment = demandConfigLightingResultService.cfgCount(param.getProjectId(), qp);

        vo.setTotality(totality);
        vo.setOpenTotal(open);
        vo.setCloseTotal(close);
        vo.setFeedbackAdjustmentTotal(feedbackAdjustment);
        vo.setNotCfgTotal(totality - open - close - feedbackAdjustment);
        return vo;
    }

    @Override
    public DemandLightingTreeObjectVO treeObject(DemandTreeQueryParam param) {
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum lightingType = param.getTemplateType();

        ProjectDTO project = midPermissionClientService.getProject(false, true, projectId);
        // 添加顶级项目信息节点
        DemandLightingTreeObjectVO vo = new DemandLightingTreeObjectVO();
        vo.setId(project.getProjectId());
        vo.setName(project.getProjectName());
        vo.setLocalName(project.getProjectLocalName());
        vo.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        if (lightingType.isNightLighting()) {
            // 夜景照明只有项目层级
            return vo;
        }

        // 获取主子结构关系对象数据
        List<DtpRwdTreeLightingObjectDTO> rwdObjLst = demandLightingStrategy.queryBuildingFloorSpaceObject(projectId, lightingType);
        // 数据转换成vo
        List<DemandLightingTreeObjectVO> result = getDemandLightingTreeObjectVOList(rwdObjLst);
        vo.setChildren(result);

        // 取出所有id 去数据库检索是否 进行了配置
        List<String> ids = new ArrayList<>();
        ids.add(project.getProjectId());
        rwdObjLst.forEach(i -> ids.addAll(i.getAllIds()));

        // 配置数据打标
        cfgMarking(vo, ids, param);
        return vo;
    }

    @Override
    public DemandConfigLightingScene getSceneAndCheckThrow(
            @NonNull String projectId, @NonNull Long templateId, @NonNull Long sceneId) {
        DemandConfigLightingScene dbLightingScene = demandConfigLightingSceneService.getByIdValidate(
                sceneId, LIGHTING_SCENE_NOT_FOUND_MESSAGE);
        if (StringUtils.equals(dbLightingScene.getProjectId(), projectId)
                && dbLightingScene.getSt() == StateTypeEnum.VALID
                && dbLightingScene.getDemandType() == DemandTypeEnum.LIGHTING
                && Objects.equals(dbLightingScene.getRelDemandTemplateId(), templateId)) {
            return dbLightingScene;
        }
        throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LIGHTING_SCENE_NOT_FOUND_MESSAGE);
    }

    @Override
    public DemandConfigLighting getSceneObjectSettings(String groupCode, String projectId, String instanceId, Long templateId, Long sceneId) {
        DemandConfigLightingQueryDaoParam queryDaoParam = new DemandConfigLightingQueryDaoParam();
        queryDaoParam.setProjectId(projectId);
        queryDaoParam.setDemandInstanceId(instanceId);
        queryDaoParam.setRelDemandTemplateId(templateId);
        queryDaoParam.setRelDemandLightingSceneId(sceneId);
        return demandConfigLightingService.get(projectId, queryDaoParam);
    }

    @Override
    public DemandLightingTemplateTypeVO getTemplateFuncTypeList(String groupCode, String projectId, Long templateId, Long sceneId) {
        // 1.验证模板是否存在
        DemandTemplate demandTemplate = demandLightingStrategy.getTemplateAndCheckThrow(projectId, templateId);

        // 2.获取当前目标类型下所有的照明功能类型树
        DemandLightingTypeGroupTree lightingTypeGroupTree = demandConfigLightingHandler.getDemandLightingTypeGroupTree(projectId, templateId, demandTemplate.getDemandTemplateType(), null, null, 0);

        // 3.获取所有的场景配置数据
        DemandConfigLightingSceneQueryDaoParam sceneQueryDaoParam = new DemandConfigLightingSceneQueryDaoParam();
        sceneQueryDaoParam.setId(sceneId);
        sceneQueryDaoParam.setRelDemandTemplateId(templateId);
        List<DemandConfigLightingScene> lightingScenes = demandConfigLightingSceneService.list(projectId, sceneQueryDaoParam);
        TreeMap<Long, String> sceneNameMap = new TreeMap<>();
        Map<Long, Integer> sceneIdAndSequence = new HashMap<>();
        if (CollectionUtil.isNotEmpty(lightingScenes)) {
            for (DemandConfigLightingScene lightingScene : lightingScenes) {
                sceneNameMap.put(lightingScene.getId(), lightingScene.getSceneName());
                sceneIdAndSequence.put(lightingScene.getId(), lightingScene.getSequence());
            }
        }

        // 3.获取当前模板下所有的回路配置详情
        List<DemandConfigLightingResult> circuitResults = queryLightingConfigResult(projectId, templateId, sceneId);

        // 4.处理回路、场景、类型之间的数据关系
        DemandLightingTemplateTypeVO result = demandConfigLightingHandler.lightingFuncTypeTree(
                lightingTypeGroupTree.getFuncTypeSettings(), circuitResults,
                sceneNameMap, demandTemplate.getDemandTemplateType());

        // 5.场景排序
        return sortScene(result, sceneIdAndSequence);
    }

    @Override
    public DemandLightingCircuitView getTemplateCircuitView(String groupCode, DemandLightingCircuitViewParam viewParam) {
        // 1.验证模板是否存在
        DemandTemplate demandTemplate = demandLightingStrategy.getTemplateAndCheckThrow(viewParam.getProjectId(), viewParam.getTemplateId());

        // 2.获取当前目标类型下所有的照明功能类型树
        DemandLightingTypeGroupTree lightingTypeGroupTree = this.demandConfigLightingHandler.getDemandLightingTypeGroupTree(viewParam.getProjectId(), viewParam.getTemplateId(), demandTemplate.getDemandTemplateType(), null, null, 0);

        // 3.获取所有的场景配置数据
        DemandConfigLightingSceneQueryDaoParam sceneQueryDaoParam = new DemandConfigLightingSceneQueryDaoParam();
        sceneQueryDaoParam.setId(viewParam.getSceneId());
        sceneQueryDaoParam.setRelDemandTemplateId(viewParam.getTemplateId());
        List<DemandConfigLightingScene> lightingScenes = this.demandConfigLightingSceneService.list(viewParam.getProjectId(), sceneQueryDaoParam);
        TreeMap<Long, String> sceneNameMap = new TreeMap<>();
        Map<Long, Integer> sceneIdAndSequence = new HashMap<>();
        if (CollectionUtil.isNotEmpty(lightingScenes)) {
            for (DemandConfigLightingScene lightingScene : lightingScenes) {
                sceneNameMap.put(lightingScene.getId(), lightingScene.getSceneName());
                sceneIdAndSequence.put(lightingScene.getId(), lightingScene.getSequence());
            }
        }
        // 4.获取当前模板下所有的回路配置详情
        List<DemandConfigLightingResult> circuitResults = queryLightingConfigResult(viewParam.getProjectId(), viewParam.getTemplateId(), viewParam.getSceneId());

        // 5.根据传参，处理对应的照明回路，返回回路列表
        DemandLightingCircuitView circuitView = this.demandConfigLightingHandler.queryLightingCircuitListByType(lightingTypeGroupTree.getFuncTypeSettings(), circuitResults, sceneNameMap, viewParam.getSettingMode(), viewParam.getFuncType(), viewParam.getLampFuncType());
        List<DemandLightingCircuitSettingVO> allCircuitSettings = circuitView.getCircuitSettings();
        allCircuitSettings.sort(Comparator.comparing(DemandLightingCircuitSettingVO::getCircuitLocalName, Comparator.nullsFirst(String::compareTo)));

        // 6.分页数据处理
        if (viewParam.getCurrent() == null || viewParam.getCurrent() < 1 || viewParam.getSize() == null || viewParam.getSize() < 1 || CollectionUtil.isEmpty(allCircuitSettings)) {
            return sortScene(fillCircuitFeedbackAdjustmentServiceSpaces(circuitView), sceneIdAndSequence);
        }
        List<List<DemandLightingCircuitSettingVO>> partition = Lists.partition(allCircuitSettings, viewParam.getSize());
        if (partition.size() >= viewParam.getCurrent()) {
            circuitView.setCircuitSettings(partition.get(viewParam.getCurrent() - 1));
            return sortScene(fillCircuitFeedbackAdjustmentServiceSpaces(circuitView), sceneIdAndSequence);
        }
        circuitView.setCircuitSettings(Lists.newArrayList());

        // 7. 场景排序
        return sortScene(fillCircuitFeedbackAdjustmentServiceSpaces(circuitView), sceneIdAndSequence);
    }

    @Override
    public DemandConfigLightingDataVO getSceneObjectSettings(DemandLightingInstanceQueryParam param) {
        StopWatch stopWatch = new StopWatch("getSceneObjectSettings 耗时统计");
        // 1.校验模板是否存在
        if (null == param.getDemandTemplateType() && null == param.getTemplateId()) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0402.getCode(), LocaleMessageUtil.getMessage("key_af40d131ac834883a433cfb14d247793"));
        }
        if (null == param.getDemandTemplateType()) {
            DemandTemplate demandTemplate = demandLightingStrategy.getTemplateAndCheckThrow(param.getProjectId(), param.getTemplateId());
            param.setDemandTemplateType(demandTemplate.getDemandTemplateType());
        }

        DemandConfigLightingDataVO dataVO = new DemandConfigLightingDataVO();
        DemandConfigLightingGlobalConfigVO globalConfigVO = new DemandConfigLightingGlobalConfigVO();
        dataVO.setGlobal(globalConfigVO);

        // 1、=====查询实例ID对应的基线配置树=====
        stopWatch.start("查询实例ID对应的基线配置树");
        DemandLightingTypeGroupTree lightingTypeGroupTree = demandConfigLightingHandler.getDemandLightingTypeGroupTree(param.getProjectId(), param.getTemplateId(), param.getDemandTemplateType(),
                param.getInstanceId(), param.getInstanceType(), -1);

        if (CollUtil.isEmpty(lightingTypeGroupTree.getFuncTypeSettings())) {
            log.info("无可配置回路对象");
            return new DemandConfigLightingDataVO();
        }
        stopWatch.stop();

        //=====2、查询当前需求实例配置，若无配置则查询上级配置=====
        stopWatch.start("查询当前需求实例配置");
        DemandConfigLighting demandConfigLighting = getInstanceConfig(param.getProjectId(), param.getInstanceId(),
                param.getInstanceType(), param.getTemplateId(), param.getDemandTemplateType(), param.getSceneId());
        // 回路总数
        int total = 0;
        for (DemandLightingFuncTypeSettingVO funcTypeSetting : lightingTypeGroupTree.getFuncTypeSettings()) {
            total += funcTypeSetting.getCircuitIds().size();
        }
        if (null == demandConfigLighting) {
            //-----未配置 直接返回-----
            DemandLightingCircuitSummaryVO summaryVO = new DemandLightingCircuitSummaryVO(total, null, null, null, null);
            globalConfigVO.setSummary(summaryVO);
            return dataVO;
        }
        stopWatch.stop();
        DemandConfigLightingDataDTO configData = demandConfigLighting.getConfigData();
        fillTreeWithGroup(lightingTypeGroupTree, configData);
        //=====3、构建返回数据结构=====
        //-----配置来源-----
        dataVO.setSourceId(demandConfigLighting.getDemandInstanceId());
        dataVO.setSourceName(demandConfigLighting.getDemandInstanceName());
        stopWatch.start("全局配置类统计");
        // 全局配置
        if (CollUtil.isEmpty(configData.getGlobalSettings())) {
            DemandLightingCircuitSummaryVO summaryVO = new DemandLightingCircuitSummaryVO(total, null, null, null, null);
            globalConfigVO.setSummary(summaryVO);
        } else {
            //-----全局配置构建-----
            List<DemandLightingFuncTypeSettingVO> funcTypeSettings = demandConfigLightingHandler.globalConfigBuilding(lightingTypeGroupTree,
                    configData.getGlobalSettings());
            globalConfigVO.setFuncTypeSettings(funcTypeSettings);
            //-----统计-----
            demandConfigLightingHandler.configSummary(globalConfigVO);
        }
        setSpaceFeedbackAdjust(demandConfigLighting, lightingTypeGroupTree, dataVO);
        stopWatch.stop();

        //=====空间类型配置=====
        stopWatch.start("空间类型配置统计");
        List<DemandLightingSpaceFuncTypeSettingVO> spaceFuncTypeSettings;

        if (CollUtil.isNotEmpty(configData.getSpaceSettings()) && !DemandInstanceTypeEnum.SPACE.equals(demandConfigLighting.getDemandInstanceType())) {
            spaceFuncTypeSettings = demandConfigLightingHandler.spaceConfigBuildingAndSummary(param.getInstanceType(), lightingTypeGroupTree, configData.getSpaceSettings());
            if (CollUtil.isNotEmpty(spaceFuncTypeSettings)) {
                //-----空间在引用时，根据该空间的空间类型查询是否有空间类型的配置，若有则页面显示的内容为空间类型的配置；若没有则引用全局配置-----
                if (DemandInstanceTypeEnum.SPACE.equals(param.getInstanceType())) {
                    //-----使用空间类型配置作为全局配置使用-----
                    dataVO.getGlobal().setFuncTypeSettings(spaceFuncTypeSettings.get(0).getFuncTypeSettings());
                    dataVO.getGlobal().setSummary(spaceFuncTypeSettings.get(0).getSummary());
                } else {
                    DemandConfigLightingSpaceConfigVO spaceConfigVO = new DemandConfigLightingSpaceConfigVO();
                    spaceConfigVO.setSettings(spaceFuncTypeSettings);
                    dataVO.setSpace(spaceConfigVO);
                }
            }
        }
        stopWatch.stop();
        log.info(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        return dataVO;
    }

    /**
     * 填充带分组配置的树
     *
     * @param tree       树
     * @param configData 配置数据
     * @since 7.250602.0
     */
    private void fillTreeWithGroup(
            @NonNull DemandLightingTypeGroupTree tree,
            @NonNull DemandConfigLightingDataDTO configData) {
        List<DemandLightingFuncTypeSettingDTO> globalSettings = configData.getGlobalSettings();
        List<DemandLightingSpaceFuncTypeSettingDTO> spaceSettings = configData.getSpaceSettings();

        Set<Long> allGroupIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(globalSettings)) {
            for (DemandLightingFuncTypeSettingDTO globalSetting : globalSettings) {
                Map<Long, DemandLightingTypeSettingModeEnum> groupIdAndSettingMode =
                        DemandLightingCircuitGroupSettingDTO.toGroupIdAndSettingMode(
                                globalSetting.getCircuitGroupSettings());
                if (MapUtils.isEmpty(groupIdAndSettingMode)) {
                    continue;
                }
                allGroupIds.addAll(groupIdAndSettingMode.keySet());
            }
        }
        if (CollectionUtils.isNotEmpty(spaceSettings)) {
            for (DemandLightingSpaceFuncTypeSettingDTO spaceSetting : spaceSettings) {
                List<DemandLightingFuncTypeSettingDTO> funcTypeSettings = spaceSetting.getFuncTypeSettings();
                if (CollectionUtils.isEmpty(funcTypeSettings)) {
                    continue;
                }
                for (DemandLightingFuncTypeSettingDTO funcTypeSetting : funcTypeSettings) {
                    Map<Long, DemandLightingTypeSettingModeEnum> groupIdAndSettingMode =
                            DemandLightingCircuitGroupSettingDTO.toGroupIdAndSettingMode(
                                    funcTypeSetting.getCircuitGroupSettings());
                    if (MapUtils.isEmpty(groupIdAndSettingMode)) {
                        continue;
                    }
                    allGroupIds.addAll(groupIdAndSettingMode.keySet());
                }
            }
        }
        Map<Long, Set<String>> groupIdAndCircuitIds = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allGroupIds)) {
            DemandInstanceGroupQueryDaoParam instanceGroupQueryDaoParam = new DemandInstanceGroupQueryDaoParam();
            instanceGroupQueryDaoParam.setIds(allGroupIds);
            instanceGroupQueryDaoParam.setQueryDetail(true);
            List<DemandInstanceGroupPO> groups = demandInstanceGroupResource.list(instanceGroupQueryDaoParam);
            if (CollectionUtils.isNotEmpty(groups)) {
                for (DemandInstanceGroupPO group : groups) {
                    Long groupId = group.getId();
                    List<DemandInstanceGroupDetail> details = group.getDetails();
                    if (CollectionUtils.isNotEmpty(details)) {
                        groupIdAndCircuitIds.put(groupId, details.stream()
                                .map(DemandInstanceGroupDetail::getInstanceId)
                                .collect(Collectors.toSet()));
                    }
                }
            }

        }
        tree.setGroupIdAndCircuitIds(groupIdAndCircuitIds);
        tree.fillFuncTypeWithGroup();
        tree.fillRoomFuncTypeWithGroup();
    }

    /**
     * 设置反馈调节返回对象
     *
     * @param lightingTypeGroupTree
     * @param dataVO
     */
    private void setSpaceFeedbackAdjust(DemandConfigLighting demandConfigLighting, DemandLightingTypeGroupTree lightingTypeGroupTree,
                                        DemandConfigLightingDataVO dataVO) {
        if (lightingTypeGroupTree == null || CollUtil.isEmpty(lightingTypeGroupTree.getFuncTypeSettings())) {
            return;
        }
        // 取出回路列表circuitSettings
        List<DemandLightingCircuitSettingVO> lightingCircuits = new ArrayList<>();
        lightingTypeGroupTree.getFuncTypeSettings().forEach(funcTypeSetting -> lightingCircuits.addAll(funcTypeSetting.getCircuitSettings()));
        dataVO.setSpaceFeedbackAdjust(new LightingSpaceFeedbackAdjustSettingVO(demandConfigLighting.getId(), lightingCircuits));
    }

    @Override
    public Map<String, List<DemandLightingFuncTypeSettingDTO>> listAllSpaceSceneObjectSettings(String projectId, Long templateId, Long sceneId) {
        Map<String, List<DemandLightingFuncTypeSettingDTO>> map = new HashMap<>();

        try {
            // 1. 校验模板是否存在
            DemandTemplate demandTemplate = demandLightingStrategy.getTemplateAndCheckThrow(projectId, templateId);

            // 2、查询所有空间已保存的配置
            DemandConfigLightingQueryDaoParam param = new DemandConfigLightingQueryDaoParam();
            param.setRelDemandTemplateId(templateId);
            param.setRelDemandLightingSceneId(sceneId);
            List<DemandConfigLighting> demandConfigLightingList = demandConfigLightingService.list(projectId, param);
            Map<String, DemandConfigLightingDataDTO> id2config = demandConfigLightingList.stream().collect(Collectors.toMap(DemandConfigLighting::getDemandInstanceId, DemandConfigLighting::getConfigData));
            List<DtpRwdTreeLightingObjectDTO> rwdObjLst = demandLightingStrategy
                    .queryBuildingFloorSpaceObject(projectId, demandTemplate.getDemandTemplateType());
            getInstanceConfig(id2config, param.getProjectId(), rwdObjLst);

            // 3、构建最终返回结果
            return demandLightingStrategy.spaceSettingsBuilding(projectId, demandTemplate.getDemandTemplateType(), id2config);

        } catch (Exception e) {
            // 日志记录异常或进行其他处理
            log.error("listAllSpaceSceneObjectSettings异常", e);
        }

        return map;
    }

    private void getInstanceConfig(Map<String, DemandConfigLightingDataDTO> id2config, String parentId, List<DtpRwdTreeLightingObjectDTO> rwdObjLst) {
        if (CollUtil.isEmpty(rwdObjLst)) {
            return;
        }
        for (DtpRwdTreeLightingObjectDTO rwdObj : rwdObjLst) {
            if (!id2config.containsKey(rwdObj.getId())) {
                id2config.put(rwdObj.getId(), id2config.get(parentId));
            }
            getInstanceConfig(id2config, rwdObj.getId(), rwdObj.getChildrenList());
        }
    }


    @Override
    public DemandConfigLighting getReferenceSceneObjectSettings(DemandLightingInstanceQueryParam instanceQueryParam, DemandTemplate demandTemplate) {
        // 1.获取当前实例所引用的配置
        DemandConfigLightingDataVO sceneObjectSettings = getSceneObjectSettings(instanceQueryParam);
        boolean invalid = sceneObjectSettings == null || (sceneObjectSettings.getGlobal() == null && sceneObjectSettings.getSpace() == null);
        if (invalid) {
            return null;
        }
        DemandConfigLighting demandConfigLighting = new DemandConfigLighting();
        demandConfigLighting.setProjectId(instanceQueryParam.getProjectId());
        demandConfigLighting.setDemandType(demandTemplate.getDemandType());
        demandConfigLighting.setDemandTemplateType(demandTemplate.getDemandTemplateType());
        demandConfigLighting.setDemandTemplateTag(demandTemplate.getDemandTemplateTag());
        demandConfigLighting.setDemandInstanceId(instanceQueryParam.getInstanceId());
        demandConfigLighting.setDemandInstanceType(instanceQueryParam.getInstanceType());
        demandConfigLighting.setIsCustomizedSetting(true);
        demandConfigLighting.setRelDemandTemplateId(instanceQueryParam.getTemplateId());
        demandConfigLighting.setRelDemandLightingSceneId(instanceQueryParam.getSceneId());

        DemandConfigLightingDataDTO configLightingDataDTO = new DemandConfigLightingDataDTO();
        configLightingDataDTO.setGlobalSettings(Lists.newArrayList());
        configLightingDataDTO.setSpaceSettings(Lists.newArrayList());

        List<DemandLightingFuncTypeSettingVO> globalSettings = sceneObjectSettings.getGlobal() == null ? null : sceneObjectSettings.getGlobal().getFuncTypeSettings();
        if (CollectionUtils.isNotEmpty(globalSettings)) {
            for (DemandLightingFuncTypeSettingVO globalSetting : globalSettings) {
                DemandLightingFuncTypeSettingDTO funcTypeSettingDTO = JsonUtils.fromJson(JsonUtils.toJson(globalSetting), new TypeReference<DemandLightingFuncTypeSettingDTO>() {
                });
                configLightingDataDTO.getGlobalSettings().add(funcTypeSettingDTO);
            }
        }
        List<DemandLightingSpaceFuncTypeSettingVO> spaceSettings = sceneObjectSettings.getSpace() == null ? null : sceneObjectSettings.getSpace().getSettings();
        if (CollectionUtils.isNotEmpty(spaceSettings)) {
            for (DemandLightingSpaceFuncTypeSettingVO spaceSetting : spaceSettings) {
                DemandLightingSpaceFuncTypeSettingDTO spaceFuncTypeSettingDTO = JsonUtils.fromJson(JsonUtils.toJson(spaceSetting), new TypeReference<DemandLightingSpaceFuncTypeSettingDTO>() {
                });
                spaceFuncTypeSettingDTO.setConfigId(MD5.create().digestHex(JsonUtils.toJson(spaceFuncTypeSettingDTO.getSpaceFuncTypes())));
                configLightingDataDTO.getSpaceSettings().add(spaceFuncTypeSettingDTO);
            }
        }
        demandConfigLighting.setConfigData(configLightingDataDTO);
        return demandConfigLighting;
    }

    @Override
    public List<DemandLightingSpaceFuncTypeVO> listSpaceFuncTypeList(String groupCode, DemandLightingInstanceQueryParam instanceQueryParam, boolean filterExists) {
        boolean oneself = "0".equals(instanceQueryParam.getReferenceStatus());

        // 1.验证模板是否存在
        DemandTemplate demandTemplate = demandLightingStrategy.getTemplateAndCheckThrow(instanceQueryParam.getProjectId(), instanceQueryParam.getTemplateId());

        // 1.获取当前实例已配置的空间功能类型
        DemandConfigLighting currentConfigLighting = oneself ? getSceneObjectSettings(groupCode, instanceQueryParam.getProjectId(), instanceQueryParam.getInstanceId(), instanceQueryParam.getTemplateId(), instanceQueryParam.getSceneId())
                : getReferenceSceneObjectSettings(instanceQueryParam, demandTemplate);

        DemandLightingTypeGroupTree lightingTypeGroupTree = demandConfigLightingHandler.getDemandLightingTypeGroupTree(instanceQueryParam.getProjectId(), instanceQueryParam.getTemplateId(), demandTemplate.getDemandTemplateType(),
                instanceQueryParam.getInstanceId(), instanceQueryParam.getInstanceType(), 1);
        List<DemandLightingSpaceFuncTypeVO> spaceFuncTypes = lightingTypeGroupTree == null || CollectionUtil.isEmpty(lightingTypeGroupTree.getSpaceFuncTypeSettings()) ? Lists.newArrayList() : lightingTypeGroupTree.getSpaceFuncTypeSettings();
        if (currentConfigLighting == null || currentConfigLighting.getConfigData() == null || CollectionUtil.isEmpty(currentConfigLighting.getConfigData().getSpaceSettings())) {
            return spaceFuncTypes;
        }
        List<DemandLightingSpaceFuncTypeSettingDTO> currentSpaceSettings = currentConfigLighting.getConfigData().getSpaceSettings();
        Set<String> existsSpaceFuncTypes = new HashSet<>();
        currentSpaceSettings.forEach(item -> item.getSpaceFuncTypes().forEach(type -> existsSpaceFuncTypes.add((String) type.getKey())));

        List<DemandLightingSpaceFuncTypeVO> result = new ArrayList<>();
        for (DemandLightingSpaceFuncTypeVO spaceFuncTypeVO : spaceFuncTypes) {
            boolean contains = existsSpaceFuncTypes.contains(spaceFuncTypeVO.getSpaceFuncType());
            spaceFuncTypeVO.setSelect(contains);
            if (filterExists && !contains) {
                result.add(spaceFuncTypeVO);
            }
        }
        return filterExists ? result : spaceFuncTypes;
    }

    @Override
    @SneakyThrows
    public List<DemandLightingSpaceFuncTypeVO> listSpaceFuncTypeList(DemandLightingParam param) {

        // 1、获取空间功能类型
        DemandLightingTypeGroupTree lightingTypeGroupTree = demandConfigLightingHandler.getDemandLightingTypeGroupTree(param.getProjectId(), null, param.getDemandTemplateType(),
                param.getInstanceId(), param.getInstanceType(), 1);
        List<DemandLightingSpaceFuncTypeVO> spaceFuncTypes = lightingTypeGroupTree == null || CollectionUtil.isEmpty(lightingTypeGroupTree.getSpaceFuncTypeSettings()) ? Lists.newArrayList() : lightingTypeGroupTree.getSpaceFuncTypeSettings();

        // 2.获取当前实例已配置的空间功能类型
        List<DemandLightingSpaceFuncTypeSettingDTO> currentSpaceSettings = null;
        if (param.getInterventionId() != null) {
            InterventionConfig interventionConfig = interventionConfigQueryResourceService.getById(param.getInterventionId());
            String configData = null;
            if (interventionConfig.getConfigData() != null) {
                configData = objectMapper.writeValueAsString(interventionConfig.getConfigData());
            }
            if (!StringUtils.isEmpty(configData)) {
                DemandConfigLightingDataDTO configLightingDataDTO = JSON.parseObject(configData, DemandConfigLightingDataDTO.class);
                currentSpaceSettings = configLightingDataDTO.getSpaceSettings();
            }

        }

        // 3、 设置已选setSelect
        if (CollUtil.isEmpty(currentSpaceSettings)) {
            return spaceFuncTypes;
        }
        Set<String> existsSpaceFuncTypes = new HashSet<>();
        currentSpaceSettings.forEach(item -> item.getSpaceFuncTypes().forEach(type -> existsSpaceFuncTypes.add((String) type.getKey())));

        for (DemandLightingSpaceFuncTypeVO spaceFuncTypeVO : spaceFuncTypes) {
            boolean contains = existsSpaceFuncTypes.contains(spaceFuncTypeVO.getSpaceFuncType());
            spaceFuncTypeVO.setSelect(contains);
        }
        return spaceFuncTypes;
    }

    @Override
    public List<DemandLightingFloorWithCircuitVO> listFloorWithCircuits(
            @NonNull DemandLightingFloorWithCircuitsQueryParam param) {
        String projectId = param.getProjectId();
        Set<String> floorIds = param.getFloorIds();
        Set<String> circuitIds = param.getCircuitIds();
        DemandTemplate dbTemplate = demandLightingStrategy.getTemplateAndCheckThrow(projectId, param.getTemplateId());
        Long templateId = dbTemplate.getId();
        DemandConfigLightingScene dbLightingScene = getSceneAndCheckThrow(
                projectId, templateId, param.getSceneId());
        Long sceneId = dbLightingScene.getId();
        DemandTemplateTypeEnum templateType = dbTemplate.getDemandTemplateType();

        List<DtpRwdFloorDTO> floors = demandLightingStrategy.listFloorsByCircuit(
                projectId, templateType, circuitIds, floorIds);
        if (CollectionUtils.isEmpty(floors)) {
            return Lists.newArrayList();
        }
        Set<String> allRelCircuitIds = floors.stream()
                .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds()))
                .flatMap(item -> item.getRelObjIds().stream())
                .collect(Collectors.toSet());

        // 查询楼层下所有照明回路
        Map<String, DtpRwdCircuitDTO> circuitIdAndData = demandLightingStrategy.listCircuits(
                projectId, templateType, allRelCircuitIds).stream().collect(Collectors.toMap(
                DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
        // 查询楼层下所有照明回路配置结果
        List<DemandConfigLightingResult> dbConfigResults = queryLightingConfigResult(
                projectId, templateId, sceneId, allRelCircuitIds);
        Map<String, DemandLightingCircuitSettingModeEnum> circuitIdAndSettingMode = dbConfigResults.stream()
                .collect(Collectors.toMap(DemandConfigLightingResult::getCircuitId,
                        item -> item.getConfigData().getCircuitSettingMode(), (o1, o2) -> o1));

        List<DemandLightingFloorWithCircuitVO> result = new ArrayList<>();
        for (DtpRwdFloorDTO floor : floors) {
            Set<String> relCircuitIds = floor.getRelObjIds();

            Set<DemandLightingCircuitVO> relCircuits = new HashSet<>();
            if (CollectionUtils.isNotEmpty(relCircuitIds)) {
                for (String relCircuitId : relCircuitIds) {
                    DtpRwdCircuitDTO circuit = circuitIdAndData.get(relCircuitId);
                    if (Objects.nonNull(circuit)) {
                        relCircuits.add(demandLightingConverter.toCircuitVo(
                                circuit, circuitIdAndSettingMode.get(relCircuitId)));
                    }
                }
            }
            result.add(demandLightingConverter.toFloorWithCircuitVo(floor, relCircuits));
        }
        return result;
    }

    @Override
    public List<DemandTemplateVO> listExistsSceneConfigTemplate(String groupCode, String projectId, Long templateId) {
        // 1.校验模板是否存在
        DemandTemplate demandTemplate = demandLightingStrategy.getTemplateAndCheckThrow(projectId, templateId);

        // 2.获取此需求类型下，所有的场景下实例配置数据
        DemandConfigLightingQueryDaoParam queryDaoParam = new DemandConfigLightingQueryDaoParam();
        queryDaoParam.setDemandType(demandTemplate.getDemandType());
        queryDaoParam.setDemandTemplateType(demandTemplate.getDemandTemplateType());
        List<DemandConfigLighting> lightingConfigs = demandConfigLightingService.list(projectId, queryDaoParam);
        if (CollectionUtil.isEmpty(lightingConfigs)) {
            return Lists.newArrayList();
        }

        // 3.获取存在配置数据的所有模板
        List<Long> templateIds = lightingConfigs.stream().filter(item -> !item.getRelDemandTemplateId().equals(templateId) && item.getConfigData() != null
                        && (CollectionUtil.isNotEmpty(item.getConfigData().getGlobalSettings()) || CollectionUtil.isNotEmpty(item.getConfigData().getSpaceSettings())))
                .map(DemandConfigLighting::getRelDemandTemplateId).distinct().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(templateIds)) {
            return Lists.newArrayList();
        }
        DemandTemplateQueryDaoParam templateQueryDaoParam = new DemandTemplateQueryDaoParam();
        templateQueryDaoParam.setIds(templateIds);
        templateQueryDaoParam.setIsEnabled(true);
        List<DemandTemplate> demandTemplates = demandTemplateService.list(templateQueryDaoParam);
        return demandCalendarConverter.toTemplateVo(demandTemplates);
    }

    @Override
    public List<DemandConfigLightingScene> listSceneConfig(String groupCode, String projectId, Long templateId) {
        DemandConfigLightingSceneQueryDaoParam param = new DemandConfigLightingSceneQueryDaoParam();
        param.setRelDemandTemplateId(templateId);
        List<DemandConfigLightingScene> demandConfigLightingScenes = demandConfigLightingSceneService.list(projectId, param);
        if (CollectionUtils.isEmpty(demandConfigLightingScenes)) {
            return Lists.newArrayList();
        }
        return demandConfigLightingScenes;
    }

    @Override
    public List<DemandLightingAllSceneVO> listAllScene(DemandTemplateBatchQueryParam param){
        List<DemandLightingAllSceneVO> result = Lists.newArrayList();
        //获取类型下所有模版
        List<DemandTemplateVO> demandTemplateVOS = demandCalendarQueryService.listTemplate(param);
        if(CollectionUtil.isEmpty(demandTemplateVOS)){
            return result;
        }

        Map<Long, DemandTemplateVO> demandTemplateMap = demandTemplateVOS.stream().collect(Collectors.toMap(DemandTemplateVO::getTemplateId, Function.identity()));
        Set<Long> templateIds = demandTemplateVOS.stream().map(DemandTemplateVO::getTemplateId).collect(Collectors.toSet());
        DemandConfigLightingSceneQueryDaoParam sceneQueryDaoParam = new DemandConfigLightingSceneQueryDaoParam();
        sceneQueryDaoParam.setRelDemandTemplateIds(templateIds);
        List<DemandConfigLightingScene> scenes = demandConfigLightingSceneService.list(ContextUtil.getProjectId(), sceneQueryDaoParam);
        if(CollectionUtil.isNotEmpty(param.getExcludeSceneIds())) {
            scenes = scenes.stream().filter(scene -> !param.getExcludeSceneIds().contains(scene.getId())).collect(Collectors.toList());
        }

        Map<Long, List<DemandConfigLightingScene>> sceneMap = scenes.stream().collect(Collectors.groupingBy(DemandConfigLightingScene::getRelDemandTemplateId));
        for (Long templateId : demandTemplateMap.keySet()) {
            DemandTemplateVO template = demandTemplateMap.get(templateId);
            List<DemandConfigLightingScene> sceneList = sceneMap.get(templateId);
            if (template != null && CollectionUtil.isNotEmpty(sceneList)) {
                DemandLightingAllSceneVO vo = new DemandLightingAllSceneVO();
                vo.setTemplate(template);
                vo.setScenes(sceneList);
                result.add(vo);
            }
        }

        Comparator<DemandLightingAllSceneVO> comparator = Comparator.comparing(
                item -> item.getTemplate().getTemplateId(),
                Comparator.nullsLast(Comparator.naturalOrder()));
        CollectionUtil.sort(result, comparator);
        return result;
    }

    @Override
    public List<DemandLightingSpaceCircuitVO> listSpaceCfgStatistics(CircuitObjCfgStatisticsQueryParam param) {
        List<DtpRwdSpaceDTO> spaces = null;
        // 空间回路关系
        Map<String, Set<String>> map = new HashMap<>(16);

        // 项目纬度的配置不进行 回路配置的过滤
        if (!isProjectCfg(param.getTemplateType())) {
            // 通过空间查询 空间下的负载id
            spaces = demandLightingStrategy.listSpacesByCircuit(param.getProjectId(), param.getTemplateType(), null, param.getSpaceIds());
            spaces.forEach(i -> map.put(i.getId(), i.getRelObjIds()));
            if (CollectionUtils.isEmpty(spaces)) {
                return Lists.newArrayList();
            }
        }

        // 获取回路的配置状态
        DemandConfigLightingResultQueryDaoParam qp = getDemandConfigLightingResultQueryDaoParam(param, spaces);
        // 回路配置查询
        List<DemandConfigLightingResult> cfgAll = demandConfigLightingResultService.list(param.getProjectId(), qp);

        // 项目级别计算
        List<DemandLightingSpaceCircuitVO> cgfCircuit = new ArrayList<>();
        if (isProjectCfg(param.getTemplateType())) {
            computingType(cgfCircuit, param.getProjectId(), cfgAll);
        } else {
            // 空间级别计算
            Map<String, DemandConfigLightingResult> hlCfg = cfgAll.stream().collect(Collectors.toMap(DemandConfigLightingResult::getCircuitId, Function.identity()));
            // 按照空间纬度分组
            Map<String, List<DemandConfigLightingResult>> spaceMap = new HashMap<>(Collections.emptyMap());
            map.forEach((k, v) -> {
                List<DemandConfigLightingResult> list = new ArrayList<>();
                for (String i : v) {
                    if (hlCfg.get(i) != null) {
                        list.add(hlCfg.get(i));
                    } else {
                        // 没有配置给默认值
                        list.add(new DemandConfigLightingResult());
                    }
                }
                spaceMap.put(k, list);
            });
            spaceMap.forEach((k, v) -> computingType(cgfCircuit, k, v));
        }
        return cgfCircuit;
    }

    @Override
    public List<DemandConfigLighting> listLightingConfigBySceneId(String projectId, Long sceneId) {
        return demandConfigLightingService.list(projectId, new DemandConfigLightingQueryDaoParam().setRelDemandLightingSceneId(sceneId));
    }

    @Override
    public Boolean spaceFeedbackAdjustSettingsCheck(SpaceFeedbackAdjustSettingsCheckParam param) {
        boolean r = false;

        // 查询空间对象
        DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam();
        spaceQueryParam.setId(param.getSpaceId());
        spaceQueryParam.addInfoCode("replacedByNatLighting", "1");
        DtpRwdSpaceDTO space = dtpRwdClientService.getSpace(spaceQueryParam);
        if (space == null) {
            return r;
        }

        // 查看空间的回路 是否有普通回路
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuitsBySpaceIds(
                ContextUtil.getProjectId(), param.getTemplateType(), Lists.newArrayList(param.getSpaceId()), null,
                null, null);
        if (CollectionUtils.isEmpty(circuits)) {
            return false;
        }
        for (DtpRwdCircuitDTO i : circuits) {
            if (DtpRwdCircuitFuncTypeEnum.GENERAL_LIGHTING.getValue().equals(i.getFuncType())) {
                r = true;
                break;
            }
        }
        return r;
    }

    /**
     * 排序场景
     *
     * @param target             数据源
     * @param sceneIdAndSequence 场景ID -> 顺序
     * @return 排序后的数据源
     * @since 7.250402.0
     */
    private DemandLightingCircuitView sortScene(DemandLightingCircuitView target, Map<Long, Integer> sceneIdAndSequence) {
        Comparator<DemandLightingSceneModel> comparator = Comparator.comparing(
                item -> sceneIdAndSequence.get(item.getSceneId()),
                Comparator.nullsLast(Comparator.naturalOrder()));

        List<DemandLightingCircuitSettingVO> circuitSettings = target.getCircuitSettings();
        if (CollectionUtils.isNotEmpty(circuitSettings)) {
            for (DemandLightingCircuitSettingVO circuitSetting : circuitSettings) {
                if (CollectionUtils.isNotEmpty(circuitSetting.getSceneModes())) {
                    circuitSetting.getSceneModes().sort(comparator);
                }
            }
        }
        return target;
    }

    /**
     * 排序场景
     *
     * @param target             数据源
     * @param sceneIdAndSequence 场景ID -> 顺序
     * @return 排序后的数据源
     * @since 7.250402.0
     */
    private DemandLightingTemplateTypeVO sortScene(DemandLightingTemplateTypeVO target, Map<Long, Integer> sceneIdAndSequence) {
        Comparator<DemandLightingSceneModel> comparator = Comparator.comparing(
                item -> sceneIdAndSequence.get(item.getSceneId()),
                Comparator.nullsLast(Comparator.naturalOrder()));

        List<DemandLightingSceneModel> sceneModes = target.getSceneModes();
        if (CollectionUtils.isNotEmpty(sceneModes)) {
            sceneModes.sort(comparator);
        }
        List<DemandLightingFuncTypeSettingVO> funcTypeSettings = target.getFuncTypeSettings();
        if (CollectionUtils.isNotEmpty(funcTypeSettings)) {
            for (DemandLightingFuncTypeSettingVO funcTypeSetting : funcTypeSettings) {
                if (CollectionUtils.isNotEmpty(funcTypeSetting.getSceneModes())) {
                    funcTypeSetting.getSceneModes().sort(comparator);
                }
                if (CollectionUtils.isNotEmpty(funcTypeSetting.getLampFuncTypeSettings())) {
                    for (DemandLightingLampFuncTypeSettingVO lampFuncTypeSetting : funcTypeSetting.getLampFuncTypeSettings()) {
                        if (CollectionUtils.isNotEmpty(lampFuncTypeSetting.getSceneModes())) {
                            lampFuncTypeSetting.getSceneModes().sort(comparator);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(funcTypeSetting.getCircuitSettings())) {
                    for (DemandLightingCircuitSettingVO circuitSetting : funcTypeSetting.getCircuitSettings()) {
                        if (CollectionUtils.isNotEmpty(circuitSetting.getSceneModes())) {
                            circuitSetting.getSceneModes().sort(comparator);
                        }
                    }
                }
            }
        }
        return target;
    }

    /**
     * 填充照明回路反馈调节服务空间
     *
     * @param target 目标源
     * @return 目标源
     * @since 5.241202.0
     */
    private DemandLightingCircuitView fillCircuitFeedbackAdjustmentServiceSpaces(@NonNull DemandLightingCircuitView target) {
        List<DemandLightingCircuitSettingVO> circuitSettings = target.getCircuitSettings();
        if (CollectionUtils.isEmpty(circuitSettings)) {
            return target;
        }
        boolean needQueryCircuitServiceSpaces = false;
        for (DemandLightingCircuitSettingVO circuitSetting : circuitSettings) {
            List<DemandLightingSceneModel> sceneModes = circuitSetting.getSceneModes();
            // 判断外层对象是否为有效的反馈调节配置
            if (validFeedbackAdjustmentConfig(circuitSetting.getSettingMode(), circuitSetting.getFeedbackAdjustSetting())) {
                needQueryCircuitServiceSpaces = true;
                break;
            }
            // 判断场景是否为有效的反馈调节配置
            if (CollectionUtils.isNotEmpty(sceneModes) && sceneModes.stream().anyMatch(item ->
                    validFeedbackAdjustmentConfig(item.getSettingMode(), item.getFeedbackAdjustSetting()))) {
                needQueryCircuitServiceSpaces = true;
                break;
            }
        }
        if (!needQueryCircuitServiceSpaces) {
            return target;
        }
        Set<String> circuitIds = circuitSettings.stream()
                .map(DemandLightingCircuitSettingVO::getCircuitId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(circuitIds)) {
            return target;
        }
        // 查询设备服务空间
        List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpaceServiceByEquipmentId(circuitIds,
                new DtpRwdObjectQueryParam().addWithColumn(DtpRwdObjectDTO.Fields.id,
                        DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.relObjIds));
        Map<String, List<DtpRwdSpaceDTO>> circuitIdAndRelSpaces = dtpRwdClientService.mapObjectIdAndRelObjects(spaces);
        if (MapUtils.isEmpty(circuitIdAndRelSpaces)) {
            return target;
        }
        for (DemandLightingCircuitSettingVO circuitSetting : circuitSettings) {
            List<DemandLightingSceneModel> sceneModes = circuitSetting.getSceneModes();
            if (CollectionUtils.isNotEmpty(sceneModes)) {
                for (DemandLightingSceneModel sceneMode : sceneModes) {
                    DemandLightingCircuitFeedbackAdjustSettingDTO sceneFeedbackAdjustmentConfig =
                            sceneMode.getFeedbackAdjustSetting();
                    this.validFeedbackAdjustmentConfig(sceneMode.getSettingMode(), sceneFeedbackAdjustmentConfig);
                }
            }
        }
        return target;
    }

    /**
     * 是否为有效的反馈调节
     *
     * @param settingMode              设置模式
     * @param feedbackAdjustmentConfig 反馈调节配置
     * @return 是否为有效的反馈调节
     * @since 5.241202.0
     */
    private boolean validFeedbackAdjustmentConfig(
            @Nullable DemandLightingCircuitSettingModeEnum settingMode,
            @Nullable DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustmentConfig) {
        return settingMode == DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT && Objects.nonNull(feedbackAdjustmentConfig);
    }

    /**
     * 查询当前需求实例配置，若无配置则查询上级配置
     *
     * @param projectId    项目ID
     * @param instanceId   实例ID
     * @param instanceType 实例类型
     * @param templateId   模版ID
     * @return 照明配置
     * @author yuecaipu
     * @since 24Q1 T3S1-5.17.0
     */
    private DemandConfigLighting getInstanceConfig(String projectId, String instanceId, DemandInstanceTypeEnum instanceType,
                                                   Long templateId, DemandTemplateTypeEnum templateType, Long sceneId) {

        //=====1、查询当前需求实例ID配置=====
        DemandConfigLightingQueryDaoParam daoParam = new DemandConfigLightingQueryDaoParam();
        daoParam.setDemandInstanceId(instanceId);
        daoParam.setDemandInstanceType(instanceType);
        daoParam.setDemandType(DemandTypeEnum.LIGHTING);
        daoParam.setRelDemandTemplateId(templateId);
        daoParam.setRelDemandLightingSceneId(sceneId);
        DemandConfigLighting demandConfigLighting = demandConfigLightingService.get(projectId, daoParam);

        //=====2、若当前需求实例ID无配置，查询上级配置=====
        if (null == demandConfigLighting && !instanceType.equals(DemandInstanceTypeEnum.PROJECT)) {
            //-----2.1、构建上级对象查询配置参数-----
            //-----获取上级配置对象-----
            DtpRwdTreeLightingObjectDTO object = getParent(projectId, instanceId, instanceType, templateType);
            instanceType = DemandInstanceTypeEnum.getParentInstanceType(instanceType);

            //-----2.2、查询上级对象配置-----
            demandConfigLighting = getInstanceConfig(projectId, object.getId(), instanceType, templateId, templateType, sceneId);
            if (null != demandConfigLighting && StrUtil.isEmpty(demandConfigLighting.getDemandInstanceName())) {
                demandConfigLighting.setDemandInstanceName(object.getLocalName());
            }
        }

        return demandConfigLighting;
    }

    /**
     * 获取上级配置对象
     *
     * @param projectId    项目ID
     * @param instanceId   实例ID
     * @param instanceType 实例类型
     * @param templateType 模版类型
     * @return 照明对象树DTO
     * @author yuecaipu
     * @since 24Q1 T3S1-5.17.0
     */
    private DtpRwdTreeLightingObjectDTO getParent(String projectId, String instanceId,
                                                  DemandInstanceTypeEnum instanceType, DemandTemplateTypeEnum templateType) {

        if (instanceType.equals(DemandInstanceTypeEnum.BUILDING)) {
            // 项目
            ProjectDTO project = midPermissionClientService.getProject(false, true, projectId);
            // 添加顶级项目信息节点
            DtpRwdTreeLightingObjectDTO dto = new DtpRwdTreeLightingObjectDTO();
            dto.setId(project.getProjectId());
            dto.setName(project.getProjectName());
            dto.setLocalName(project.getProjectLocalName());
            dto.setInstanceType(DemandInstanceTypeEnum.PROJECT);
            return dto;
        } else {
            // 获取主子结构关系对象数据
            List<DtpRwdTreeLightingObjectDTO> rwdObjLst = demandLightingStrategy.queryBuildingFloorSpaceObject(projectId, templateType);
            return findParentById(rwdObjLst, instanceId);
        }
    }


    private DtpRwdTreeLightingObjectDTO findParentById(List<DtpRwdTreeLightingObjectDTO> rwdObjLst, String instanceId) {
        if (CollUtil.isEmpty(rwdObjLst)) {
            return null;
        }
        for (DtpRwdTreeLightingObjectDTO dto : rwdObjLst) {
            if (CollUtil.isEmpty(dto.getChildrenList())) {
                continue;
            }
            for (DtpRwdTreeLightingObjectDTO child : dto.getChildrenList()) {
                if (child.getId().equals(instanceId)) {
                    return dto;
                } else {
                    DtpRwdTreeLightingObjectDTO parent = findParentById(dto.getChildrenList(), instanceId);
                    if (null != parent) {
                        return parent;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 查询照明配置结果
     *
     * @param projectId  项目ID
     * @param templateId 模版ID
     * @param sceneId    场景ID
     * @return 照明配置结果
     * @since 24Q1 T3S2-5.18.0
     */
    private List<DemandConfigLightingResult> queryLightingConfigResult(
            @NonNull String projectId, @NonNull Long templateId, @NonNull Long sceneId) {
        return queryLightingConfigResult(projectId, templateId, sceneId, null);
    }

    /**
     * 查询照明配置结果
     *
     * @param projectId  项目ID
     * @param templateId 模版ID
     * @param sceneId    场景ID
     * @param circuitIds 照明回路ID, 为空时查询所有
     * @return 照明配置结果
     * @since 24Q1 T3S2-5.18.0
     */
    private List<DemandConfigLightingResult> queryLightingConfigResult(
            @NonNull String projectId, @NonNull Long templateId,
            @NonNull Long sceneId, @Nullable Collection<String> circuitIds) {
        DemandConfigLightingResultQueryDaoParam queryDaoParam = new DemandConfigLightingResultQueryDaoParam();
        queryDaoParam.setCircuitIds(circuitIds);
        queryDaoParam.setRelDemandTemplateId(templateId);
        queryDaoParam.setRelDemandLightingSceneId(sceneId);
        return demandConfigLightingResultService.list(projectId, queryDaoParam);
    }

    /**
     * 配置数据打标
     *
     * @param vo  返回vo
     * @param ids 目标数据id
     */
    private void cfgMarking(DemandLightingTreeObjectVO vo, List<String> ids, DemandTreeQueryParam param) {

        Long templateId = param.getTemplateId();
        Long sceneId = param.getSceneId();
        // 获取对象配置信息
        List<String> cfgObjId = queryCustomizedSettingInstanceIds(param.getProjectId(), templateId, sceneId, ids);
        // 项目级别特殊处理
        if (CollectionUtils.isNotEmpty(cfgObjId) && cfgObjId.contains(vo.getId())) {
            vo.setIsCfg(true);
        }
        // 子节点处理
        recursionCalculate(vo.getChildren(), cfgObjId);
    }


    /**
     * 数据转换
     *
     * @param rwdObjLst
     * @return
     */
    private List<DemandLightingTreeObjectVO> getDemandLightingTreeObjectVOList(List<DtpRwdTreeLightingObjectDTO> rwdObjLst) {
        List<DemandLightingTreeObjectVO> result = new ArrayList<>();
        rwdObjLst.forEach(i -> {
            DemandLightingTreeObjectVO r = BeanUtil.toBean(i, DemandLightingTreeObjectVO.class);
            result.add(r);
            if (CollectionUtils.isNotEmpty(i.getChildrenList())) {
                r.setChildren(getDemandLightingTreeObjectVOList(i.getChildrenList()));
            }
        });
        return result;
    }

    /**
     * 是否配置打标
     * 小小递归一下
     *
     * @param children
     * @param cfgObjId
     */
    private void recursionCalculate(List<DemandLightingTreeObjectVO> children, List<String> cfgObjId) {
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        children.forEach(c -> {
            // 是否已配打标
            if (CollectionUtils.isNotEmpty(cfgObjId) && cfgObjId.contains(c.getId())) {
                c.setIsCfg(true);
            }
            recursionCalculate(c.getChildren(), cfgObjId);
        });
    }

    /**
     * 状态计算
     *
     * @param cgfCircuit
     * @param k
     * @param v
     */
    private void computingType(List<DemandLightingSpaceCircuitVO> cgfCircuit, String k, List<DemandConfigLightingResult> v) {
        DemandLightingSpaceCircuitVO vo = new DemandLightingSpaceCircuitVO();
        vo.setSpaceId(k);
        // 计算开启状态
        // 空间下载 负责总数
        int total = v.size();
        // 空间下开启数量
        int openTotal = 0;
        // 空间下关闭数量
        int closeTotal = 0;
        // 空间下反馈调节数量
        int feedbackAdjustmentTotal = 0;

        if (CollectionUtils.isNotEmpty(v)) {
            for (DemandConfigLightingResult vf : v) {
                if (vf == null || vf.getConfigData() == null) {
                    continue;
                }
                if (DemandLightingCircuitSettingModeEnum.CLOSE.equals(vf.getConfigData().getCircuitSettingMode())) {
                    closeTotal++;
                }
                if (DemandLightingCircuitSettingModeEnum.OPEN.equals(vf.getConfigData().getCircuitSettingMode())) {
                    openTotal++;
                }
                if (DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT.equals(vf.getConfigData().getCircuitSettingMode())) {
                    feedbackAdjustmentTotal++;
                }
            }
        }

        // 计算开启状态
        DemandLightingTypeSettingModeEnum type = DemandLightingTypeSettingModeEnum.NOT_CONFIGURED;
        // 空间下任何一个照明回路
        if (feedbackAdjustmentTotal > 0) {
            type = DemandLightingTypeSettingModeEnum.FEEDBACK_ADJUSTMENT;
        } else if (total == openTotal) {
            type = DemandLightingTypeSettingModeEnum.ALL_OPEN;
        } else if (total == closeTotal) {
            type = DemandLightingTypeSettingModeEnum.ALL_CLOSE;
        } else if (openTotal != 0) {
            type = DemandLightingTypeSettingModeEnum.PARTIAL_OPEN;
        }
        vo.setCircuitSettingMode(type);
        cgfCircuit.add(vo);
    }

    /**
     * 判断是否是项目纬度的配置
     *
     * @param templateType
     * @return
     */
    private boolean isProjectCfg(DemandTemplateTypeEnum templateType) {
        switch (templateType) {
            // 项目纬度配置
            case BUILDING_LIGHTING:
            case LANDSCAPE_LIGHTING:
            case SIGN_LIGHTING:
                return true;
            default:
                return false;
        }
    }

    /**
     * 查询自定义配置实例ID
     *
     * @param projectId       项目ID
     * @param templateId      模版ID
     * @param lightingSceneId 照明场景ID
     * @param instanceIds     实例ID, 为空时查询所有
     * @return 自定义配置实例ID
     * @since 24Q1 T3S1-5.17.0
     */
    private List<String> queryCustomizedSettingInstanceIds(
            @NonNull String projectId,
            @NonNull Long templateId,
            @NonNull Long lightingSceneId,
            @Nullable Collection<String> instanceIds) {
        DemandConfigLightingQueryDaoParam queryDaoParam = new DemandConfigLightingQueryDaoParam();
        queryDaoParam.setDemandInstanceIds(instanceIds);
        queryDaoParam.setIsCustomizedSetting(true);
        queryDaoParam.setRelDemandTemplateId(templateId);
        queryDaoParam.setRelDemandLightingSceneId(lightingSceneId);
        List<DemandConfigLighting> customizedSettingConfigs =
                demandConfigLightingService.list(projectId, queryDaoParam);
        if (CollectionUtils.isEmpty(customizedSettingConfigs)) {
            return Lists.newArrayList();
        }
        return customizedSettingConfigs.stream()
                .map(DemandConfigLighting::getDemandInstanceId)
                .collect(Collectors.toList());
    }

    private DemandConfigLightingResultQueryDaoParam getDemandConfigLightingResultQueryDaoParam(CircuitObjCfgStatisticsQueryParam param, List<DtpRwdSpaceDTO> spaces) {
        // 获取回路的配置状态
        DemandConfigLightingResultQueryDaoParam qp = new DemandConfigLightingResultQueryDaoParam();
        if (CollectionUtils.isNotEmpty(spaces)) {
            // ids
            List<String> ids = new ArrayList<>();
            for (DtpRwdSpaceDTO i : spaces) {
                if (i.getRelObjIds() != null) {
                    ids.addAll(i.getRelObjIds());
                }
            }
            ids = ids.stream().distinct().collect(Collectors.toList());

            qp.setCircuitIds(ids);
        }
        qp.setRelDemandTemplateId(param.getTemplateId());
        qp.setRelDemandLightingSceneId(param.getSceneId());
        return qp;
    }

    @Override
    public String logTag() {
        return "照明需求查询服务";
    }
}
