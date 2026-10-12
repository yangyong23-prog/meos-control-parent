package com.persagy.apccontrol.basic.service.demand.lighting;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.*;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdTreeLightingObjectDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitLampFuncTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandLightingFromClearParam;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.*;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingClearRangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 照明需求数据处理
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/3/13 16:11
 */
@Component
public class DemandConfigLightingHandler {

    @Autowired
    private DemandTemplateService demandTemplateService;

    @Autowired
    private DemandLightingStrategy demandLightingStrategy;

    @Autowired
    private DtpKnowledgeClientService dtpKnowledgeClientService;

    /**
     * 获取空间功能类型编码对应的名称
     *
     * @param queryCache
     * @param spaceFuncType
     * @return
     */
    public String getSpaceFuncTypeName(boolean queryCache, String spaceFuncType) {
        Map<String, List<DtpKnowledgeInfoCodeDataSourceDTO>> listMap = this.dtpKnowledgeClientService.mapInfoCodeAndDataSources(queryCache, false, DtpRwdClassCodeEnum.SPACE.value());
        List<DtpKnowledgeInfoCodeDataSourceDTO> infoCdeDataSourceDTOS = listMap == null ? null : listMap.get(InfoCodeEnum.ROOM_FUNC_TYPE.getCode());
        if (CollectionUtil.isNotEmpty(infoCdeDataSourceDTOS)) {
            for (DtpKnowledgeInfoCodeDataSourceDTO infoCdeDataSourceDTO : infoCdeDataSourceDTOS) {
                if (infoCdeDataSourceDTO.getCode().equals(spaceFuncType)) {
                    return infoCdeDataSourceDTO.getName();
                }
            }
        }
        return null;
    }

    /**
     * 查询当前类型下最新的照明回路及其对应的空间功能类型、回路功能类型、回路灯具类型分组情况
     *
     * @param projectId
     * @param templateId
     * @param templateType
     * @param range        -1-所有的，0-照明回路功能类型树，1-空间功能类型树
     * @return
     */
    public DemandLightingTypeGroupTree getDemandLightingTypeGroupTree(String projectId, Long templateId, DemandTemplateTypeEnum templateType,
                                                                      String instanceId, DemandInstanceTypeEnum instanceType, int range) {
        // 1.获取照明需求类型
        if (templateType == null) {
            DemandTemplate demandTemplate = this.demandTemplateService.getById(templateId);
            templateType = demandTemplate == null ? null : demandTemplate.getDemandTemplateType();
        }
        if (templateType == null) {
            return new DemandLightingTypeGroupTree();
        }

        // 2. 如果存在实例ID，说明需要进行数据过滤，过滤掉与此实例无关的各种类型数据
        List<DtpRwdCircuitDTO> filterCircuitDTOS = null;
        if (StrUtil.isNotBlank(instanceId) && instanceType != null && !DemandInstanceTypeEnum.PROJECT.equals(instanceType)) {
            filterCircuitDTOS = this.demandLightingStrategy.listCircuitsByInstance(projectId, templateType, instanceType, instanceId);
            if (CollectionUtil.isEmpty(filterCircuitDTOS)) {
                // 当前实例下没有回路，直接返回
                return new DemandLightingTypeGroupTree();
            }
        }
        Set<String> filterCircuitIds = filterCircuitDTOS == null ? null : filterCircuitDTOS.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());

        // 3.根据照明需求类型，获取对应的照明回路与其对应的空间功能类型
        List<DtpRwdCircuitDTO> circuitDTOS = this.demandLightingStrategy.listCircuits(projectId, templateType, filterCircuitIds);
        // id 集合
        Set<String> circuitIds = circuitDTOS.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());
        Map<String, List<String>> circuitId_spaceFuncTypes = this.demandLightingStrategy.mapCircuitIdAndSpaceFuncTypes(projectId, templateType, circuitIds);
        if (CollectionUtil.isEmpty(circuitDTOS)) {
            return new DemandLightingTypeGroupTree();
        }

        // 4.按照空间功能类型、照明功能类型、灯具类型，对照明回路进行分组

        /** key: spaceFuncType, funcType, lampFuncType, value: circuits */
        Map<String, Map<String, Map<String, List<DtpRwdCircuitDTO>>>> space_func_lamp_map_circuits = new HashMap<>();
        /** key: funcType, lampFuncType, value: circuits */
        Map<String, Map<String, List<DtpRwdCircuitDTO>>> func_lamp_map_circuits = new HashMap<>();

        for (DtpRwdCircuitDTO circuitDTO : circuitDTOS) {
            String funcType = circuitDTO.getFuncType();
            String lampFuncType = circuitDTO.getLampFuncType();
            if (filterCircuitIds != null && !filterCircuitIds.contains(circuitDTO.getId())) {
                continue;
            }
            if (StrUtil.isBlank(funcType)) {
                continue;
            }

            List<String> spaceFuncTypes = circuitId_spaceFuncTypes == null ? null : circuitId_spaceFuncTypes.get(circuitDTO.getId());
            if (CollectionUtil.isNotEmpty(spaceFuncTypes)) {
                for (String spaceFuncType : spaceFuncTypes) {
                    if (!space_func_lamp_map_circuits.containsKey(spaceFuncType)) {
                        space_func_lamp_map_circuits.put(spaceFuncType, new HashMap<>());
                    }
                    if (!space_func_lamp_map_circuits.get(spaceFuncType).containsKey(funcType)) {
                        space_func_lamp_map_circuits.get(spaceFuncType).put(funcType, new HashMap<>());
                    }
                    if (StringUtils.isNotBlank(lampFuncType)) {
                        space_func_lamp_map_circuits.get(spaceFuncType).get(funcType).computeIfAbsent(lampFuncType, e -> Lists.newArrayList()).add(circuitDTO);
                    }
                }
            }

            if (!func_lamp_map_circuits.containsKey(funcType)) {
                func_lamp_map_circuits.put(funcType, new HashMap<>());
            }
            if (StringUtils.isNotBlank(lampFuncType)) {
                func_lamp_map_circuits.get(funcType).computeIfAbsent(lampFuncType, e -> Lists.newArrayList()).add(circuitDTO);
            }
        }

        // 5.根据分组后的 map 数据，形成树形结构
        List<DemandLightingFuncTypeSettingVO> funcTypeSettingVOS = range == 0 || range == -1 ? this.funcLampMapTransferTree(func_lamp_map_circuits) : Lists.newArrayList();
        List<DemandLightingSpaceFuncTypeVO> spaceFuncTypeGroupVOS = range == 1 || range == -1 ? this.spaceFuncLampMapTransferTree(space_func_lamp_map_circuits) : Lists.newArrayList();
        DemandLightingTypeGroupTree result = new DemandLightingTypeGroupTree();
        result.setFuncTypeSettings(funcTypeSettingVOS);
        result.setSpaceFuncTypeSettings(spaceFuncTypeGroupVOS);
        return result;
    }

    /**
     * 清除配置
     *
     * @param groupCode
     * @param param
     */
    public Set<String> getClearDemandConfigLightingInstanceIds(String groupCode, DemandLightingFromClearParam param, DemandTemplateTypeEnum templateType) {
        DemandLightingClearRangeTypeEnum clearRange = param.getClearRange();

        Set<String> clearInstanceIds = new HashSet<>();

        // 2. 根据清楚范围与当前的实例ID，汇总需要清除配置的对象ID集合
        if (DemandLightingClearRangeTypeEnum.CURRENT.equals(clearRange)) {
            clearInstanceIds.add(param.getInstanceId());
        } else if (DemandLightingClearRangeTypeEnum.NEXT.equals(clearRange)) {
            // 获取下级所有需要清除的对象ID集合
            Set<String> childrenObjectIds = this.getChildrenObjectIds(groupCode, param.getProjectId(), templateType, param.getInstanceId());
            clearInstanceIds.addAll(childrenObjectIds);

        } else if (DemandLightingClearRangeTypeEnum.ALL.equals(clearRange)) {
            if (!DemandInstanceTypeEnum.PROJECT.equals(param.getInstanceType())) {
                clearInstanceIds.add(param.getInstanceId());

                // 获取下级所有需要清除的对象ID集合
                Set<String> childrenObjectIds = this.getChildrenObjectIds(groupCode, param.getProjectId(), templateType, param.getInstanceId());
                clearInstanceIds.addAll(childrenObjectIds);
            }
        }

        return clearInstanceIds;
    }

    /**
     * 处理实际的回路配置与当前中台交付的回路数据
     *
     * @param rwdFuncTypeSettings 中台交付的全量数据
     * @param circuitResults      当前需要进行处理本方配置数据
     * @param sceneNameMap        场景名称映射
     * @param templateType        当前照明类型
     * @return
     */
    public DemandLightingTemplateTypeVO lightingFuncTypeTree(List<DemandLightingFuncTypeSettingVO> rwdFuncTypeSettings, List<DemandConfigLightingResult> circuitResults,
                                                             TreeMap<Long, String> sceneNameMap, DemandTemplateTypeEnum templateType) {
        DemandLightingTemplateTypeVO templateTypeVO = new DemandLightingTemplateTypeVO();
        templateTypeVO.setTemplateType(templateType);
        templateTypeVO.setFuncTypeSettings(rwdFuncTypeSettings);

        Map<String, List<DemandConfigLightingResult>> circuit_scenes = CollectionUtil.isEmpty(circuitResults) ? new HashMap<>()
                : circuitResults.stream().collect(Collectors.groupingBy(DemandConfigLightingResult::getCircuitId));

        // 解析当前交付的数据，在本方的配置情况
        Map<String, List<DemandLightingSceneModel>> func_type_scene_modes = new HashMap<>();
        DemandLightingCircuitSummaryVO totalSummary = new DemandLightingCircuitSummaryVO(0, 0, 0, 0, 0);
        for (DemandLightingFuncTypeSettingVO rwdFuncTypeSetting : rwdFuncTypeSettings) {
            Map<String, List<DemandLightingSceneModel>> lam_type_scene_modes = new HashMap<>();
            DemandLightingCircuitSummaryVO funcTypeSummary = new DemandLightingCircuitSummaryVO(0, 0, 0, 0, 0);

            // 合并处理灯具类型配置数据
            List<DemandLightingLampFuncTypeSettingVO> rwdLampFuncTypeSettings = rwdFuncTypeSetting.getLampFuncTypeSettings();
            for (DemandLightingLampFuncTypeSettingVO rwdLampFuncTypeSetting : rwdLampFuncTypeSettings) {
                DemandLightingCircuitSummaryVO summary = new DemandLightingCircuitSummaryVO(0, 0, 0, 0, 0);
                List<DemandLightingSceneModel> sceneModels = this.mergeCircuitSceneConfigs(rwdLampFuncTypeSetting.getCircuitSettings(), summary, circuit_scenes, sceneNameMap);
                rwdLampFuncTypeSetting.setSceneModes(sceneModels);
                rwdLampFuncTypeSetting.setSummary(summary);
                rwdLampFuncTypeSetting.setCircuitSettings(null);
                DtpRwdCircuitLampFuncTypeEnum lampFuncType = rwdLampFuncTypeSetting.getLampFuncType();
                lam_type_scene_modes.put(null == lampFuncType ? null : lampFuncType.getValue(), sceneModels);

                funcTypeSummary.setTotal(funcTypeSummary.getTotal() + summary.getTotal());
                funcTypeSummary.setOpenCount(funcTypeSummary.getOpenCount() + summary.getOpenCount());
                funcTypeSummary.setCloseCount(funcTypeSummary.getCloseCount() + summary.getCloseCount());
                funcTypeSummary.setFeedbackAdjustmentCount(funcTypeSummary.getFeedbackAdjustmentCount() + summary.getFeedbackAdjustmentCount());
                funcTypeSummary.setUnConfigCount(funcTypeSummary.getUnConfigCount() + summary.getUnConfigCount());
            }

            // 合并处理功能类型配置数据
            List<DemandLightingSceneModel> sceneModels = this.getParentSceneModes(lam_type_scene_modes);
            rwdFuncTypeSetting.setSceneModes(sceneModels);
            rwdFuncTypeSetting.setSummary(funcTypeSummary);
            rwdFuncTypeSetting.setCircuitSettings(null);
            func_type_scene_modes.put(rwdFuncTypeSetting.getFuncType().getValue(), sceneModels);

            totalSummary.setTotal(totalSummary.getTotal() + funcTypeSummary.getTotal());
            totalSummary.setOpenCount(totalSummary.getOpenCount() + funcTypeSummary.getOpenCount());
            totalSummary.setCloseCount(totalSummary.getCloseCount() + funcTypeSummary.getCloseCount());
            totalSummary.setFeedbackAdjustmentCount(totalSummary.getFeedbackAdjustmentCount() + funcTypeSummary.getFeedbackAdjustmentCount());
            totalSummary.setUnConfigCount(totalSummary.getUnConfigCount() + funcTypeSummary.getUnConfigCount());
        }

        // 最后，处理一次当前照明类型下的所有配置数据
        List<DemandLightingSceneModel> sceneModels = this.getParentSceneModes(func_type_scene_modes);
        templateTypeVO.setSceneModes(sceneModels);
        templateTypeVO.setSummary(totalSummary);
        return templateTypeVO;
    }

    /**
     * 根据条件，提取出 funcTypeSettings 所需要的回路ID
     *
     * @param funcTypeSettings
     * @param circuitResults
     * @param sceneNameMap
     * @param settingMode
     * @param funcType
     * @param lampFuncType
     * @return
     */
    public DemandLightingCircuitView queryLightingCircuitListByType(List<DemandLightingFuncTypeSettingVO> funcTypeSettings, List<DemandConfigLightingResult> circuitResults,
                                                                    TreeMap<Long, String> sceneNameMap, DemandLightingCircuitSettingModeEnum settingMode, DtpRwdCircuitFuncTypeEnum funcType, DtpRwdCircuitLampFuncTypeEnum lampFuncType) {
        List<DemandLightingCircuitSettingVO> circuitSettings = Lists.newArrayList();
        DemandLightingCircuitSummaryVO summary = new DemandLightingCircuitSummaryVO(0, 0, 0, 0, 0);
        DemandLightingCircuitView circuitView = new DemandLightingCircuitView(summary, circuitSettings);

        // 1.获取所有需要进行处理的回路ID
        Map<String, DemandLightingCircuitSettingVO> circuit_setting = this.getLightingCircuitIdsByType(funcTypeSettings, funcType, lampFuncType);
        if (CollectionUtil.isEmpty(circuit_setting)) {
            return circuitView;
        }

        Map<String, List<DemandConfigLightingResult>> circuit_scenes = CollectionUtil.isEmpty(circuitResults) ? new HashMap<>()
                : circuitResults.stream().collect(Collectors.groupingBy(DemandConfigLightingResult::getCircuitId));

        // 2根据回路ID集合，返回对应的列表数据
        int openCount = 0, closeCount = 0, feedbackAdjustmentCount = 0, unConfigCount = 0;
        for (Map.Entry<String, DemandLightingCircuitSettingVO> circuitSettingVOEntry : circuit_setting.entrySet()) {
            String circuitId = circuitSettingVOEntry.getKey();
            DemandLightingCircuitSettingVO circuitSetting = circuitSettingVOEntry.getValue();
            List<DemandConfigLightingResult> sceneSettings = circuit_scenes.get(circuitId);

            // 判断此回路在每个场景下的状态，以及回路的最终状态
            List<DemandLightingSceneModel> sceneModels = new ArrayList<>();
            Pair<DemandLightingCircuitSettingModeEnum[], DemandLightingCircuitFeedbackAdjustSettingDTO> finalSettingMode =
                    this.getCircuitModeBySceneSettings(sceneSettings, null, null, sceneNameMap, sceneModels);
            if (settingMode != null && !settingMode.equals(finalSettingMode.getLeft()[1])) {
                continue;
            }

            if (DemandLightingCircuitSettingModeEnum.OPEN.equals(finalSettingMode.getLeft()[0])) {
                openCount++;
            } else if (DemandLightingCircuitSettingModeEnum.CLOSE.equals(finalSettingMode.getLeft()[0])) {
                closeCount++;
            } else if (DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT.equals(finalSettingMode.getLeft()[0])) {
                feedbackAdjustmentCount++;
            } else {
                unConfigCount++;
            }
            circuitSetting.setSceneModes(sceneModels);
            circuitSetting.setSettingMode(finalSettingMode.getLeft()[0]);
            circuitSetting.setFeedbackAdjustSetting(finalSettingMode.getRight());
            circuitSettings.add(circuitSetting);
        }
        summary.setTotal(openCount + closeCount + feedbackAdjustmentCount + unConfigCount);
        summary.setOpenCount(openCount);
        summary.setCloseCount(closeCount);
        summary.setFeedbackAdjustmentCount(feedbackAdjustmentCount);
        summary.setUnConfigCount(unConfigCount);

        return circuitView;
    }

    /**
     * 获取空间功能类型树
     *
     * @param func_lamp_map_circuits
     * @return
     */
    private List<DemandLightingFuncTypeSettingVO> funcLampMapTransferTree(Map<String, Map<String, List<DtpRwdCircuitDTO>>> func_lamp_map_circuits) {
        List<DemandLightingFuncTypeSettingVO> funcTypeSettingVOS = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<DtpRwdCircuitDTO>>> mapEntry : func_lamp_map_circuits.entrySet()) {
            String funcType = mapEntry.getKey();
            Map<String, List<DtpRwdCircuitDTO>> lampMap = mapEntry.getValue();
            if (CollectionUtil.isEmpty(lampMap)) {
                continue;
            }

            // 解析所有灯具类型下的配置
            List<DemandLightingCircuitSettingVO> allLampCircuitSettingVOS = new ArrayList<>();
            List<DemandLightingLampFuncTypeSettingVO> allLampFuncTypeSettingVOS = new ArrayList<>();
            for (Map.Entry<String, List<DtpRwdCircuitDTO>> listEntry : lampMap.entrySet()) {
                String lampFuncType = listEntry.getKey();
                List<DtpRwdCircuitDTO> rwdCircuitDTOS = listEntry.getValue();

                Set<String> tempIds = new HashSet<>();
                List<DemandLightingCircuitSettingVO> lampCircuitSettingVOS = new ArrayList<>();
                if (CollectionUtil.isNotEmpty(rwdCircuitDTOS)) {
                    for (DtpRwdCircuitDTO rwdCircuitDTO : rwdCircuitDTOS) {
                        if (tempIds.contains(rwdCircuitDTO.getId())) {
                            continue;
                        }
                        DemandLightingCircuitSettingVO circuitSettingVO = new DemandLightingCircuitSettingVO();
                        BeanUtil.copyProperties(rwdCircuitDTO, circuitSettingVO);
                        circuitSettingVO.setCircuitId(rwdCircuitDTO.getId());
                        circuitSettingVO.setCircuitLocalId(rwdCircuitDTO.getLocalId());
                        circuitSettingVO.setCircuitLocalName(rwdCircuitDTO.getLocalName());
                        tempIds.add(rwdCircuitDTO.getId());
                        lampCircuitSettingVOS.add(circuitSettingVO);
                        allLampCircuitSettingVOS.add(circuitSettingVO);
                    }
                }

                // 设置灯具类型的数据
                if (CollectionUtil.isNotEmpty(lampCircuitSettingVOS)) {
                    DemandLightingLampFuncTypeSettingVO lampFuncTypeSettingVO = new DemandLightingLampFuncTypeSettingVO();
                    lampFuncTypeSettingVO.setLampFuncType(DtpRwdCircuitLampFuncTypeEnum.analyze(lampFuncType));
                    lampFuncTypeSettingVO.setCircuitSettings(lampCircuitSettingVOS);
                    allLampFuncTypeSettingVOS.add(lampFuncTypeSettingVO);
                }
            }

            // 设置回路功能类型的数据
            if (CollectionUtil.isNotEmpty(allLampFuncTypeSettingVOS)) {
                DemandLightingFuncTypeSettingVO funcTypeSettingVO = new DemandLightingFuncTypeSettingVO();
                funcTypeSettingVO.setFuncType(DtpRwdCircuitFuncTypeEnum.analyze(funcType));
                funcTypeSettingVO.setCircuitSettings(allLampCircuitSettingVOS);
                funcTypeSettingVO.setLampFuncTypeSettings(allLampFuncTypeSettingVOS);
                funcTypeSettingVOS.add(funcTypeSettingVO);
            }
        }

        return funcTypeSettingVOS;
    }

    /**
     * 获取空间功能类型树
     *
     * @param space_func_lamp_map_circuits
     * @return
     */
    private List<DemandLightingSpaceFuncTypeVO> spaceFuncLampMapTransferTree(Map<String, Map<String, Map<String, List<DtpRwdCircuitDTO>>>> space_func_lamp_map_circuits) {
        List<DemandLightingSpaceFuncTypeVO> spaceFuncTypeGroupVOS = new ArrayList<>();
        for (Map.Entry<String, Map<String, Map<String, List<DtpRwdCircuitDTO>>>> stringMapEntry : space_func_lamp_map_circuits.entrySet()) {
            String spaceFuncType = stringMapEntry.getKey();
            Map<String, Map<String, List<DtpRwdCircuitDTO>>> next = stringMapEntry.getValue();
            if (CollectionUtil.isEmpty(next)) {
                continue;
            }

            // 解析所有回路功能类型下的配置
            List<DemandLightingCircuitSettingVO> allFuncCircuitSettingVOS = new ArrayList<>();
            List<DemandLightingFuncTypeSettingVO> allFuncTypeSettingVOS = new ArrayList<>();
            for (Map.Entry<String, Map<String, List<DtpRwdCircuitDTO>>> mapEntry : next.entrySet()) {
                String funcType = mapEntry.getKey();
                Map<String, List<DtpRwdCircuitDTO>> lampMap = mapEntry.getValue();
                if (CollectionUtil.isEmpty(lampMap)) {
                    continue;
                }

                // 解析所有灯具类型下的配置
                List<DemandLightingCircuitSettingVO> allLampCircuitSettingVOS = new ArrayList<>();
                List<DemandLightingLampFuncTypeSettingVO> allLampFuncTypeSettingVOS = new ArrayList<>();
                for (Map.Entry<String, List<DtpRwdCircuitDTO>> listEntry : lampMap.entrySet()) {
                    String lampFuncType = listEntry.getKey();
                    List<DtpRwdCircuitDTO> rwdCircuitDTOS = listEntry.getValue();

                    Set<String> tempIds = new HashSet<>();
                    List<DemandLightingCircuitSettingVO> lampCircuitSettingVOS = new ArrayList<>();
                    if (CollectionUtil.isNotEmpty(rwdCircuitDTOS)) {
                        for (DtpRwdCircuitDTO rwdCircuitDTO : rwdCircuitDTOS) {
                            if (tempIds.contains(rwdCircuitDTO.getId())) {
                                continue;
                            }
                            DemandLightingCircuitSettingVO circuitSettingVO = new DemandLightingCircuitSettingVO();
                            BeanUtil.copyProperties(rwdCircuitDTO, circuitSettingVO);
                            circuitSettingVO.setCircuitId(rwdCircuitDTO.getId());
                            circuitSettingVO.setCircuitLocalId(rwdCircuitDTO.getLocalId());
                            circuitSettingVO.setCircuitLocalName(rwdCircuitDTO.getLocalName());

                            tempIds.add(rwdCircuitDTO.getId());
                            lampCircuitSettingVOS.add(circuitSettingVO);
                            allLampCircuitSettingVOS.add(circuitSettingVO);
                            allFuncCircuitSettingVOS.add(circuitSettingVO);
                        }
                    }

                    // 设置灯具类型的数据
                    if (CollectionUtil.isNotEmpty(lampCircuitSettingVOS)) {
                        DemandLightingLampFuncTypeSettingVO lampFuncTypeSettingVO = new DemandLightingLampFuncTypeSettingVO();
                        lampFuncTypeSettingVO.setLampFuncType(DtpRwdCircuitLampFuncTypeEnum.analyze(lampFuncType));
                        lampFuncTypeSettingVO.setCircuitSettings(lampCircuitSettingVOS);
                        allLampFuncTypeSettingVOS.add(lampFuncTypeSettingVO);
                    }
                }

                // 设置回路功能类型的数据
                if (CollectionUtil.isNotEmpty(allLampFuncTypeSettingVOS)) {
                    DemandLightingFuncTypeSettingVO funcTypeSettingVO = new DemandLightingFuncTypeSettingVO();
                    funcTypeSettingVO.setFuncType(DtpRwdCircuitFuncTypeEnum.analyze(funcType));
                    funcTypeSettingVO.setCircuitSettings(allLampCircuitSettingVOS);
                    funcTypeSettingVO.setLampFuncTypeSettings(allLampFuncTypeSettingVOS);
                    allFuncTypeSettingVOS.add(funcTypeSettingVO);
                }
            }

            // 设备空间功能类型的数据
            if (CollectionUtil.isNotEmpty(allFuncTypeSettingVOS)) {
                DemandLightingSpaceFuncTypeVO spaceFuncTypeGroupVO = new DemandLightingSpaceFuncTypeVO();
                spaceFuncTypeGroupVO.setSpaceFuncType(spaceFuncType);
                spaceFuncTypeGroupVO.setSpaceFuncTypeName(this.getSpaceFuncTypeName(true, spaceFuncType));
                spaceFuncTypeGroupVO.setCircuitSettings(allFuncCircuitSettingVOS);
                spaceFuncTypeGroupVO.setCircuitFuncTypes(allFuncTypeSettingVOS);
                spaceFuncTypeGroupVOS.add(spaceFuncTypeGroupVO);
            }
        }

        return spaceFuncTypeGroupVOS;
    }

    /**
     * 根据实例ID，获取此实例下的所有子级对象ID
     *
     * @param groupCode
     * @param projectId
     * @param templateType
     * @param instanceId
     * @return
     */
    private Set<String> getChildrenObjectIds(String groupCode, String projectId, DemandTemplateTypeEnum templateType, String instanceId) {
        // 1. 获取建筑楼层空间树
        List<DtpRwdTreeLightingObjectDTO> rwdObjLst = demandLightingStrategy.queryBuildingFloorSpaceObject(projectId, templateType);
        if (CollectionUtil.isEmpty(rwdObjLst)) {
            return Sets.newHashSet();
        }

        // 2.汇总所有下级配置
        Map<String, Set<String>> parent_nextIds = new HashMap<>();

        Set<String> allBuildFloorSpaceIds = new HashSet<>();
        for (DtpRwdTreeLightingObjectDTO building : rwdObjLst) {
            String buildingId = building.getId();
            List<DtpRwdTreeLightingObjectDTO> floorList = building.getChildrenList();
            if (CollectionUtil.isEmpty(floorList)) {
                continue;
            }
            Set<String> allFloorSpaceIds = new HashSet<>();
            for (DtpRwdTreeLightingObjectDTO floor : floorList) {
                String floorId = floor.getId();
                List<DtpRwdTreeLightingObjectDTO> spaceList = floor.getChildrenList();
                if (CollectionUtil.isEmpty(spaceList)) {
                    continue;
                }
                Set<String> allSpaceIds = new HashSet<>();
                for (DtpRwdTreeLightingObjectDTO space : spaceList) {
                    allSpaceIds.add(space.getId());
                    parent_nextIds.put(space.getId(), Sets.newHashSet(space.getId()));
                }
                allFloorSpaceIds.add(floorId);
                allFloorSpaceIds.addAll(allSpaceIds);
                parent_nextIds.put(floorId, allSpaceIds);
            }
            allBuildFloorSpaceIds.add(buildingId);
            allBuildFloorSpaceIds.addAll(allFloorSpaceIds);
            parent_nextIds.put(buildingId, allFloorSpaceIds);
        }
        parent_nextIds.put(projectId, allBuildFloorSpaceIds);

        return parent_nextIds.get(instanceId) == null ? Sets.newHashSet() : parent_nextIds.get(instanceId);
    }

    private List<DemandLightingSceneModel> mergeCircuitSceneConfigs(List<DemandLightingCircuitSettingVO> rwdCircuitSettings, DemandLightingCircuitSummaryVO summary,
                                                                    Map<String, List<DemandConfigLightingResult>> circuit_scenes, TreeMap<Long, String> sceneNameMap) {
        if (CollectionUtil.isEmpty(rwdCircuitSettings) || CollectionUtil.isEmpty(circuit_scenes)) {
            summary.setTotal(rwdCircuitSettings == null ? 0 : rwdCircuitSettings.size());
            summary.setUnConfigCount(rwdCircuitSettings == null ? 0 : rwdCircuitSettings.size());
            return Lists.newArrayList();
        }

        // 如下两个map，仅统计本方实际配置的，并非全量
        Map<Long, Set<String>> scene_circuits = new HashMap<>();
        Map<Long, Map<String, DemandLightingCircuitSettingModeEnum>> scene_circuit_model = new HashMap<>();

        // 根据本方已有的配置数据，判断中台交付数据实际的状态
        int openCount = 0, closeCount = 0, feedbackAdjustmentCount = 0, unConfigCount = 0;
        for (DemandLightingCircuitSettingVO rwdCircuitSetting : rwdCircuitSettings) {
            // 获取此回路在本方的配置数据
            List<DemandConfigLightingResult> sceneSettings = circuit_scenes.get(rwdCircuitSetting.getCircuitId());

            // 判断此回路在每个场景下的状态，以及回路的最终状态
            Pair<DemandLightingCircuitSettingModeEnum[], DemandLightingCircuitFeedbackAdjustSettingDTO> finalSettingMode = this.getCircuitModeBySceneSettings(sceneSettings, scene_circuits, scene_circuit_model, sceneNameMap, null);
            if (DemandLightingCircuitSettingModeEnum.OPEN.equals(finalSettingMode.getLeft()[0])) {
                openCount++;
            } else if (DemandLightingCircuitSettingModeEnum.CLOSE.equals(finalSettingMode.getLeft()[0])) {
                closeCount++;
            } else if (DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT.equals(finalSettingMode.getLeft()[0])) {
                feedbackAdjustmentCount++;
            } else {
                unConfigCount++;
            }
            rwdCircuitSetting.setSettingMode(finalSettingMode.getLeft()[0]);
            rwdCircuitSetting.setFeedbackAdjustSetting(finalSettingMode.getRight());
        }
        summary.setTotal(openCount + closeCount + feedbackAdjustmentCount + unConfigCount);
        summary.setOpenCount(openCount);
        summary.setCloseCount(closeCount);
        summary.setFeedbackAdjustmentCount(feedbackAdjustmentCount);
        summary.setUnConfigCount(unConfigCount);

        // 根据每个回路的开关状态，以及对应的场景，判断当前场景的状态
        return this.getSceneModesByCircuitMode(scene_circuit_model, sceneNameMap, rwdCircuitSettings.size());
    }

    /**
     * 获取单回路下，在不同场景下的状态，以及得出最终的回路状态
     *
     * @param sceneSettings       单回路下所有的场景实际配置
     * @param scene_circuits
     * @param scene_circuit_model
     * @param sceneModes;
     * @return 0-回路最终状态，1-如果存在未配置，就是未配置状态，否则与最终状态保持一致
     */
    private Pair<DemandLightingCircuitSettingModeEnum[], DemandLightingCircuitFeedbackAdjustSettingDTO> getCircuitModeBySceneSettings(
            List<DemandConfigLightingResult> sceneSettings, Map<Long, Set<String>> scene_circuits,
            Map<Long, Map<String, DemandLightingCircuitSettingModeEnum>> scene_circuit_model,
            TreeMap<Long, String> sceneNameMap, List<DemandLightingSceneModel> sceneModes) {
        DemandLightingCircuitSettingModeEnum[] result = new DemandLightingCircuitSettingModeEnum[]{DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED, DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED};
        DemandLightingCircuitSettingModeEnum finalSettingMode = DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED;
        DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting = null;
        if (CollectionUtil.isEmpty(sceneSettings)) {
            return Pair.of(result, feedbackAdjustSetting);
        }
        Map<Long, DemandLightingSceneModel> scene_models = new HashMap<>();

        for (DemandConfigLightingResult sceneSetting : sceneSettings) {
            Long sceneId = sceneSetting.getRelDemandLightingSceneId();
            DemandLightingCircuitSettingModeEnum settingMode = sceneSetting.getConfigData() == null ? null : sceneSetting.getConfigData().getCircuitSettingMode();
            if (!DemandLightingCircuitSettingModeEnum.OPEN.equals(finalSettingMode)
                    && !DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT.equals(finalSettingMode)) {
                if (DemandLightingCircuitSettingModeEnum.OPEN.equals(settingMode)) {
                    finalSettingMode = DemandLightingCircuitSettingModeEnum.OPEN;
                } else if (DemandLightingCircuitSettingModeEnum.CLOSE.equals(settingMode)) {
                    finalSettingMode = DemandLightingCircuitSettingModeEnum.CLOSE;
                } else if (DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT.equals(settingMode)) {
                    finalSettingMode = DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT;
                    feedbackAdjustSetting = sceneSetting.getConfigData().getFeedbackAdjustSettings();
                }
            }
            scene_models.put(sceneId, new DemandLightingSceneModel(sceneId, sceneNameMap.get(sceneId), null, settingMode,
                    settingMode == DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT
                            ? sceneSetting.getConfigData().getFeedbackAdjustSettings() : null));

            if (scene_circuits != null) {
                scene_circuits.computeIfAbsent(sceneId, v -> Sets.newHashSet()).add(sceneSetting.getCircuitId());
            }
            if (scene_circuit_model != null) {
                if (!scene_circuit_model.containsKey(sceneId)) {
                    scene_circuit_model.put(sceneId, new HashMap<>());
                }
                scene_circuit_model.get(sceneId).put(sceneSetting.getCircuitId(), settingMode);
            }
        }
        // 验证是否存在未配置的场景
        boolean existsNon = false;
        for (Long sceneId : sceneNameMap.keySet()) {
            DemandLightingSceneModel sceneModel = scene_models.get(sceneId);
            if (sceneModes != null) {
                sceneModes.add(sceneModel == null ? new DemandLightingSceneModel(sceneId, sceneNameMap.get(sceneId), null, DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED, null) : sceneModel);
            }
            if (sceneModel == null || DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED.equals(sceneModel.getSettingMode())) {
                existsNon = true;
            }
        }
        if (!existsNon) {
            result[1] = finalSettingMode;
        }
        result[0] = finalSettingMode;
        return Pair.of(result, feedbackAdjustSetting);
    }

    /**
     * 统计当前条件下，对应的场景状态
     *
     * @param scene_circuit_model
     * @param sceneNameMap        本方配置的所有场景信息
     * @param circuitNum          中台交付的回路总数
     * @return
     */
    private List<DemandLightingSceneModel> getSceneModesByCircuitMode(Map<Long, Map<String, DemandLightingCircuitSettingModeEnum>> scene_circuit_model,
                                                                      TreeMap<Long, String> sceneNameMap, int circuitNum) {
        List<DemandLightingSceneModel> sceneModels = new ArrayList<>();
        if (CollectionUtil.isEmpty(sceneNameMap)) {
            return sceneModels;
        }

        for (Map.Entry<Long, String> sceneEntry : sceneNameMap.entrySet()) {
            Long sceneId = sceneEntry.getKey();
            String sceneName = sceneEntry.getValue();
            Map<String, DemandLightingCircuitSettingModeEnum> settingModeEnumMap = scene_circuit_model.get(sceneId);
            if (CollectionUtil.isEmpty(settingModeEnumMap)) {
                sceneModels.add(new DemandLightingSceneModel(sceneId, sceneName, DemandLightingTypeSettingModeEnum.NOT_CONFIGURED, null, null));
                continue;
            }

            // 判断出，实际场景的配置
            int openCount = 0, closeCount = 0, feedbackAdjustmentCount = 0, unConfigCount = 0;
            for (Map.Entry<String, DemandLightingCircuitSettingModeEnum> settingModeEnumEntry : settingModeEnumMap.entrySet()) {
                DemandLightingCircuitSettingModeEnum settingMode = settingModeEnumEntry.getValue();
                if (DemandLightingCircuitSettingModeEnum.OPEN.equals(settingMode)) {
                    openCount++;
                } else if (DemandLightingCircuitSettingModeEnum.CLOSE.equals(settingMode)) {
                    closeCount++;
                } else if (DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT.equals(settingMode)) {
                    feedbackAdjustmentCount++;
                } else {
                    unConfigCount++;
                }
            }
            // 判断场景下照明回路是否为全开|全关|部分开启, 反馈调节认为是部分开启
            DemandLightingTypeSettingModeEnum sceneMode = openCount == circuitNum ? DemandLightingTypeSettingModeEnum.ALL_OPEN
                    : closeCount == circuitNum ? DemandLightingTypeSettingModeEnum.ALL_CLOSE
                    : unConfigCount == circuitNum ? DemandLightingTypeSettingModeEnum.NOT_CONFIGURED
                    : DemandLightingTypeSettingModeEnum.PARTIAL_OPEN;
            sceneModels.add(new DemandLightingSceneModel(sceneId, sceneName, sceneMode, null, null));
        }
        return sceneModels;
    }

    /**
     * 根据下级的场景模式信息，综合出父级的配置数据
     *
     * @param type_sceneModes
     */
    private List<DemandLightingSceneModel> getParentSceneModes(Map<String, List<DemandLightingSceneModel>> type_sceneModes) {
        if (CollectionUtil.isEmpty(type_sceneModes)) {
            return Lists.newArrayList();
        }
        TreeMap<Long, DemandLightingSceneModel> sceneModelMap = new TreeMap<>();
        for (List<DemandLightingSceneModel> sceneModels : type_sceneModes.values()) {
            if (CollectionUtil.isEmpty(sceneModels)) {
                continue;
            }
            for (DemandLightingSceneModel sceneModel : sceneModels) {
                Long sceneId = sceneModel.getSceneId();
                if (!sceneModelMap.containsKey(sceneId)) {
                    DemandLightingSceneModel parentMode = new DemandLightingSceneModel();
                    BeanUtil.copyProperties(sceneModel, parentMode);
                    sceneModelMap.put(sceneId, parentMode);
                    continue;
                }

                // 判断两者之间的模式
                DemandLightingSceneModel preSceneMode = sceneModelMap.get(sceneId);
                if (preSceneMode.getSceneMode().equals(sceneModel.getSceneMode())) {
                    continue;
                }
                preSceneMode.setSceneMode(DemandLightingTypeSettingModeEnum.PARTIAL_OPEN);
            }
        }
        return new ArrayList<>(sceneModelMap.values());
    }

    /**
     * 提取出所有满足条件的回路ID集合
     *
     * @param funcTypeSettings
     * @param funcType
     * @param lampFuncType
     */
    private Map<String, DemandLightingCircuitSettingVO> getLightingCircuitIdsByType(List<DemandLightingFuncTypeSettingVO> funcTypeSettings, DtpRwdCircuitFuncTypeEnum funcType, DtpRwdCircuitLampFuncTypeEnum lampFuncType) {
        Map<String, DemandLightingCircuitSettingVO> circuit_setting = new HashMap<>();
        for (DemandLightingFuncTypeSettingVO funcTypeSetting : funcTypeSettings) {
            List<DemandLightingLampFuncTypeSettingVO> lampFuncTypeSettings = funcTypeSetting.getLampFuncTypeSettings();
            for (DemandLightingLampFuncTypeSettingVO lampFuncTypeSetting : lampFuncTypeSettings) {
                List<DemandLightingCircuitSettingVO> circuitSettings = lampFuncTypeSetting.getCircuitSettings();
                for (DemandLightingCircuitSettingVO circuitSetting : circuitSettings) {
                    if (funcType != null && !funcType.equals(funcTypeSetting.getFuncType())) {
                        continue;
                    }
                    if (lampFuncType != null && !lampFuncType.equals(lampFuncTypeSetting.getLampFuncType())) {
                        continue;
                    }
                    circuit_setting.put(circuitSetting.getCircuitId(), circuitSetting);
                }
            }
        }
        return circuit_setting;
    }

    /**
     * 配置统计
     *
     * @param globalConfigVO 全局配置VO
     * @author yuecaipu
     * @since 24Q1 T3S2-5.18.0
     */
    public void configSummary(DemandConfigLightingGlobalConfigVO globalConfigVO) {
        List<DemandLightingFuncTypeSettingVO> settings = globalConfigVO.getFuncTypeSettings();
        if (CollUtil.isEmpty(settings)) {
            return;
        }
        DemandLightingCircuitSummaryVO globalSummary = new DemandLightingCircuitSummaryVO(0, 0, 0, 0, 0);
        globalConfigVO.setSummary(globalSummary);
        for (DemandLightingFuncTypeSettingVO setting : settings) {
            int total = setting.getCircuitIds().size();
            int openCount = DemandLightingTypeSettingModeEnum.ALL_OPEN.equals(setting.getSettingMode()) ? total : 0;
            int closeCount = DemandLightingTypeSettingModeEnum.ALL_CLOSE.equals(setting.getSettingMode()) ? total : 0;
            int unConfigCount = null == setting.getSettingMode() ? total : 0;

            DemandLightingCircuitSummaryVO funcTypeSummary = new DemandLightingCircuitSummaryVO(total, openCount, closeCount, null, unConfigCount);
            setting.setSummary(funcTypeSummary);

            // 部分开启 统计
            if (DemandLightingTypeSettingModeEnum.PARTIAL_OPEN.equals(setting.getSettingMode())) {
                List<DemandLightingCircuitSettingVO> circuitSettings = setting.getCircuitSettings();
                if (CollUtil.isEmpty(circuitSettings)) {
                    continue;
                }
                for (DemandLightingCircuitSettingVO circuitSetting : circuitSettings) {
                    if (DemandLightingCircuitSettingModeEnum.OPEN.equals(circuitSetting.getSettingMode())) {
                        funcTypeSummary.setOpenCount(funcTypeSummary.getOpenCount() + 1);
                    } else if (DemandLightingCircuitSettingModeEnum.CLOSE.equals(circuitSetting.getSettingMode())) {
                        funcTypeSummary.setCloseCount(funcTypeSummary.getCloseCount() + 1);
                    } else {
                        funcTypeSummary.setUnConfigCount(funcTypeSummary.getUnConfigCount() + 1);
                    }
                }
            }
            globalSummary.plus(funcTypeSummary);
        }
    }

    /**
     * 空间配置数据统计
     *
     * @param spaceFuncTypeSettings 功能类型配置
     * @author yuecaipu
     * @since 24Q1 T3S1-5.17.0
     */
    public void spaceConfigSummary(List<DemandLightingSpaceFuncTypeSettingVO> spaceFuncTypeSettings) {
        for (DemandLightingSpaceFuncTypeSettingVO spaceFuncTypeSetting : spaceFuncTypeSettings) {
            configSummary(spaceFuncTypeSetting);
        }
    }

    /**
     * 构建全局配置
     *
     * @param tree           功能类型配置
     * @param globalSettings 全局配置
     * @author yuecaipu
     * @since 24Q1 T3S2-5.18.0
     */
    public List<DemandLightingFuncTypeSettingVO> globalConfigBuilding(DemandLightingTypeGroupTree tree, List<DemandLightingFuncTypeSettingDTO> globalSettings) {
        Map<Long, Set<String>> groupIdAndCircuitIds = tree.getGroupIdAndCircuitIds();
        List<DemandLightingFuncTypeSettingVO> funcTypeSettings = tree.getFuncTypeSettings();
        Map<DtpRwdCircuitFuncTypeEnum, DemandLightingFuncTypeSettingVO> funcType2setting = funcTypeSettings.stream()
                .collect(Collectors.toMap(DemandLightingFuncTypeSettingVO::getFuncType, v -> v));
        for (DemandLightingFuncTypeSettingDTO funcTypeSettingDTO : globalSettings) {
            DtpRwdCircuitFuncTypeEnum funcType = funcTypeSettingDTO.getFuncType();
            if (!funcType2setting.containsKey(funcType)) {
                continue;
            }
            DemandLightingFuncTypeSettingVO demandLightingFuncTypeSettingVO = funcType2setting.get(funcType);
            globalConfigSetting(funcTypeSettingDTO, demandLightingFuncTypeSettingVO, groupIdAndCircuitIds);
        }
        return funcTypeSettings;
    }

    /**
     * 全局配置设置：配置状态根据实际配置赋值
     *
     * @param funcTypeSettingDTO              功能类型配置
     * @param demandLightingFuncTypeSettingVO 功能类型配置VO
     * @param groupIdAndCircuitIds            分组ID -> 分组下回路ID
     * @author yuecaipu
     * @since 24Q1 T3S2-5.18.0
     */
    private void globalConfigSetting(DemandLightingFuncTypeSettingDTO funcTypeSettingDTO,
                                     DemandLightingFuncTypeSettingVO demandLightingFuncTypeSettingVO,
                                     Map<Long, Set<String>> groupIdAndCircuitIds) {
        List<DemandLightingCircuitGroupSettingDTO> dbGroupSettings = funcTypeSettingDTO.getCircuitGroupSettings();
        // 功能类型设置
        DemandLightingTypeSettingModeEnum settingMode = funcTypeSettingDTO.getSettingMode();
        demandLightingFuncTypeSettingVO.setSettingMode(null == funcTypeSettingDTO.getSettingMode() ?
                DemandLightingTypeSettingModeEnum.NOT_CONFIGURED : funcTypeSettingDTO.getSettingMode());
        demandLightingFuncTypeSettingVO.setCircuitGroupSettings(dbGroupSettings);

        Map<String, DemandLightingCircuitSettingModeEnum> groupCircuitIdAndSettingMode = new HashMap<>();
        if (MapUtils.isNotEmpty(groupIdAndCircuitIds) && CollectionUtils.isNotEmpty(dbGroupSettings)) {
            for (DemandLightingCircuitGroupSettingDTO dbGroupSetting : dbGroupSettings) {
                Long groupId = dbGroupSetting.getGroupId();
                DemandLightingTypeSettingModeEnum groupSettingMode = dbGroupSetting.getSettingMode();
                Set<String> circuitIds = groupIdAndCircuitIds.get(groupId);
                if (CollectionUtils.isNotEmpty(circuitIds)) {
                    for (String circuitId : circuitIds) {
                        groupCircuitIdAndSettingMode.put(circuitId, groupSettingMode.toCircuitSettingMode());
                    }
                }
            }
        }

        List<DemandLightingCircuitSettingVO> circuitSettings = demandLightingFuncTypeSettingVO.getCircuitSettings();
        if (CollUtil.isNotEmpty(circuitSettings)) {
            // 已配置回路设置映射
            Map<String, DemandLightingCircuitSettingModeEnum> circuitId2setting = null;
            if (CollUtil.isNotEmpty(funcTypeSettingDTO.getCircuitSettings())) {
                circuitId2setting = funcTypeSettingDTO.getCircuitSettings().stream()
                        .filter(i -> null != i.getSettingMode())
                        .collect(Collectors.toMap(DemandLightingCircuitSettingDTO::getCircuitId, DemandLightingCircuitSettingDTO::getSettingMode));
            }
            // 回路设置
            for (DemandLightingCircuitSettingVO circuitSetting : circuitSettings) {
                String circuitId = circuitSetting.getCircuitId();
                if (MapUtils.isNotEmpty(groupCircuitIdAndSettingMode)) {
                    DemandLightingCircuitSettingModeEnum groupSettingMode = groupCircuitIdAndSettingMode.get(circuitId);
                    // 如果存在分组配置, 则强制写入分组的配置
                    circuitSetting.setSettingMode(Objects.nonNull(groupSettingMode)
                            ? groupSettingMode : DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED);
                    continue;
                }
                if (Objects.nonNull(circuitSetting.getSettingMode())) {
                    continue;
                }
                circuitSetting.setSettingMode(DemandLightingCircuitSettingModeEnum.getSettingMode(settingMode,
                        null == circuitId2setting ? null : circuitId2setting.get(circuitId)));
            }
        }
    }

    public List<DemandLightingSpaceFuncTypeSettingVO> spaceConfigBuildingAndSummary(DemandInstanceTypeEnum demandInstanceType,
                                                                                    DemandLightingTypeGroupTree tree,
                                                                                    List<DemandLightingSpaceFuncTypeSettingDTO> spaceFuncTypeSettingDTOList) {
        List<DemandLightingSpaceFuncTypeVO> spaceFuncTypeVOList = tree.getSpaceFuncTypeSettings();
        Map<Long, Set<String>> groupIdAndCircuitIds = tree.getGroupIdAndCircuitIds();
        List<DemandLightingSpaceFuncTypeSettingVO> spaceFuncTypeSettings = new ArrayList<>();
        if (CollUtil.isNotEmpty(spaceFuncTypeSettingDTOList)) {
            Map<String, DemandLightingSpaceFuncTypeVO> map = spaceFuncTypeVOList.stream().collect(Collectors.toMap(DemandLightingSpaceFuncTypeVO::getSpaceFuncType, o -> o));

            for (DemandLightingSpaceFuncTypeSettingDTO spaceFuncTypeSettingDTO : spaceFuncTypeSettingDTOList) {
                List<BaseEnumVO> spaceFuncTypes = spaceFuncTypeSettingDTO.getSpaceFuncTypes();
                //-----过滤本实例下没有的空间功能类型-----
                spaceFuncTypes = spaceFuncTypes.stream().filter(o -> map.containsKey(o.getKey().toString())).collect(Collectors.toList());
                if (CollUtil.isEmpty(spaceFuncTypes)) {
                    continue;
                }

                //-----根据过滤出的空间功能类型组装树-----
                List<DemandLightingFuncTypeSettingVO> funcTypeSettings = buildingFuncTypeSetting(demandInstanceType,
                        map, spaceFuncTypes, spaceFuncTypeSettingDTO.getFuncTypeSettings(), groupIdAndCircuitIds);
                if (CollUtil.isEmpty(funcTypeSettings)) {
                    continue;
                }
                DemandLightingSpaceFuncTypeSettingVO spaceFuncTypeSettingVO = BeanUtil.copyProperties(spaceFuncTypeSettingDTO, DemandLightingSpaceFuncTypeSettingVO.class);
                spaceFuncTypeSettingVO.setSpaceFuncTypes(spaceFuncTypes);
                spaceFuncTypeSettingVO.setFuncTypeSettings(funcTypeSettings);

                spaceFuncTypeSettings.add(spaceFuncTypeSettingVO);
            }

        }
        spaceConfigSummary(spaceFuncTypeSettings);
        return spaceFuncTypeSettings;
    }

    /**
     * 描述
     *
     * @param map                  最新的空间功能类型map key:空间功能类型编码 value：照明回路空间类型分组VO
     * @param spaceFuncTypes       已选中配置的空间功能类型
     * @param funcTypeSettings     已选中空间功能类型配置的功能类型配置
     * @param groupIdAndCircuitIds 分组ID -> 分组下回路ID
     * @return {@link List<DemandLightingFuncTypeSettingVO>}
     * @author yuecaipu
     * @since 24Q1 T3S2-5.18.0
     */
    private List<DemandLightingFuncTypeSettingVO> buildingFuncTypeSetting(DemandInstanceTypeEnum demandInstanceType,
                                                                          Map<String, DemandLightingSpaceFuncTypeVO> map,
                                                                          List<BaseEnumVO> spaceFuncTypes,
                                                                          List<DemandLightingFuncTypeSettingDTO> funcTypeSettings,
                                                                          Map<Long, Set<String>> groupIdAndCircuitIds) {

        Map<DtpRwdCircuitFuncTypeEnum, DemandLightingFuncTypeSettingDTO> funcTypeSettingDtoMap = funcTypeSettings.stream()
                .collect(Collectors.toMap(DemandLightingFuncTypeSettingDTO::getFuncType, o -> o));
        HashMap<String, DemandLightingFuncTypeSettingVO> funcTypeSettingMap = new HashMap<>(16);
        Set<String> spaceFuncTypeCodes;
        if (DemandInstanceTypeEnum.SPACE.equals(demandInstanceType)) {
            spaceFuncTypeCodes = map.keySet();
        } else {
            spaceFuncTypeCodes = new HashSet<>();
            Set<String> finalSpaceFuncTypeCodes = spaceFuncTypeCodes;
            spaceFuncTypes.forEach(node -> finalSpaceFuncTypeCodes.add(node.getKey().toString()));
        }
        for (String spaceFuncType : spaceFuncTypeCodes) {
            // 获取空间功能类型下的功能类型配置分组
            DemandLightingSpaceFuncTypeVO spaceFuncTypeVO = map.get(spaceFuncType);
            if (null == spaceFuncTypeVO || CollUtil.isEmpty(spaceFuncTypeVO.getCircuitFuncTypes())) {
                continue;
            }

            // 遍历功能类型配置
            for (DemandLightingFuncTypeSettingVO funcTypeSettingNode : spaceFuncTypeVO.getCircuitFuncTypes()) {
                // 最新的配置
                DemandLightingFuncTypeSettingVO funcTypeSettingVO = funcTypeSettingMap.get(funcTypeSettingNode.getFuncType().getValue());
                // 已保存的照明功能类型配置
                DemandLightingFuncTypeSettingDTO funcTypeSettingDTO = funcTypeSettingDtoMap.get(funcTypeSettingNode.getFuncType());
                if (null == funcTypeSettingVO) {
                    funcTypeSettingVO = BeanUtil.copyProperties(funcTypeSettingNode, DemandLightingFuncTypeSettingVO.class);
                    funcTypeSettingVO.setCircuitIds(new HashSet<>());
                    funcTypeSettingVO.setCircuitSettings(new ArrayList<>());
                    if (Objects.nonNull(funcTypeSettingDTO)) {
                        funcTypeSettingVO.setSettingMode(funcTypeSettingDTO.getSettingMode());
                        funcTypeSettingVO.setCircuitGroupSettings(funcTypeSettingDTO.getCircuitGroupSettings());
                    }
                    funcTypeSettingMap.put(funcTypeSettingNode.getFuncType().getValue(), funcTypeSettingVO);
                }
                Map<String, DemandLightingCircuitSettingModeEnum> groupCircuitIdAndSettingMode = new HashMap<>();
                if (Objects.nonNull(funcTypeSettingDTO)
                        && CollectionUtils.isNotEmpty(funcTypeSettingDTO.getCircuitGroupSettings())
                        && MapUtils.isNotEmpty(groupIdAndCircuitIds)) {
                    for (DemandLightingCircuitGroupSettingDTO dbGroupSetting : funcTypeSettingDTO.getCircuitGroupSettings()) {
                        Long groupId = dbGroupSetting.getGroupId();
                        DemandLightingTypeSettingModeEnum groupSettingMode = dbGroupSetting.getSettingMode();
                        Set<String> circuitIds = groupIdAndCircuitIds.get(groupId);
                        if (CollectionUtils.isNotEmpty(circuitIds)) {
                            for (String circuitId : circuitIds) {
                                groupCircuitIdAndSettingMode.put(circuitId, groupSettingMode.toCircuitSettingMode());
                            }
                        }
                    }
                }
                if (CollUtil.isNotEmpty(funcTypeSettingNode.getCircuitSettings())) {
                    Map<String, DemandLightingCircuitSettingModeEnum> circuitId2settingMode = null;
                    if (null != funcTypeSettingDTO && CollUtil.isNotEmpty(funcTypeSettingDTO.getCircuitSettings())) {
                        // 已保存的回路ID和其设置映射map
                        circuitId2settingMode = funcTypeSettingDTO.getCircuitSettings().stream()
                                .collect(Collectors.toMap(DemandLightingCircuitSettingDTO::getCircuitId, DemandLightingCircuitSettingDTO::getSettingMode));
                    }

                    for (DemandLightingCircuitSettingVO circuitSetting : funcTypeSettingNode.getCircuitSettings()) {
                        String circuitId = circuitSetting.getCircuitId();
                        // 去重，防止回路在多个空间下导致合并重复
                        if (!funcTypeSettingVO.getCircuitIds().contains(circuitId)) {
                            funcTypeSettingVO.getCircuitIds().add(circuitId);
                            funcTypeSettingVO.getCircuitSettings().add(circuitSetting);
                        }
                        if (MapUtils.isNotEmpty(groupCircuitIdAndSettingMode)) {
                            DemandLightingCircuitSettingModeEnum groupSettingMode = groupCircuitIdAndSettingMode.get(circuitId);
                            // 如果存在分组配置, 则强制写入分组的配置
                            circuitSetting.setSettingMode(Objects.nonNull(groupSettingMode)
                                    ? groupSettingMode : DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED);
                            continue;
                        }
                        if (Objects.nonNull(circuitSetting.getSettingMode())) {
                            continue;
                        }
                        circuitSetting.setSettingMode(DemandLightingCircuitSettingModeEnum.getSettingMode(
                                funcTypeSettingVO.getSettingMode(), Objects.isNull(circuitId2settingMode) ? null : circuitId2settingMode.get(circuitId)));
                    }
                }
            }
        }
        return new ArrayList<>(funcTypeSettingMap.values());
    }


}
