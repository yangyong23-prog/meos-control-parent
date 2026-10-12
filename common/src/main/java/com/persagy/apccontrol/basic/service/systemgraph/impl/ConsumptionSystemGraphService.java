package com.persagy.apccontrol.basic.service.systemgraph.impl;

import cn.hutool.core.date.StopWatch;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.EquipmentNodeDto;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.DmpObjTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.SystemGraphType;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.*;
import com.persagy.apccontrol.basic.outside.feign.client.DtpRwdClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphEditorService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirConditioningColdSourceStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author houyuanzhi
 * @date 2023/6/21 18:51
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConsumptionSystemGraphService implements LogComponent {

    private final DtpRwdClientService dtpRwdClientService;
    private final DtpRwdClient dtpRwdClient;
    private final CommonConfigService commonConfigService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final SystemGraphService systemGraphService;
    private final SystemGraphEditorService systemGraphEditorService;
    private final AbstractCommonServer abstractCommonServer;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final LocalRedisCacheHandler localRedisCacheHandler;
    private final RunningMonitorAirConditioningColdSourceStrategy runningMonitorAirConditioningColdSourceStrategy;
    private final Executor executor = BusinessThreadPoolExecutor.ttlExecutor();
    /**
     * 物理世界是否查询缓存
     */
    boolean RWD_QUERY_CACHE = false;
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);


    /**
     * 按id查询系统图（会加工图例数据，交互信息，信息点数据）
     *
     * @param id 身份证件
     * @return {@link SystemGraphInstanceVo }
     */
    public SystemGraphInstanceVo querySystemGraphById(Long id) {
        SystemGraph graph = systemGraphService.getById(id);
        if (graph == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_93ca357c440049079899fa13d6674614"), LocaleMessageUtil.getMessage("key_93ca357c440049079899fa13d6674614"));
        }
        Long systemGraphDuplicateId = graph.getSystemGraphDuplicateId();
        if (systemGraphDuplicateId == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_93ca357c440049079899fa13d6674614"), LocaleMessageUtil.getMessage("key_5cb3be614a2342edb2fd7fed43c162f5"));
        }
        SystemGraphInstanceVo systemGraphData = systemGraphEditorService.getSystemGraphData(systemGraphDuplicateId, false);
        if (Objects.isNull(systemGraphData)) {
            return new SystemGraphInstanceVo();
        }
        List<GraphConfigVo> graphConfig = commonConfigService.getGraphConfig();
        List<SystemGraphFilterStatusVO> systemGraphFilterStatusList = commonConfigService.getSystemGraphFilterStatus();
        Map<String, SystemGraphFilterStatusVO> filterStatusMap = systemGraphFilterStatusList.stream().collect(Collectors.toMap(SystemGraphFilterStatusVO::getClassCode, Function.identity(), (value1, value2) -> value2));
        String graphName = systemGraphService.getGraphName(graph.getClassCode(), graph.getGraphTypeCode(), graphConfig, graph.getSystemObjectId());
        Map<String, GraphConfigVo> graphConfigMap = graphConfig.stream().collect(Collectors.toMap(GraphConfigVo::getMapTitle, Function.identity(), (v1, v2) -> v1));
        GraphConfigVo graphConfigVo = graphConfigMap.get(graphName);
        systemGraphData.setDisplayMode(graphConfigVo == null || graphConfigVo.getDisplayMode() == null ? 1 : graphConfigVo.getDisplayMode());
        List<GraphGroupVo> groupVos = systemGraphData.getGroupVos();
        List<EquipmentNodeVo> equipmentNodeVos = systemGraphData.getEquipmentNodeVos();
        List<EquipmentNodeVo> passByVos = systemGraphData.getPassByVos();
        boolean groupVosIsNotEmpty = CollectionUtils.isNotEmpty(groupVos);
        boolean equipmentIsNotEmpty = CollectionUtils.isNotEmpty(equipmentNodeVos);
        boolean passByVosIsNotEmpty = CollectionUtils.isNotEmpty(passByVos);
        Set<String> allClassCodeSet = Sets.newHashSet();
        if (groupVosIsNotEmpty) {
            allClassCodeSet.addAll(groupVos.stream().map(e -> e.getDataObject().getString("classCode")).filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
        }
        if (equipmentIsNotEmpty) {
            allClassCodeSet.addAll(equipmentNodeVos.stream().map(EquipmentNodeDto::getObjClassCode).filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
        }
        if (passByVosIsNotEmpty) {
            allClassCodeSet.addAll(passByVos.stream().map(EquipmentNodeDto::getObjClassCode).filter(StringUtils::isNotEmpty).collect(Collectors.toSet()));
        }
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine = new HashMap<>();
        Map<String, String> mapClassCodeAndName = new HashMap<>();
        Map<String, Set<String>> objectRuleMap = new HashMap<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("buildMaps");
        if (CollectionUtils.isNotEmpty(allClassCodeSet)) {
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> mapClassCodeInfoCodeAndDefine.putAll(dtpKnowledgeClientService.mapClassCodeInfoCodeAndDefine(false, false, allClassCodeSet)), executor);
            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> mapClassCodeAndName.putAll(dtpKnowledgeClientService.mapClassCodeAndName(false, false, allClassCodeSet)), executor);
            CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> objectRuleMap.putAll(this.abstractCommonServer.queryObjectsExistRuleMap(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), allClassCodeSet)), executor);
            CompletableFuture.allOf(future1, future2, future3).join();
        }
        stopWatch.stop();
        stopWatch.start("processGroupVos");
        if (groupVosIsNotEmpty) {
            processGroupVos(groupVos, objectRuleMap, filterStatusMap, mapClassCodeInfoCodeAndDefine, systemGraphData);
        }
        stopWatch.stop();
        stopWatch.start("processEquipmentVos");
        List<EquipmentNodeVo> equipmentNodeVoAll = new ArrayList<>();
        if (equipmentIsNotEmpty) {
            processEquipmentVos(equipmentNodeVos, mapClassCodeAndName, graphName, filterStatusMap, objectRuleMap, equipmentNodeVoAll, graph);
        }
        stopWatch.stop();
        stopWatch.start("processPassByVos");
        if (passByVosIsNotEmpty) {
            // 获取所有阀门的classCode
            passByVos.parallelStream().forEach(vo -> {
                CompletableFuture.runAsync(() -> systemGraphService.buildFixedLabelByPassBy(vo, objectRuleMap, mapClassCodeInfoCodeAndDefine), executor);
                FilterStatusVO filterStatusVO = getFilterStatus(vo.getObjClassCode(), filterStatusMap, objectRuleMap, vo.getDataObjectId());
                vo.setFilterStatusVO(filterStatusVO);
                vo.setFilterStatus(filterStatusVO.getInfoCode());
            });
            equipmentNodeVoAll.addAll(passByVos);
        }
        stopWatch.stop();
        if (CollectionUtils.isEmpty(equipmentNodeVoAll)) {
            return systemGraphData;
        }
        stopWatch.start("haveEquipmentNodeVo");
        Map<String, Map<String, Object>> infoCodeValues = getInfoCodeValues(equipmentNodeVoAll);
        if (equipmentIsNotEmpty) {
            haveEquipmentNodeVo(equipmentNodeVos, infoCodeValues, mapClassCodeInfoCodeAndDefine);
            systemGraphData.setEquipmentNodeVos(equipmentNodeVos);
        }
        if (passByVosIsNotEmpty) {
            haveEquipmentNodeVo(passByVos, infoCodeValues, mapClassCodeInfoCodeAndDefine);
            systemGraphData.setPassByVos(passByVos);
        }
        stopWatch.stop();
        logProjectInstance().info("获取关系图耗时", Pair.of("cost", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return systemGraphData;
    }

    /**
     * 处理设备
     *
     * @param equipmentNodeVos    设备节点vos
     * @param mapClassCodeAndName 映射类代码和名称
     * @param graphName           图形名称
     * @param filterStatusMap     滤镜状态Map
     * @param objectRuleMap       对象规则map
     * @param equipmentNodeVoAll  全部设备
     */
    private void processEquipmentVos(List<EquipmentNodeVo> equipmentNodeVos, Map<String, String> mapClassCodeAndName, String graphName, Map<String, SystemGraphFilterStatusVO> filterStatusMap,
                                     Map<String, Set<String>> objectRuleMap, List<EquipmentNodeVo> equipmentNodeVoAll, SystemGraph graph) {
        // 获取信息点配置
        List<InteractiveConfigVo> interactiveConfig = commonConfigService.getInteractiveConfig();
        Map<String, List<GraphInformationVo>> graphInformationGroupByClassCode = commonConfigService.getGraphInformationGroupByClassCode();
        // 获取信息点配置
        Set<String> classCodeSet = equipmentNodeVos.stream().map(EquipmentNodeDto::getObjClassCode).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        Map<String, List<RcInfoCodeCfg>> mapListInfoCodeConfigs = localRedisCacheHandler.mapListInfoCodeConfigs(classCodeSet);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        equipmentNodeVos.forEach(vo -> futures.add(CompletableFuture.runAsync(() -> {
            List<CompletableFuture<Void>> voidCompletableFutures = new ArrayList<>();
            vo.setClassName(mapClassCodeAndName.get(vo.getObjClassCode()));
            // 构建交互配置
            if (CollectionUtils.isNotEmpty(interactiveConfig)) {
                voidCompletableFutures.add(CompletableFuture.runAsync(() -> systemGraphService.buildInteractiveConfig(vo, graphName, interactiveConfig), executor));
            }
            // 对供冷总览图做额外的交互处理
            if ((StringUtils.equals(graph.getClassCode(), DtpRwdClassCodeEnum.ACAC_SYSTEM.value()) || StringUtils.equals(graph.getClassCode(), DtpRwdClassCodeEnum.ACAH_SYSTEM.value())
                    || StringUtils.equals(graph.getClassCode(), DtpRwdClassCodeEnum.ACAI_SYSTEM.value()) || StringUtils.equals(graph.getClassCode(), DtpRwdClassCodeEnum.ACCI_SYSTEM.value()) )
                    && StringUtils.equals(graph.getGraphTypeCode(), "源端供水关系图")) {
                voidCompletableFutures.add(CompletableFuture.runAsync(() -> buildPackNodeInteractiveConfig(vo, graph), executor));
            }
            FilterStatusVO filterStatusVO = getFilterStatus(vo.getObjClassCode(), filterStatusMap, objectRuleMap, vo.getDataObjectId());
            vo.setFilterStatusVO(filterStatusVO);
            vo.setFilterStatus(filterStatusVO.getInfoCode());
            systemGraphService.buildFixedLabel(vo, objectRuleMap, graphInformationGroupByClassCode);
            systemGraphService.buildDrawerAllInfoCodes(vo, objectRuleMap, mapListInfoCodeConfigs);
            boolean isShowPanelNode = systemGraphService.buildPanelNodeInformationList(vo, graphName, objectRuleMap, mapListInfoCodeConfigs);
            // 如果返回false，则不显示信息板
            if (!isShowPanelNode) {
                vo.setPanelNode(null);
            }
            JSONObject anchor2EquipmentNodeList = vo.getAnchor2EquipmentNodeList();
            if (MapUtils.isNotEmpty(anchor2EquipmentNodeList)) {
                for (String anchor : anchor2EquipmentNodeList.keySet()) {
                    JSONObject jo = anchor2EquipmentNodeList.getJSONObject(anchor);
                    if (MapUtils.isEmpty(jo)) {
                        continue;
                    }
                    for (String anchorEquip : jo.keySet()) {
                        JSONArray jsonArray = jo.getJSONArray(anchorEquip);
                        if (CollectionUtils.isEmpty(jsonArray)) {
                            continue;
                        }
                        JSONObject equipmentObj = jsonArray.getJSONObject(0);
                        JSONObject dataObject = equipmentObj.getJSONObject("dataObject");
                        String classCode = dataObject.getString("classCode");
                        String objId = dataObject.getString("id");
                        SystemGraph systemGraph = systemGraphService.getSystemGraph(objId, classCode, "供电关系图");
                        boolean hasSystemGraph = systemGraph != null && systemGraph.getSystemGraphDuplicateId() != null;
                        equipmentObj.put("canJump", hasSystemGraph);
                        if (hasSystemGraph) {
                            equipmentObj.put("systemObjectId", systemGraph.getSystemObjectId());
                            equipmentObj.put("interactiveObjectCode", classCode);
                            equipmentObj.put("graphTypeCode", systemGraph.getGraphTypeCode());
                            equipmentObj.put("graphName", systemGraph.getGraphName());
                        }
                        // 产品要求：统一认第一个动态 一个列表只认第一个对象
                        break;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(voidCompletableFutures)) {
                CompletableFuture.allOf(voidCompletableFutures.toArray(new CompletableFuture[0])).join();
            }
        }, executor)));

        // 确保所有异步任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        equipmentNodeVoAll.addAll(equipmentNodeVos);
    }

    /**
     * 构建供冷总览图打包设备的交互配置
     *
     * @param vo 设备节点
     */
    private void buildPackNodeInteractiveConfig(EquipmentNodeVo vo, SystemGraph graph) {
        if (!StringUtils.equals(vo.getCompType(), "packNode")) {
            return;
        }

        if (StringUtils.equals(vo.getObjClassCode(), DtpRwdClassCodeEnum.ACGEFP.value()) &&
                StringUtils.equals(graph.getClassCode(), DtpRwdClassCodeEnum.ACAC_SYSTEM.value())) {
            // 补水泵
            processWaterPump(vo, graph);
        } else if (StringUtils.equals(vo.getObjClassCode(), DtpRwdClassCodeEnum.AC_SPACE.value())) {
            // 控制分区
            InteractiveConfigVo interactiveConfigVo = new InteractiveConfigVo();
            interactiveConfigVo.setGraphTypeCode("末端供水关系图");
            systemGraphService.generateInteractiveConfig(vo, graph.getSystemObjectId(), graph.getClassCode(), interactiveConfigVo);
        }
    }

    /**
     * 处理水泵
     *
     * @param vo 设备
     * @since 7.250701.0
     */
    private void processWaterPump(EquipmentNodeVo vo, SystemGraph graph) {
        JSONObject dataObject = vo.getDataObject();
        List<JSONObject> items = dataObject.getJSONArray("items").toJavaList(JSONObject.class);
        Set<String> objIds = items.stream().map(e -> e.getString("id")).collect(Collectors.toSet());
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        DtpRwdObjectRelationQueryParam relationQueryParam = new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.CHILL_WATER_LOOP);
        relationQueryParam.setObjFrom(objIds);
        param.setRelationTo(relationQueryParam);
        // 查询空调冷冻水补水关系
        int count = dtpRwdClientService.countObject(param);
        if (count != 0) {
            InteractiveConfigVo interactiveConfigVo = new InteractiveConfigVo();
            interactiveConfigVo.setGraphTypeCode("冷冻水补水监测图");
            systemGraphService.generateInteractiveConfig(vo, graph.getSystemObjectId(), DtpRwdClassCodeEnum.ACAC_SYSTEM.value(), interactiveConfigVo);
            return;
        }
        // 查询空调冷却水补水关系
        relationQueryParam = new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.COOLING_WATER_LOOP);
        relationQueryParam.setObjFrom(objIds);
        param.setRelationTo(relationQueryParam);
        // 查询空调冷冻水补水关系
        count = dtpRwdClientService.countObject(param);
        if (count != 0) {
            InteractiveConfigVo interactiveConfigVo = new InteractiveConfigVo();
            interactiveConfigVo.setGraphTypeCode("冷却水补水监测图");
            systemGraphService.generateInteractiveConfig(vo, graph.getSystemObjectId(), DtpRwdClassCodeEnum.ACAC_SYSTEM.value(), interactiveConfigVo);
        }
    }

    /**
     * 处理GroupVos
     *
     * @param groupVos                      vos集团
     * @param objectRuleMap                 对象规则图
     * @param filterStatusMap               过滤器状态图
     * @param mapClassCodeInfoCodeAndDefine 映射类代码信息代码并定义
     * @param systemGraphData               系统图数据
     */
    private void processGroupVos(List<GraphGroupVo> groupVos, Map<String, Set<String>> objectRuleMap, Map<String, SystemGraphFilterStatusVO> filterStatusMap, Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine, SystemGraphInstanceVo systemGraphData) {
        groupVos.forEach(groupVo -> {
            // 添加固定标签
            systemGraphService.buildFixedLabelByGroup(groupVo, objectRuleMap);
            String classCode = groupVo.getDataObject().getString("classCode");
            FilterStatusVO filterStatusVO = getFilterStatus(classCode, filterStatusMap, objectRuleMap, groupVo.getDataObjectId());
            groupVo.setFilterStatusVO(filterStatusVO);
            groupVo.setFilterStatus(filterStatusVO.getInfoCode());
        });

        Map<String, Map<String, Object>> groupInfoCodeValues = getInfoCodeValuesByGroup(groupVos);
        haveGraphGroupVo(groupVos, groupInfoCodeValues, mapClassCodeInfoCodeAndDefine);
        systemGraphData.setGroupVos(groupVos);
    }

    /**
     * 得到过滤状态
     *
     * @param classCode       设备类
     * @param filterStatusMap 过滤状态map
     * @param objectRuleMap   对象规则map
     * @param objectId        对象id
     * @return {@code FilterStatusVO }
     * @since 7.250403.0
     */
    private FilterStatusVO getFilterStatus(String classCode, Map<String, SystemGraphFilterStatusVO> filterStatusMap, Map<String, Set<String>> objectRuleMap, String objectId) {
        SystemGraphFilterStatusVO systemGraphFilterStatusVO = filterStatusMap.get(classCode);
        FilterStatusVO filterStatus = new FilterStatusVO();
        if (systemGraphFilterStatusVO != null) {
            // 小附件滤镜状态
            switch (systemGraphFilterStatusVO.getFilterStatus()) {
                case "2":
                    filterStatus.setInfoCode(InfoCodeEnum.RUN_STATUS.getCode());
                    break;
                case "3":
                    filterStatus.setInfoCode(InfoCodeEnum.SWITCH_STATUS.getCode());
                    break;
                case "4":
                    filterStatus.setInfoCode(InfoCodeEnum.GROUP_EQUIP_RUNING_NUM.getCode());
                    break;
                default:
            }
        }
        String infoCode = filterStatus.getInfoCode();
        if (StringUtils.isNotBlank(infoCode)) {
            Set<String> infoCodes = objectRuleMap.get(objectId);
            if (CollectionUtils.isNotEmpty(infoCodes)) {
                filterStatus.setExistsIotRule(infoCodes.contains(infoCode));
            } else {
                filterStatus.setExistsIotRule(false);
            }
        }
        return filterStatus;
    }

    /**
     * 查询未加工的系统图数据（数据库数据，不进行加工）
     *
     * @param id 身份证件
     * @return {@link SystemGraphInstanceVo }
     */
    public SystemGraphInstanceVo querySystemGraphDataNoProcess(Long id) {
        SystemGraphInstanceVo systemGraphData = new SystemGraphInstanceVo();
        SystemGraph graph = systemGraphService.getById(id);
        if (graph == null) {
            return systemGraphData;
        }
        Long systemGraphDuplicateId = graph.getSystemGraphDuplicateId();
        if (systemGraphDuplicateId == null) {
            return systemGraphData;
        }
        systemGraphData = systemGraphEditorService.getSystemGraphDataNoDetailDataById(systemGraphDuplicateId);
        if (Objects.isNull(systemGraphData)) {
            return new SystemGraphInstanceVo();
        }
        return systemGraphData;
    }

    /**
     * @Author 薛涛
     * @Description 将iot数据放到原有数据中
     * @Date 17:37 2024/7/4
     * @Param [equipmentNodeVos, infoCodeValues]
     **/
    private void haveEquipmentNodeVo(List<EquipmentNodeVo> equipmentNodeVos, Map<String, Map<String, Object>> infoCodeValues,
                                     Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine) {
        Set<String> objIds = equipmentNodeVos.stream().map(EquipmentNodeVo::getDataObjectId).collect(Collectors.toSet());
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setId(objIds));
        Map<String, DtpRwdObjectDTO> objectMap = dtpRwdObjectDTOS.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (v1, v2) -> v2));
        for (EquipmentNodeVo equipmentNodeVo : equipmentNodeVos) {
            String dataObjectId = equipmentNodeVo.getDataObjectId();
            Map<String, DtpKnowledgeInfoCodeDTO> infoCodeMap = mapClassCodeInfoCodeAndDefine.getOrDefault(equipmentNodeVo.getObjClassCode(), new HashMap<>());
            Map<String, Object> infoCodeObjectMap = infoCodeValues.getOrDefault(dataObjectId, new HashMap<>());
            List<RcInfoCodeCfg> fixedInfoCodes = equipmentNodeVo.getFixedInfoCodes();
            List<RcInfoCodeCfg> drawerAllInfoCodes = equipmentNodeVo.getDrawerAllInfoCodes();
            DtpRwdObjectDTO dtpRwdObjectDTO = objectMap.getOrDefault(dataObjectId, new DtpRwdObjectDTO());
            if (CollectionUtils.isNotEmpty(fixedInfoCodes)) {
                Iterator<RcInfoCodeCfg> it = fixedInfoCodes.iterator();
                while (it.hasNext()) {
                    RcInfoCodeCfg fixedInfoCode = it.next();
                    String infoCode = fixedInfoCode.getInfoCode();
                    DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeMap.get(infoCode);
                    if (dtpKnowledgeInfoCodeDTO != null) {
                        fixedInfoCode.setOriginDataType(dtpKnowledgeInfoCodeDTO.getOriginDataType());
                        fixedInfoCode.setCategory(dtpKnowledgeInfoCodeDTO.getCategory());
                    }
                    boolean b = handleValue(fixedInfoCode, infoCodeObjectMap, dtpRwdObjectDTO, infoCode);
                    if (!b) {
                        it.remove();
                    }
                }

            }
            if (CollectionUtils.isNotEmpty(drawerAllInfoCodes)) {
                Iterator<RcInfoCodeCfg> it = drawerAllInfoCodes.iterator();
                while (it.hasNext()) {
                    RcInfoCodeCfg codeCfg = it.next();
                    String infoCode = codeCfg.getInfoCode();
                    DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeMap.get(infoCode);
                    if (dtpKnowledgeInfoCodeDTO != null) {
                        codeCfg.setOriginDataType(dtpKnowledgeInfoCodeDTO.getOriginDataType());
                        codeCfg.setCategory(dtpKnowledgeInfoCodeDTO.getCategory());
                    }
                    boolean b = handleValue(codeCfg, infoCodeObjectMap, dtpRwdObjectDTO, infoCode);
                    if (!b) {
                        it.remove();
                    }
                }
            }
            if (equipmentNodeVo.getPanelNode() != null && CollectionUtils.isNotEmpty(equipmentNodeVo.getPanelNode().getInfoCodes())) {
                List<RcInfoCodeCfg> infoCodes = equipmentNodeVo.getPanelNode().getInfoCodes();
                Iterator<RcInfoCodeCfg> it = infoCodes.iterator();
                while (it.hasNext()) {
                    RcInfoCodeCfg codeCfg = it.next();
                    String infoCode1 = codeCfg.getInfoCode();
                    DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeMap.get(infoCode1);
                    if (dtpKnowledgeInfoCodeDTO != null) {
                        codeCfg.setOriginDataType(dtpKnowledgeInfoCodeDTO.getOriginDataType());
                        codeCfg.setCategory(dtpKnowledgeInfoCodeDTO.getCategory());
                    }
                    boolean b = handleValue(codeCfg, infoCodeObjectMap, dtpRwdObjectDTO, infoCode1);
                    if (!b) {
                        it.remove();
                    }
                }
            }
            JSONObject textMap = equipmentNodeVo.getTextMap();
            if (textMap != null && MapUtils.isNotEmpty(textMap.getInnerMap())) {
                for (String key : textMap.keySet()) {
                    JSONObject textMapValueJson = textMap.getJSONObject(key);
                    String textCode = textMapValueJson.getString("textCode");
                    DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeMap.get(textCode);
                    if (dtpKnowledgeInfoCodeDTO != null) {
                        textMapValueJson.put("originDataType", dtpKnowledgeInfoCodeDTO.getOriginDataType());
                    }
                    Object value = null;
                    JSONObject infoDetail = textMapValueJson.getJSONObject("infoDetail");
                    if (ObjectUtils.isEmpty(infoDetail)) {
                        continue;
                    }
                    if (StringUtils.equals(infoDetail.getString("category"), "STATIC")) {
                        Map<String, Object> undefinedProperties = dtpRwdObjectDTO.getUndefinedProperties();
                        if (MapUtils.isNotEmpty(undefinedProperties)) {
                            value = undefinedProperties.get(textCode);
                        }
                    } else {
                        value = infoCodeObjectMap.get(textCode);
                    }
                    if (value != null) {
                        textMapValueJson.put("dataValue", value);
                    }
                }
            }
            equipmentNodeVo.setFixedInfoCodes(fixedInfoCodes);
        }
    }

    private boolean handleValue(RcInfoCodeCfg fixedInfoCode, Map<String, Object> infoCodeObjectMap, DtpRwdObjectDTO dtpRwdObjectDTO, String infoCode) {
        if (StringUtils.equals(fixedInfoCode.getCategory(), "STATIC")) {
            // 如果是静态信息点，需要对象中取
            if (MapUtils.isEmpty(dtpRwdObjectDTO.getUndefinedProperties())) {
                return false;
            }
            Object val = dtpRwdObjectDTO.getUndefinedProperties().get(infoCode);
            if (val == null) {
                return false;
            }
            fixedInfoCode.setDataValue(String.valueOf(val));
        } else {
            Object value = infoCodeObjectMap.get(infoCode);
            if (value != null) {
                fixedInfoCode.setDataValue(String.valueOf(value));
            }
        }
        return true;
    }

    private void haveGraphGroupVo(List<GraphGroupVo> groupVos, Map<String, Map<String, Object>> infoCodeValues, Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> mapClassCodeInfoCodeAndDefine) {
        Set<String> objIds = groupVos.stream().map(GraphGroupVo::getDataObjectId).collect(Collectors.toSet());
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), new DtpRwdObjectQueryParam().setId(objIds));
        Map<String, DtpRwdObjectDTO> objectMap = dtpRwdObjectDTOS.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (v1, v2) -> v2));
        for (GraphGroupVo groupVo : groupVos) {
            String dataObjectId = groupVo.getDataObjectId();
            Map<String, Object> infoCodeObjectMap = infoCodeValues.getOrDefault(dataObjectId, new HashMap<>());
            Map<String, DtpKnowledgeInfoCodeDTO> infoCodeMap = mapClassCodeInfoCodeAndDefine.getOrDefault(groupVo.getDataObject().getString("classCode"), new HashMap<>());
            List<RcInfoCodeCfg> fixedInfoCodes = groupVo.getFixedInfoCodes();
            DtpRwdObjectDTO groupObj = objectMap.getOrDefault(dataObjectId, new DtpRwdObjectDTO());

            if (CollectionUtils.isNotEmpty(fixedInfoCodes)) {
                Iterator<RcInfoCodeCfg> it = fixedInfoCodes.iterator();
                while (it.hasNext()) {
                    RcInfoCodeCfg fixedInfoCode = it.next();
                    String infoCode = fixedInfoCode.getInfoCode();
                    DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeMap.get(infoCode);
                    if (dtpKnowledgeInfoCodeDTO != null) {
                        fixedInfoCode.setCategory(dtpKnowledgeInfoCodeDTO.getCategory());
                    }
                    if (StringUtils.equals(fixedInfoCode.getCategory(), "STATIC")) {
                        // 如果是静态信息点，需要对象中取
                        if (MapUtils.isEmpty(groupObj.getUndefinedProperties())) {
                            it.remove();
                            continue;
                        }
                        Object val = groupObj.getUndefinedProperties().get(infoCode);
                        if (val == null) {
                            it.remove();
                        }
                        fixedInfoCode.setDataValue(String.valueOf(val));
                    } else {
                        Object value = infoCodeObjectMap.get(infoCode);
                        if (value != null) {
                            fixedInfoCode.setDataValue(String.valueOf(value));
                        }
                    }


                }
            }
            JSONObject textMap = groupVo.getTextMap();
            if (textMap != null && MapUtils.isNotEmpty(textMap.getInnerMap())) {
                for (String key : textMap.keySet()) {
                    JSONObject textMapValueJson = textMap.getJSONObject(key);
                    String textCode = textMapValueJson.getString("textCode");
                    Object value = infoCodeObjectMap.get(textCode);
                    textMapValueJson.put("dataValue", value);
                }
            }
            groupVo.setFixedInfoCodes(fixedInfoCodes);
        }
    }


    /**
     * 获取信息点值
     *
     * @param equipmentNodes 设备节点
     * @return {@link Map }<{@link String }, {@link Map }<{@link String }, {@link Object }>>
     */
    private Map<String, Map<String, Object>> getInfoCodeValues(Collection<EquipmentNodeVo> equipmentNodes) {
        String projectId = ContextUtil.getProjectId();
        Set<String> objectIdList = new HashSet<>();
        Set<String> infoCodeList = new HashSet<>();
        for (EquipmentNodeVo equipmentNode : equipmentNodes) {
            List<RcInfoCodeCfg> fixedInfoCodes = equipmentNode.getFixedInfoCodes();
            objectIdList.add(equipmentNode.getDataObjectId());
            if (CollectionUtils.isNotEmpty(fixedInfoCodes)) {
                for (RcInfoCodeCfg fixedInfoCode : fixedInfoCodes) {
                    infoCodeList.add(fixedInfoCode.getInfoCode());
                }
            }
            List<RcInfoCodeCfg> drawerAllInfoCodes = equipmentNode.getDrawerAllInfoCodes();
            if (CollectionUtils.isNotEmpty(drawerAllInfoCodes)) {
                for (RcInfoCodeCfg codeCfg : drawerAllInfoCodes) {
                    infoCodeList.add(codeCfg.getInfoCode());
                }
            }
            if (equipmentNode.getPanelNode() != null && CollectionUtils.isNotEmpty(equipmentNode.getPanelNode().getInfoCodes())) {
                List<RcInfoCodeCfg> infoCodes = equipmentNode.getPanelNode().getInfoCodes();
                for (RcInfoCodeCfg infoCode : infoCodes) {
                    infoCodeList.add(infoCode.getInfoCode());
                }
            }
            JSONObject textMap = equipmentNode.getTextMap();
            if (textMap != null && MapUtils.isNotEmpty(textMap.getInnerMap())) {
                Map<String, Object> innerMap = textMap.getInnerMap();
                Collection<Object> textMapValues = innerMap.values();
                for (Object textMapValue : textMapValues) {
                    JSONObject textMapValueJson = (JSONObject) textMapValue;
                    String textCode = textMapValueJson.getString("textCode");
                    infoCodeList.add(textCode);
                }
            }
        }
        List<InfoCodeValueVO> list = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIdList, infoCodeList);
        return InfoCodeValueVO.listToObjectInfoCodeValueMap(list);
    }

    private Map<String, Map<String, Object>> getInfoCodeValuesByGroup(Collection<GraphGroupVo> groups) {
        Set<String> objectIdList = new HashSet<>();
        Set<String> infoCodeList = new HashSet<>();
        for (GraphGroupVo groupVo : groups) {
            List<RcInfoCodeCfg> fixedInfoCodes = groupVo.getFixedInfoCodes();
            objectIdList.add(groupVo.getDataObjectId());
            if (CollectionUtils.isNotEmpty(fixedInfoCodes)) {
                for (RcInfoCodeCfg fixedInfoCode : fixedInfoCodes) {
                    infoCodeList.add(fixedInfoCode.getInfoCode());
                }
            }
            JSONObject textMap = groupVo.getTextMap();
            if (textMap != null && MapUtils.isNotEmpty(textMap.getInnerMap())) {
                Map<String, Object> innerMap = textMap.getInnerMap();
                Collection<Object> textMapValues = innerMap.values();
                for (Object textMapValue : textMapValues) {
                    JSONObject textMapValueJson = (JSONObject) textMapValue;
                    String textCode = textMapValueJson.getString("textCode");
                    infoCodeList.add(textCode);
                }
            }
        }
        List<InfoCodeValueVO> list = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIdList, infoCodeList);
        return InfoCodeValueVO.listToObjectInfoCodeValueMap(list);
    }

    /**
     * 获取设备所属空间（只查物业空间）
     *
     * @param equipmentIds
     * @return
     */
    private Map<String, List<String>> getInSpaceMap(List<String> equipmentIds) {
        Map<String, List<String>> inSpaceMap = null;// 查询设备和空间关系
       /* List<RwdRelation> rwdRelations = dmpRwdClient.queryRelation(new RelationCriteria().setObjFrom(equipmentIds).setGraphCode("MechInArch").setRelCode("Eq2Sp"));
        //只查询物业空间
        List<String> relGeneralZones = dmpRwdClient.queryObject(new SpaceCriteria().setId(rwdRelations.stream().map(RwdRelation::getObjTo).collect(Collectors.toList())))
                .stream()
                .filter(it -> SpaceClassCodeEnum.GeneralZone.getClassCode().equals(it.getClassCode()))
                .map(RwdObject::getId)
                .collect(Collectors.toList());
        inSpaceMap = rwdRelations
                .stream()
                .filter(it -> relGeneralZones.contains(it.getObjTo()))
                .collect(Collectors.groupingBy(RwdRelation::getObjFrom, Collectors.mapping(RwdRelation::getObjTo, Collectors.toList())));*/
        return inSpaceMap;
    }


    /**
     * @param systemObjectId
     * @return
     */
    public List<SystemGraph> getRelationGraphId(String systemObjectId, boolean passBy) {
        // 先查询所有默认系统图列表
       /* List<SystemGraph> defaultSystemGraphList =
                systemGraphRepository.getNoDataPoBySystemObjectId(systemObjectId, passBy)
                        .stream().map(it -> {
                            SystemGraph vo = new SystemGraph();
                            vo.setId(it.getId());
                            vo.setName(it.getGraphName());
                            vo.setSystemId(it.getSystemObjectId());
                            vo.setTemplateId(it.getTemplateId());
                            return vo;
                        })
                        .collect(Collectors.toList());*/
        List<SystemGraph> defaultSystemGraphList = new ArrayList<>();

        // 业务领域不为空或者不为默认，替换对应系统图
        /*Map<String, SystemGraphDataPo> systemGraphDataMap = systemGraphRepository.getNoDataPoBySystemObjectId(systemObjectId, passBy)
                .stream()
                .collect(Collectors.toMap(it -> it.getSystemObjectId() + UNDERLINE + it.getTemplateId(), it -> it, (v1, v2) -> v1));
        Map<String, SystemGraphDataPo> systemGraphDataMap = new HashMap<>();
        defaultSystemGraphList.forEach(systemGraphListVo -> {
            String key = systemGraphListVo.getSystemId() + UNDERLINE + systemGraphListVo.getTemplateId();
            if (systemGraphDataMap.containsKey(key)) {
                systemGraphListVo.setId(systemGraphDataMap.get(key).getId());
                systemGraphListVo.setName(systemGraphDataMap.get(key).getGraphName());
            }
        });*/
        return defaultSystemGraphList;
    }

    /**
     * 通过系统对象id和类代码不获取数据po
     *
     * @param systemObjectId 系统对象id
     * @param classCode      设备类编码
     * @param graphTypeCode
     * @return {@link SystemGraph}
     */
    public List<SystemGraph> getNoDataPoBySystemObjectIdAndClassCode(String systemObjectId, String classCode, String graphTypeCode) {
        SystemGraph systemGraph = this.systemGraphService.getSystemGraph(systemObjectId, classCode, graphTypeCode);
        return systemGraph == null ? new ArrayList<>() : Lists.newArrayList(systemGraph);
    }

    public List<String> hasGraphSystemId() {
        //  return systemGraphRepository.hasGraphSystemId();
        return new ArrayList<>();

    }

    /**
     * key 系统图实例id
     *
     * @return
     */
    public Map<String, SystemGraphType> getGraphTypeMap() {
        Map<String, SystemGraphType> systemGraphTypeMap = new HashMap<>();
        // 查询是否有系统图
        List<String> hasGraphSystemIds = this.hasGraphSystemId();
        hasGraphSystemIds.forEach(it -> systemGraphTypeMap.put(it, SystemGraphType.NEW));

        return systemGraphTypeMap;
    }

    public SystemGraphType getGraphType(String systemId) {
        // 查询是否有系统图
        List<String> hasGraphSystemId = this.hasGraphSystemId();

        if (hasGraphSystemId.contains(systemId)) {
            return SystemGraphType.NEW;
        }

        return SystemGraphType.NONE;
    }

    /**
     * 查询联动模型数据
     *
     * @param id
     * @return
     */
    public SystemGraphModelVo modelInstanceList(Long id) {
        SystemGraphModelVo result = new SystemGraphModelVo();
        SystemGraphInstanceVo systemGraphInstanceVo = this.querySystemGraphById(id);

        // 设备实例id
        List<String> equipmentIds = new LinkedList<>();
        // 系统实例id
        List<String> systemIds = new LinkedList<>();
        // 空间实例id
        List<String> spaceIds = new LinkedList<>();
        // 设备组id 暂时不查数据
        List<String> equipmentGroupIds = new LinkedList<>();
        // 设备节点
        if (CollectionUtils.isNotEmpty(systemGraphInstanceVo.getEquipmentNodeVos())) {
            systemGraphInstanceVo.getEquipmentNodeVos().forEach(equipmentNodeVo -> addInstanceId(equipmentIds, systemIds, spaceIds, equipmentNodeVo.getDataObject()));
        }

        // 小部件
        if (CollectionUtils.isNotEmpty(systemGraphInstanceVo.getPassByVos())) {
            systemGraphInstanceVo.getPassByVos().forEach(equipmentNodeVo -> addInstanceId(equipmentIds, systemIds, spaceIds, equipmentNodeVo.getDataObject()));
        }


        // 系统图设备组
        if (CollectionUtils.isNotEmpty(systemGraphInstanceVo.getGroupVos())) {
            systemGraphInstanceVo.getGroupVos().forEach(groupVo -> addInstanceId(equipmentIds, systemIds, spaceIds, groupVo.getDataObject()));
        }

        List<DtpRwdObjectDTO> equipments = new LinkedList<>();
        List<DtpRwdObjectDTO> rwdSpaces = new LinkedList<>();
//        Map<String, List<String>> inSpaceMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(equipmentIds)) {
            // 查询所有设备实例
            DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
            equipments = dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), dtpRwdObjectQueryParam.setId(equipmentIds));
            // 查询设备所在空间实例
            dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
            DtpRwdObjectRelationQueryParam relationTo = new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE);
            relationTo.setObjFrom(equipmentIds);
            dtpRwdObjectQueryParam.setRelationTo(relationTo);
            rwdSpaces = dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), dtpRwdObjectQueryParam);
            // 查询设备和空间关系
//            inSpaceMap = getInSpaceMap(equipmentIds);
        }


        // 查询所有空间实例
        if (CollectionUtils.isNotEmpty(spaceIds)) {
            rwdSpaces.addAll(dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), new DtpRwdObjectQueryParam().setId(spaceIds)));
        }


//        SystemComposeConfig config = commonConfigService.getConfig(SYSTEM_COMPOSE_CONFIG, SystemComposeConfig.class);

        // 空间颜色配置
//        Map<String, SpaceColorInfo> spaceColorMap = commonConfigService.getSpaceColorConfig().getSpaceColorMap();


        result.setEquipments(equipments);
        result.setSpaces(rwdSpaces);

        return result;
    }

    private void addInstanceId(List<String> equipmentIds, List<String> systemIds, List<String> spaceIds, JSONObject dataObject) {
        if (dataObject == null || !dataObject.containsKey("id")) {
            return;
        }
        if (DmpObjTypeEnum.EQUIPMENT.getValue().equals(dataObject.get("objType"))) {
            equipmentIds.add(dataObject.getString("id"));
        } else if (DmpObjTypeEnum.SYSTEM.getValue().equals(dataObject.get("objType"))) {
            systemIds.add(dataObject.getString("id"));
        } else if (DmpObjTypeEnum.SPACE.getValue().equals(dataObject.get("objType"))) {
            spaceIds.add(dataObject.getString("id"));
        }
    }

    private void addEquipmentId(List<String> equipmentIds, JSONObject dataObject) {
        if (dataObject == null || !dataObject.containsKey("id")) {
            return;
        }
        /*if (DmpObjTypeEnum.EQUIPMENT.getValue().equals(dataObject.get(OBJECT_TYPE))) {
            equipmentIds.add(dataObject.getString("id"));
        }*/
    }


    /**
     * 菜单配置
     *
     * @param projectId     项目id
     * @param menuCode      菜单编码
     * @param productModule 产品编码
     * @return 菜单配置
     */
    public List<TreeNodeVO> getMenuConfig(String projectId, String menuCode, String productModule, String groupCode) {
        List<MenuConfigVo> menuConfig = this.commonConfigService.getMenuConfig();
        // 系统图配置
        Map<String, List<GraphConfigVo>> graphConfigMap =
                this.commonConfigService.getGraphConfig().stream().collect(Collectors.groupingBy(GraphConfigVo::getMapTitle));
        // 菜单配置与图配置重组数据结构，且过滤指定菜单
        List<MenuConfigVo> afterFiltering = new ArrayList<>();
        menuConfig.forEach(x -> {
            if (!"all".equals(menuCode) && !menuCode.equals(x.getMenuCode())) {
                return;
            }
            if (StringUtils.isNotBlank(productModule) && !productModule.equals(x.getProductModule())) {
                return;
            }
            GraphConfigVo graphConfigVo = graphConfigMap.get(x.getImageOrProductName()).get(0);
            x.setDiagramBaseObject(graphConfigVo.getDiagramBaseObjectCode());
            x.setGraphTypeCode(graphConfigVo.getDiagramTypeOfTheDiagram());
            x.setObjectType(graphConfigVo.getObjectType());
            x.setIsControl(graphConfigVo.getIsControl());
            x.setSystemJudgmentCondition(graphConfigVo.getSystemJudgmentCondition());
            afterFiltering.add(x);
        });
        // 转换为合并的树形结构数组
        return buildTree(afterFiltering, projectId, menuCode);
    }

    /**
     * 构建树结构
     *
     * @param mags      首页菜单配置
     * @param projectId 项目id
     * @param menuCode
     * @return 树结构
     */
    private List<TreeNodeVO> buildTree(List<MenuConfigVo> mags, String projectId, String menuCode) {
        Map<String, TreeNodeVO> firstLevelMap = new LinkedHashMap<>();
        // 菜单循环重组树结构
        mags.forEach(config -> {
            String first = config.getFirstlevelMenu();
            String second = config.getSecondaryMenu();

            // 树结构一级菜单
            TreeNodeVO firstLevelNode = firstLevelMap.computeIfAbsent(first, k -> new TreeNodeVO(k, k));
            firstLevelNode.setMenuCode(config.getMenuCode());

            // 二级节点 - 修改为使用传统循环方式避免并发修改异常
            TreeNodeVO secondLevelNode = null;
            for (TreeNodeVO node : firstLevelNode.getChildren()) {
                if (StringUtils.isNotBlank(node.getName()) && node.getName().equals(second)) {
                    secondLevelNode = node;
                    break;
                }
            }

            if (secondLevelNode == null) {
                secondLevelNode = new TreeNodeVO(second, second);
                firstLevelNode.getChildren().add(secondLevelNode);
            }

            // 三级菜单非空则进入三级菜单的系统级查询逻辑，若为三级菜单为空，则直接进入二级菜单即为末级菜单逻辑
            if (StringUtils.isNotEmpty(config.getThreeLevelMenu())) {
                threeNodeInfoHandle(config, secondLevelNode, projectId);
            } else {
                String imageOrProductName = config.getImageOrProductName();
                secondLevelNode.setImageOrProductName(imageOrProductName);
                secondLevelNode.setDiagramBaseObject(config.getDiagramBaseObject());
                secondLevelNode.setProductModule(config.getProductModule());
                secondLevelNode.setGraphTypeCode(config.getGraphTypeCode());
                int hierarchy = config.getHierarchy();
                secondLevelNode.setHierarchy(hierarchy);
            }
        });
        if (logProjectInstance().isDebugEnabled()) {
            logProjectInstance().debug("菜单数据", Pair.of("firstLevelMap", JSONObject.toJSONString(firstLevelMap)));
        }
        ArrayList<TreeNodeVO> TreeNodeVOs = new ArrayList<>();
        firstLevelMap.forEach((k, v) -> {
            if (CollectionUtils.isNotEmpty(v.getChildren())) {
                ArrayList<TreeNodeVO> hasChildren = new ArrayList<>();
                v.getChildren().forEach(x -> {
                    if (CollectionUtils.isNotEmpty(x.getChildren())) {
                        // 如果是空调冷源，则需要排序
                        if (DtpRwdClassCodeEnum.ACAH_SYSTEM.getValue().equals(menuCode)
                                || DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(menuCode)
                                || "WS".equals(menuCode)
                                || DtpRwdClassCodeEnum.ACAI_SYSTEM.getValue().equals(menuCode)
                        ) {
                            // 多个系统实例排序：先按照系统的"监测监控状态(monitoringSurveillanceStatus)"排序，顺序为可控、可监、不可监不可控，第一层顺序中如果再有多个系统实例，再按照系统实例名称排序。
                            List<TreeNodeVO> sorted = x.getChildren().stream()
                                    // 首先按监控状态是否为空排序，空值放后面
                                    .sorted(Comparator.comparing(
                                                    (TreeNodeVO e) -> StringUtils.isBlank(e.getMonitoringSurveillanceStatus()),
                                                    Comparator.naturalOrder() // false(非空)排在true(空)前面
                                            )
                                            // 然后按监控状态本身的字典序排序
                                            .thenComparing(
                                                    TreeNodeVO::getMonitoringSurveillanceStatus,
                                                    Comparator.nullsLast(String::compareTo).reversed() // 倒序
                                            )
                                            // 最后按本地名称排序
                                            .thenComparing(e -> StringUtils.isNotBlank(e.getLocalName()) ?
                                                    e.getLocalName() : "", Comparator.nullsLast(COLLATOR)))
                                    .collect(Collectors.toList());
                            x.setChildren(sorted);
                        }
                        hasChildren.add(x);
                    }
                });
                if (CollectionUtils.isNotEmpty(hasChildren)) {
                    v.setChildren(hasChildren);
                    TreeNodeVOs.add(v);
                }
            }
        });
        if (logProjectInstance().isDebugEnabled()) {
            logProjectInstance().debug("菜单数据", Pair.of("TreeNodeVOs", JSONObject.toJSONString(TreeNodeVOs)));
        }
        return TreeNodeVOs;
    }


    /**
     * 三级节点数据设置
     *
     * @param config          菜单配置
     * @param secondLevelNode 二级节点树
     * @param projectId       项目编码
     */
    private void threeNodeInfoHandle(MenuConfigVo config,
                                     TreeNodeVO secondLevelNode,
                                     String projectId) {
        String imageOrProductName = config.getImageOrProductName();
        // 项目维度的图纸处理逻辑
        if (DtpRwdClassCodeEnum.PROJECT.value().equals(config.getDiagramBaseObject())) {
            List<SystemGraph> result = getSystemList(projectId, config.getDiagramBaseObject(), config.getGraphTypeCode());
            if (CollectionUtils.isNotEmpty(result)) {
                TreeNodeVO thirdLevelNode = new TreeNodeVO(null, null);
                getThreeNode(thirdLevelNode, config, imageOrProductName, null);
                filterOutTheEmptyDeviceDiagram(secondLevelNode, result, thirdLevelNode);
            }
        } else {
            DtpRwdObjectQueryParam criteria = new DtpRwdObjectQueryParam();
            // 将 JSONArray 转换为 List
            criteria.setClassCode(config.getDiagramBaseObject());
            criteria.setWithColumns(Lists.newArrayList(
                            DtpRwdObjectDTO.Fields.id,
                            DtpRwdObjectDTO.Fields.classCode,
                    DtpRwdObjectDTO.Fields.objType,
                            DtpRwdObjectDTO.Fields.localId,
                            DtpRwdObjectDTO.Fields.localName,
                            InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value(),
                            InfoCodeEnum.IS_COOL_STORE.value(),
                            InfoCodeEnum.CHILL_PUMP_TYPE.value()
                    )
            );
            JSONObject systemJudgmentCondition = config.getSystemJudgmentCondition();
            if (Objects.nonNull(systemJudgmentCondition)) {
                systemJudgmentCondition.forEach(criteria::addInfoCode);
            }
            List<DtpRwdObjectDTO> rwdSystems = dtpRwdClientService.listObject(RWD_QUERY_CACHE, false, ContextUtil.getProjectId(), criteria);
            // 系统为度数据处理
            setSystemGraphInfo(rwdSystems, config, imageOrProductName, secondLevelNode);
        }
    }

    /**
     * 设置系统运行参数
     *
     * @param rwdSystems         系统实例
     * @param config             配置
     * @param imageOrProductName 图名或者产品名
     * @param secondLevelNode    二级节点
     */
    private void setSystemGraphInfo(List<DtpRwdObjectDTO> rwdSystems,
                                    MenuConfigVo config, String imageOrProductName,
                                    TreeNodeVO secondLevelNode) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        rwdSystems.forEach(system -> {
            futures.add(CompletableFuture.runAsync(() -> {
                List<SystemGraph> result = getSystemList(system.getId(), system.getClassCode(), config.getGraphTypeCode());
                if (logProjectInstance().isDebugEnabled()) {
                    LogExecutor.instance(ContextUtil.getGroupCode(), ContextUtil.getProjectId()).debug(log(), "获取系统图", JSONObject.toJSONString(system));
                }
                if (CollectionUtils.isNotEmpty(result)) {
                    TreeNodeVO thirdLevelNode = new TreeNodeVO(null, null);
                    getThreeNode(thirdLevelNode, config, imageOrProductName, system);
                    filterOutTheEmptyDeviceDiagram(secondLevelNode, result, thirdLevelNode);
                    // 新冷源额外逻辑
                    if (thirdLevelNode.getDiagramBaseObject().equals(DtpRwdClassCodeEnum.ACAC_SYSTEM.value())) {
                        String systemId = thirdLevelNode.getSystemId();
                        // 空间数据是否存在
                        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
                        spaceQueryParam.setFilterSpaceServiceSystemIds(new HashSet<>(Collections.singletonList(systemId)));
                        spaceQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.localName);
                        CompletableFuture<Void> voidFuture = CompletableFuture.runAsync(() -> {
                            List<DtpRwdSpaceDTO> rwdSpaceDTOS = runningMonitorAirConditioningColdSourceStrategy.listACSpaces(spaceQueryParam);
                            thirdLevelNode.setUnHideAirConditioningZoneControl(!rwdSpaceDTOS.isEmpty());
                        }, BusinessThreadPoolExecutor.ttlExecutor());
                        Set<String> systemIdSet = Sets.newHashSet(system.getId());
                        thirdLevelNode.setUnHideCoolingControl("3".equals(thirdLevelNode.getMonitoringSurveillanceStatus()));
                        Map<String, Set<String>> ruleParam = Maps.newHashMap();
                        ruleParam.put(system.getClassCode(), systemIdSet);
                        // 判断规则,如果为空则没有规则
                        Map<String, Set<String>> map = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ruleParam, Lists.newArrayList(InfoCodeEnum.CURRENT_OPERATING_MODE.value()));
                        if (!map.isEmpty()) {
                            // 空调工况
                            List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                                    Lists.newArrayList(systemId),
                                    Lists.newArrayList(InfoCodeEnum.CURRENT_OPERATING_MODE.value()));
                            if (!infoCodeValues.isEmpty()) {
                                thirdLevelNode.setCurrentOperatingMode(infoCodeValues.get(0).getDataValue().toString());
                            } else {
                                thirdLevelNode.setCurrentOperatingMode("");
                            }
                        }
                        // 查询runModeSet运行模式设定，仅中央空调冷热源全系统(ACAI)执行
                        if (DtpRwdClassCodeEnum.ACAI_SYSTEM.value().equals(thirdLevelNode.getMenuCode())) {
                            Map<String, Set<String>> runModeSetRuleMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ruleParam, Lists.newArrayList(InfoCodeEnum.RUN_MODEL_SET.value()));
                            if (!runModeSetRuleMap.isEmpty()) {
                                // 运行模式设定
                                List<InfoCodeValueVO> runModeSetValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                                        Lists.newArrayList(systemId),
                                        Lists.newArrayList(InfoCodeEnum.RUN_MODEL_SET.value()));
                                if (!runModeSetValues.isEmpty()) {
                                    thirdLevelNode.setRunModeSet(runModeSetValues.get(0).getDataValue().toString());
                                } else {
                                    thirdLevelNode.setRunModeSet("");
                                }
                            }
                        }
                        voidFuture.join();
                    }
                }
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        if (logProjectInstance().isDebugEnabled()) {
            logProjectInstance().debug("最终结果：", Pair.of("secondLevelNode", JSONObject.toJSONString(secondLevelNode)));
        }
    }


    /**
     * 简化系统图查询参数
     *
     * @param systemObjectId    对象id
     * @param diagramBaseObject 基准对象
     * @param graphTypeCode     图类型
     * @return 系统图信息
     */
    private List<SystemGraph> getSystemList(String systemObjectId, String diagramBaseObject, String graphTypeCode) {
        List<SystemGraph> noDataPoBySystemObjectIdAndClassCode = getNoDataPoBySystemObjectIdAndClassCode(
                systemObjectId,
                diagramBaseObject,
                graphTypeCode
        );
        return noDataPoBySystemObjectIdAndClassCode.stream().filter(x -> Objects.nonNull(x.getSystemGraphDuplicateId())).collect(Collectors.toList());
    }


    /**
     * 获取三级节点信息
     *
     * @param thirdLevelNode     三级节点
     * @param configVo           菜单vo
     * @param imageOrProductName 图名
     * @param system             系统信息
     */
    private void getThreeNode(TreeNodeVO thirdLevelNode, MenuConfigVo configVo,
                              String imageOrProductName, DtpRwdObjectDTO system) {
        thirdLevelNode.setImageOrProductName(imageOrProductName);
        thirdLevelNode.setDiagramBaseObject(configVo.getDiagramBaseObject());
        thirdLevelNode.setProductModule(configVo.getProductModule());
        String dynamicName = configVo.getDynamicName();
        thirdLevelNode.setDynamicName(dynamicName);
        if (Objects.nonNull(system)) {
            thirdLevelNode.setSystemId(system.getId());
            thirdLevelNode.setLocalId(system.getLocalId());
            thirdLevelNode.setClassCode(system.getClassCode());
            thirdLevelNode.setObjType(system.getObjType());
            Map<String, Object> undefinedProperties = system.getUndefinedProperties();
            if (MapUtils.isNotEmpty(undefinedProperties)) {
                if (undefinedProperties.containsKey(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value())) {
                    thirdLevelNode.setMonitoringSurveillanceStatus(String.valueOf(system
                            .analyzeProperties(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value())));
                }
                boolean contains = undefinedProperties.containsKey(InfoCodeEnum.IS_COOL_STORE.value());
                if (contains) {
                    thirdLevelNode.setIsCoolStore(undefinedProperties.get(InfoCodeEnum.IS_COOL_STORE.value()));
                    thirdLevelNode.setIsShowCoolStore("1".equals(system.analyzeProperties(InfoCodeEnum.IS_COOL_STORE.value())));
                } else {
                    thirdLevelNode.setIsShowCoolStore(false);
                }
                thirdLevelNode.setChillPumpType(undefinedProperties.get(InfoCodeEnum.CHILL_PUMP_TYPE.value()));
            }
        }
        if (DtpRwdObjectDTO.Fields.localName.equals(dynamicName)) {
            thirdLevelNode.setLocalName(system.getLocalName());
        } else {
            thirdLevelNode.setLocalName(configVo.getThreeLevelMenu());
        }

        thirdLevelNode.setGraphTypeCode(configVo.getGraphTypeCode());
        thirdLevelNode.setIsControl(configVo.getIsControl());
    }


    /**
     * 过滤掉空设备图
     *
     * @param secondLevelNode 二级节点
     * @param result          系统图
     * @param thirdLevelNode  三级节点
     */
    private void filterOutTheEmptyDeviceDiagram(TreeNodeVO secondLevelNode,
                                                List<SystemGraph> result,
                                                TreeNodeVO thirdLevelNode) {
        ArrayList<SystemGraphInstanceVo> systemGraphInstanceVos = new ArrayList<>();
        result.forEach(system -> {
            SystemGraphInstanceVo systemGraphInstanceVo = querySystemGraphDataNoProcess(system.getId());
            if (Objects.nonNull(systemGraphInstanceVo)) {
                // 空调冷源显示系统的localName，这里不覆盖
                if (!DtpRwdSystemClassCodeEnum.ACAC.getValue().equals(thirdLevelNode.getClassCode())
                        && !DtpRwdClassCodeEnum.ACAI_SYSTEM.value().equals(thirdLevelNode.getClassCode())
                        && !DtpRwdClassCodeEnum.ACAH_SYSTEM.value().equals(thirdLevelNode.getClassCode())
                ) {
                    thirdLevelNode.setLocalName(system.getGraphName());
                }
                systemGraphInstanceVos.add(systemGraphInstanceVo);
            }
        });
        // 过滤掉设备和设备组非空的设备
        List<SystemGraphInstanceVo> collect = systemGraphInstanceVos.stream()
                .filter(x -> Objects.nonNull(x.getEquipmentNodeVos()) && Objects.nonNull(x.getGroupVos())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return;
        }
        if (Objects.nonNull(thirdLevelNode)) {
            // 使用同步块保护集合修改操作，防止并发修改异常
            synchronized (secondLevelNode.getChildren()) {
                secondLevelNode.getChildren().add(thirdLevelNode);
            }
        }
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "关系图消费侧实现";
    }
}

