package com.persagy.apccontrol.basic.service.lighting;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.context.lighting.CommonLightingCircuitDetailContext;
import com.persagy.apccontrol.basic.model.context.lighting.CommonLightingSpaceDetailContext;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.*;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionQueryTimeSlotDto;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingLampFuncTypeTagDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.InfoProperty;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.model.entity.intervention.TimeSlotEntity;
import com.persagy.apccontrol.basic.model.enumtype.DemandInfoCodeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.DemandSourceEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeControlDeviationStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeRunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingAbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingInterventionTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.lighting.LightingRunningDeviationTypeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.DashBoardQueryCountParam;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandTreeQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateDateSettingQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.lighting.*;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.KeyValueChildrenVO;
import com.persagy.apccontrol.basic.model.vo.KeyValueVO;
import com.persagy.apccontrol.basic.model.vo.ProjectVO;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingCountDataVo;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingTreeObjectVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.model.vo.knowledge.DtpKnowledgeInfoCdeDataSourceVO;
import com.persagy.apccontrol.basic.model.vo.lighting.*;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.IotDataCommon;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdTwinObjectRuleQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdTwinObjectRuleResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigLightingResultService;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingResultQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandConfigLightingComputeService;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingQueryService;
import com.persagy.apccontrol.basic.service.intervention.InterventionConfigQueryService;
import com.persagy.apccontrol.basic.service.interventiontask.LightingInterventionTaskConfigHandler;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingOpenCloseEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.scene.DemandSceneGateway;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.InterventionConfigQueryInfrastructure;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.param.InterventionConfigQueryParam;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.InterventionConfigModel;
import com.persagy.apccontrol.basic.structure.knowledge.converter.DtpKnowledgeConverter;
import com.persagy.apccontrol.basic.structure.lighitng.converter.LightingConverter;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.validate.utility.pattern.DatePattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 照明服务实现
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Slf4j
@Validated
@SuppressWarnings({"rawtypes", "SpringJavaInjectionPointsAutowiringInspection"})
public abstract class AbstractLightingService implements LightingService, BizComponent<LightingService> {

    private static final String NEW_CLOSE = DemandLightingOpenCloseEnum.NEW_CLOSE.value();
    private static final String NEW_OPEN = DemandLightingOpenCloseEnum.NEW_OPEN.value();

    @Autowired
    private Executor executor;
    @Autowired
    private DtpKnowledgeConverter dtpKnowledgeConverter;
    @Autowired
    private DtpKnowledgeClientService dtpKnowledgeClientService;
    @Autowired
    private DemandLightingStrategy demandLightingStrategy;
    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    @Autowired
    private DtpRwdConvert dtpRwdConvert;
    @Autowired
    private LightingConverter lightingConverter;
    @Autowired
    private DemandLightingQueryService demandLightingQueryService;
    @Autowired
    private DemandConfigLightingResultService demandConfigLightingResultService;
    @Autowired
    private DemandCalendarQueryService demandCalendarQueryService;
    @Autowired
    private DemandConfigLightingComputeService interventionConfigResourceService;
    @Autowired
    private InterventionConfigQueryService<?> interventionConfigQueryService;
    @Autowired
    private DemandConfigLightingComputeService demandConfigLightingComputeService;
    @Autowired
    private AbstractCommonServer abstractCommonServer;
    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;
    @Autowired
    private AbstractIotDataCacheServer abstractIotDataCacheServer;
    @Autowired
    private InterventionConfigQueryInfrastructure<? extends InterventionConfigData> interventionConfigQueryInfrastructure;
    @Autowired
    private DemandSceneGateway demandSceneGateway;
    @Autowired
    private LightingInterventionTaskConfigHandler lightingInterventionTaskConfigHandler;

    @Override
    public LihgtingDemandTargetVO getDemandTarget(LightingDemandTargetQueryParam param) {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        DemandTemplateTypeEnum templateType = runningMonitorSecondType.toDemandTemplateTypeEnum();
        DemandInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();

        StopWatch stopWatch = StopWatch.create();

        // 查询照明回路
        stopWatch.start("查询照明回路");
        List<DtpRwdCircuitDTO> circuits = this.lightingInterventionTaskConfigHandler.queryCircuits(
                templateType, instanceType, Sets.newHashSet(instanceId));
        stopWatch.stop();
        if (CollectionUtils.isEmpty(circuits)) {
            return LihgtingDemandTargetVO.empty();
        }
        List<String> circuitIds = circuits.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toList());
        Map<String, String> circuitId2FuncType = circuits.stream()
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdCircuitDTO::getFuncType));

        LihgtingDemandTargetVO result = new LihgtingDemandTargetVO();
        List<LihgtingDemandTargetVO.Scene> scenes = Collections.emptyList();
        LocalDate queryDate = param.getDate() != null ? param.getDate() : DateUtils.now().toLocalDate();

        // 查询需求场景配置
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(templateType);
        templateParam.setDate(queryDate);
        stopWatch.start("查询需求模版");
        DemandTemplate template = templateParam.queryAndGetTemplate();
        stopWatch.stop();
        if (template != null) {
            Long templateId = template.getId();
            result.setTemplateId(templateId);
            result.setTemplateName(template.getDemandTemplateName());
            result.setTemplateType(template.getDemandTemplateType());
            result.setTemplateTag(template.getDemandTemplateTag());
            stopWatch.start("查询需求场景");
            scenes = this.queryScenes(template, queryDate, circuitIds, circuitId2FuncType);
            stopWatch.stop();
        }

        // 查询临时控制配置
        stopWatch.start("查询临时控制配置");
        List<InterventionTaskConfigVO> tasks = this.queryInterventionTasks(
                runningMonitorSecondType, instanceType, instanceId, circuits, queryDate);
        stopWatch.stop();

        // 根据场景和任务配置填充功能类型到回路配置的映射
        stopWatch.start("计算功能类型到回路配置的映射");
        List<LihgtingDemandTargetVO.TimeFuncTypeConfig> timeFuncTypeConfigs =
                this.fillFuncType2CircuitSetting(circuits, scenes, tasks, queryDate);
        stopWatch.stop();
        this.globalStopWatch().addTask(stopWatch);

        result.setTasks(tasks);
        result.setScenes(scenes);
        result.setFuncTypeConfigs(timeFuncTypeConfigs);
        return result;
    }

    @Override
    public LightingCountDataVo dashboard(DashBoardQueryCountParam param) {
        String projectId = ContextUtil.getProjectId();
        LightingCountDataVo result = new LightingCountDataVo();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        result.setTemplateType(templateType);
        // 1、 获取所有回路
        List<DtpRwdSpaceDTO> spaces = demandLightingStrategy.listSpacesByCircuit(
                projectId, templateType, null, null);
        if (CollectionUtils.isEmpty(spaces)) {
            return result;
        }
        Set<String> circuitIds = DtpRwdSpaceDTO.mapRelObjIdAndData(spaces).keySet();

        Map<String, DemandStatusDTO> mapCircuitIdAndDemandStatus = mapCircuitIdAndDemandStatus(projectId, templateType, circuitIds);

        // 2、回路IOT
        Map<String, Map<String, Object>> circuitIdAndInfoCodeValues = mapCircuitIdAndInfoCodeValues(circuitIds,
                InfoCodeEnum.RUN_STATUS.value(), InfoCodeEnum.CONTROL_DEVIATION_STATUS.value());

        // 回路id根据空间分组
        Map<String, Set<String>> spaceId2circuitIdsMap = groupCircuitBySpace(spaces);

        // 3、获取建筑楼层空间树
        DemandTreeQueryParam demandTreeQueryParam = new DemandTreeQueryParam();
        demandTreeQueryParam.setProjectId(projectId);
        demandTreeQueryParam.setTemplateType(templateType);
        DemandLightingTreeObjectVO demandLightingTreeObjectVO = demandLightingQueryService.treeObject(demandTreeQueryParam);

        // 4、结果构建
        result.resultCalculate(demandLightingTreeObjectVO, spaceId2circuitIdsMap, mapCircuitIdAndDemandStatus, circuitIdAndInfoCodeValues);
        return result;
    }


    /**
     * 回路id根据空间分组
     *
     * @param dtpRwdSpaceDTOList 空间信息
     * @return {@code Map<String,Set<String>>}
     * @author yuecaipu
     * @since V5_241201
     */
    private Map<String, Set<String>> groupCircuitBySpace(List<DtpRwdSpaceDTO> dtpRwdSpaceDTOList) {
        Map<String, Set<String>> spaceId2circuitIdsMap = new HashMap<>();
        for (DtpRwdSpaceDTO dtpRwdSpaceDTO : dtpRwdSpaceDTOList) {
            Set<String> circuitIds = dtpRwdSpaceDTO.getRelObjIds();
            if (CollectionUtils.isEmpty(circuitIds)) {
                continue;
            }
            spaceId2circuitIdsMap.put(dtpRwdSpaceDTO.getId(), circuitIds);
        }
        return spaceId2circuitIdsMap;
    }

    @Override
    public LightingSpaceDetailVO getSpaceDetail(@NonNull LightingSpaceDetailQueryParam param) {
        CommonLightingSpaceDetailContext context = initSpaceDetailContext(param);
        fillAbnormalEvents(context);
        fillDynamicInfoCodeConfig(context.getCircuitDetailContext());

        DtpRwdSpaceDTO space = context.getSpace();
        DtpRwdFloorDTO floor = context.getFloor();
        LightingSpaceTagDTO spaceTag = context.getSpaceTag();

        LightingSpaceDetailVO result = lightingConverter.toSpaceDetailVo(space);
        result.setTag(lightingConverter.toSpaceTagVo(spaceTag));
        result.setRelFloor(dtpRwdConvert.toObjectVo(floor));
        result.setRelCircuits(context.buildCircuitDetails());
        result.setRelControllers(context.buildControllerDetails());
        return result;
    }

    @Override
    public LightingCircuitInfoCodeVO getCircuitInfoCode(@NonNull LightingCircuitInfoCodeQueryParam param) {
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();

        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuits(projectId, templateType, null);

        Set<String> existsFuncTypes = new HashSet<>();
        Set<String> existsLampFuncTypes = new HashSet<>();
        // 空灯具类型关联的功能类型
        Set<String> emptyLampFuncTypeRelFuncTypes = new HashSet<>();
        // 功能类型和灯具类型映射
        Map<String, Set<String>> funcTypeAndLampFuncTypes = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));
        for (DtpRwdCircuitDTO circuit : circuits) {
            String funcType = circuit.getFuncType();
            String lampFuncType = circuit.getLampFuncType();

            existsFuncTypes.add(funcType);
            if (StringUtils.isNotBlank(lampFuncType)) {
                existsLampFuncTypes.add(lampFuncType);
                MapExecutor.dynamicPut(funcTypeAndLampFuncTypes, funcType, lampFuncType,
                        () -> new TreeSet<>(Comparator.comparingInt(Integer::parseInt)));
            } else {
                emptyLampFuncTypeRelFuncTypes.add(funcType);
            }
        }
        List<KeyValueChildrenVO<KeyValueVO>> funcTypeRelLampFuncTypes = new ArrayList<>();
        Map<String, String> funcTypeInfoCodeAndName = demandLightingStrategy
                .mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.funcType);
        Map<String, String> lampFuncTypeInfoCodeAndName = demandLightingStrategy
                .mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.lampFuncType);
        funcTypeAndLampFuncTypes.forEach((funcType, relLampFuncTypes) -> {
            List<KeyValueVO> children = relLampFuncTypes.stream()
                    .map(item -> new KeyValueVO(item, lampFuncTypeInfoCodeAndName.get(item)))
                    .collect(Collectors.toList());
            if (emptyLampFuncTypeRelFuncTypes.contains(funcType)) {
                DtpKnowledgeInfoCdeDataSourceVO emptyDataSource = DtpKnowledgeInfoCdeDataSourceVO.EMPTY;
                children.add(new KeyValueVO(emptyDataSource.getCode(), emptyDataSource.getName()));
            }
            KeyValueChildrenVO<KeyValueVO> infoCodeKeyValue = new KeyValueChildrenVO<>();
            infoCodeKeyValue.setKey(funcType);
            infoCodeKeyValue.setValue(funcTypeInfoCodeAndName.get(funcType));
            infoCodeKeyValue.setChildren(children);
            funcTypeRelLampFuncTypes.add(infoCodeKeyValue);
        });

        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndData = dtpKnowledgeClientService.listInfoCodes(
                        true, false, DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value()).stream()
                .collect(Collectors.toMap(DtpKnowledgeInfoCodeDTO::getCode, Function.identity(), (o1, o2) -> o1));

        InfoCodeDefineModel lampFuncType = toInfoCodeVO(infoCodeAndData,
                DtpRwdCircuitDTO.Fields.lampFuncType, existsLampFuncTypes);
        if (CollectionUtils.isNotEmpty(emptyLampFuncTypeRelFuncTypes)) {
            lampFuncType.getDataSources().add(InfoCodeDataSourceDefineModel.EMPTY);
        }

        LightingCircuitInfoCodeVO result = new LightingCircuitInfoCodeVO();
        result.setFuncType(toInfoCodeVO(infoCodeAndData,
                DtpRwdCircuitDTO.Fields.funcType, existsFuncTypes));
        result.setLampFuncType(lampFuncType);
        result.setFuncTypeAndLampFuncTypes(funcTypeRelLampFuncTypes);
        return result;
    }

    /**
     * 转换信息点VO
     *
     * @param infoCodeAndData      信息点 -> 信息点定义
     * @param infoCode             信息点
     * @param filterInfoCodeValues 过滤信息点值, 非 {@code null} 时进行过滤
     * @return 信息点VO
     * @since 5.240402.0
     */
    private InfoCodeDefineModel toInfoCodeVO(
            @NonNull Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndData,
            @NonNull String infoCode, @Nullable Collection<String> filterInfoCodeValues) {
        DtpKnowledgeInfoCodeDTO data = infoCodeAndData.get(infoCode);
        if (Objects.nonNull(filterInfoCodeValues)) {
            data.getDataSources().removeIf(item -> !filterInfoCodeValues.contains(item.getCode()));
        }
        return dtpKnowledgeConverter.toInfoCodeDefineModel(data);
    }


    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public LightingSidebarDemandChangeVO getDemandChange(LightingInstanceQueryParam instanceQueryParam) {
        String projectId = instanceQueryParam.getProjectId();
        DemandTemplateTypeEnum templateType = instanceQueryParam.getDemandTemplateType();

        // =====1、查询今日配置时间区间=====
        LocalDateTime now = DateUtils.now();
        InterventionQueryTimeSlotDto timeSlotDto = new InterventionQueryTimeSlotDto();
        timeSlotDto.setInstanceId(instanceQueryParam.getInstanceId());
        timeSlotDto.setInstanceType(instanceQueryParam.getInstanceType());
        timeSlotDto.setRunMonitorSecondType(templateType.toRunningMonitorSecondTypeEnum());
        timeSlotDto.setBeginDateTime(now.withHour(0).withMinute(0).withSecond(0).withNano(0));
        timeSlotDto.setEndDateTime(timeSlotDto.getBeginDateTime().plusDays(1));
        List<TimeSlotEntity> aggregationTimeSlot = interventionConfigQueryService.queryDemandTimeSlot(timeSlotDto);

        // 为了减少计算量，aggregationTimeSlot 只保留当前时间及其前后各一个的时间区间
        List<TimeSlotEntity> filteredTimeSlots = new ArrayList<>();
        for (TimeSlotEntity timeSlotEntity : aggregationTimeSlot) {
            if (timeSlotEntity.getBeginDateTime().isBefore(now) && timeSlotEntity.getEndDateTime().isAfter(now)) {
                // 找到符合条件的时间段，并记录其索引
                int index = aggregationTimeSlot.indexOf(timeSlotEntity);

                // 根据索引截取子列表
                List<TimeSlotEntity> subList = aggregationTimeSlot.subList(
                        Math.max(0, index - 1),
                        Math.min(aggregationTimeSlot.size(), index + 2)
                );

                // 将子列表添加到新的列表中
                filteredTimeSlots.addAll(subList);
            }
        }
        // 最后将过滤后的列表赋值给原始列表
        aggregationTimeSlot.clear();
        aggregationTimeSlot.addAll(filteredTimeSlots);

        // =====2、查询自动需求配置=====
        List<DemandSceneEntity> lightingSceneList = demandSceneGateway.listValidScenesByDate(templateType, now.toLocalDate());
        Map<Long, DemandSceneEntity> sceneId2SceneMap = lightingSceneList.stream().collect(Collectors.toMap(DemandSceneEntity::getId, Function.identity()));
        // -----获取当前视角下所有回路-----
        List<DtpRwdCircuitDTO> dtpRwdCircuitList = demandLightingStrategy.listCircuitsByInstance(
                projectId, templateType, instanceQueryParam.getInstanceType(), instanceQueryParam.getInstanceId());
        List<String> circuitIdList = dtpRwdCircuitList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());

        // -----获取所有场景下需求设备状态-----
        DemandConfigLightingResultQueryDaoParam param = new DemandConfigLightingResultQueryDaoParam();
        param.setRelDemandLightingSceneIds(sceneId2SceneMap.keySet());
        param.setCircuitIds(circuitIdList);
        List<DemandConfigLightingResult> lightingResultList = demandConfigLightingResultService.list(projectId, param);
        Map<Long, List<DemandConfigLightingResult>> result = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(lightingResultList)) {
            // 按模板id分组，再按场景id分组，再根据需求配置的开关状态分组后统计数量
            result = lightingResultList.stream()
                    .collect(Collectors.groupingBy(DemandConfigLightingResult::getRelDemandLightingSceneId));
        }

        // ======3、计算各时间区间回路配置结果=====
        List<LightingSidebarDemandResult> lightingSceneResultList = new ArrayList<>();
        for (TimeSlotEntity timeSlotEntity : aggregationTimeSlot) {
            String sceneName;
            List<DemandConfigLightingResult> configLightingResultList;
            boolean isCurrentScene = timeSlotEntity.getBeginDateTime().isBefore(now) && timeSlotEntity.getEndDateTime().isAfter(now);

            if (null == timeSlotEntity.getConfigId()) {
                // 无需求
                sceneName = LocaleMessageUtil.getMessage("key_20f8d693aabf483abb5e7efb58e6c69e");
                configLightingResultList = new ArrayList<>();
            } else if (!sceneId2SceneMap.containsKey(timeSlotEntity.getConfigId())) {
                // 干预需求  干预配置ID赋值给场景ID
                sceneName = LocaleMessageUtil.getMessage("key_2d1146bb44e24f99a44ea9352604939d");
                configLightingResultList = demandConfigLightingComputeService
                        .interventionResultCompute(projectId, templateType.toRunningMonitorSecondTypeEnum(), isCurrentScene ? now : timeSlotEntity.getBeginDateTime())
                        .stream().filter(o -> circuitIdList.contains(o.getCircuitId())).collect(Collectors.toList());
            } else {
                // 自动需求
                sceneName = sceneId2SceneMap.get(timeSlotEntity.getConfigId()).getSceneName();
                // 自动需求配置
                List<DemandConfigLightingResult> autoConfigList = result.getOrDefault(timeSlotEntity.getConfigId(), new ArrayList<>());
                // 查询下级的干预需求配置
                configLightingResultList = demandConfigLightingComputeService
                        .interventionResultCompute(projectId, templateType.toRunningMonitorSecondTypeEnum(), isCurrentScene ? now : timeSlotEntity.getBeginDateTime())
                        .stream().filter(o -> circuitIdList.contains(o.getCircuitId())).collect(Collectors.toList());

                // 合并干预需求和自动需求配置结果  干预需求配置结果优先自动需求
                List<String> interventionCircuitIdList = configLightingResultList.stream().map(DemandConfigLightingResult::getCircuitId).collect(Collectors.toList());
                for (DemandConfigLightingResult demandConfigLightingResult : autoConfigList) {
                    if (!interventionCircuitIdList.contains(demandConfigLightingResult.getCircuitId())) {
                        configLightingResultList.add(demandConfigLightingResult);
                    }
                }
            }

            // noinspection ExtractMethodRecommender
            LightingSidebarDemandResult lightingSidebarDemandResult = new LightingSidebarDemandResult();
            lightingSidebarDemandResult.setSceneName(sceneName);
            lightingSidebarDemandResult.setSceneId(timeSlotEntity.getConfigId());
            lightingSidebarDemandResult.setDemandConfigLightingResultList(configLightingResultList);
            lightingSidebarDemandResult.setBeginDateTime(timeSlotEntity.getBeginDateTime());
            lightingSidebarDemandResult.setEndDateTime(timeSlotEntity.getEndDateTime());
            lightingSceneResultList.add(lightingSidebarDemandResult);
        }


        // =====4、计算场景切换的开启关闭数量=====
        openOffResultCompute(lightingSceneResultList);

        // =====5、查询当日运行模式=====
        DemandTemplateVO relTemplate = queryCurrentDemandTemplate(templateType);
        // 重组数据返回
        LightingSidebarDemandChangeVO lightingSidebarDemandChangeVO = new LightingSidebarDemandChangeVO();
        if (Objects.nonNull(relTemplate)) {
            lightingSidebarDemandChangeVO.setRelTemplate(relTemplate);
        }

        lightingSidebarDemandChangeVO.setLightingSidebarDemandList(lightingSceneResultList);
        return lightingSidebarDemandChangeVO;
    }


    @Override
    @SuppressWarnings({"AlibabaUndefineMagicConstant", "AlibabaMethodTooLong"})
    public LightingSidebarDeviceStateVO countEquipmentStatus(LightingInstanceQueryParam instanceQueryParam) {
        String projectId = instanceQueryParam.getProjectId();
        String instanceId = instanceQueryParam.getInstanceId();
        DemandInstanceTypeEnum instanceType = instanceQueryParam.getInstanceType();

        List<String> objectIdList = new ArrayList<>();
        // 照明回路
        DemandTemplateTypeEnum templateType = instanceQueryParam.getDemandTemplateType();
        List<DtpRwdCircuitDTO> dtpRwdCircuitDtoList = demandLightingStrategy.listCircuitsByInstance(
                projectId, templateType, instanceType, instanceId);
        if (CollectionUtils.isNotEmpty(dtpRwdCircuitDtoList)) {
            objectIdList.addAll(dtpRwdCircuitDtoList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
        }
        // 控制模块
        List<DtpRwdObjectDTO> dtpRwdObjectList = demandLightingStrategy.listControllersByCircuit(
                projectId, templateType, objectIdList, Lists.newArrayList());
        if (CollectionUtils.isNotEmpty(dtpRwdObjectList)) {
            objectIdList.addAll(dtpRwdObjectList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
        }

        return countEquipmentStatus(projectId, objectIdList);
    }


    @Override
    public List<ExceptionEventBaseInfoVO> getAbnormalDataList(LightingInstanceQueryParam instanceParam) {
        LightingInstanceStatusQueryParam lightingInstanceStatusQueryParam = new LightingInstanceStatusQueryParam();
        lightingInstanceStatusQueryParam.setProjectId(instanceParam.getProjectId());
        lightingInstanceStatusQueryParam.setInstanceId(instanceParam.getInstanceId());
        lightingInstanceStatusQueryParam.setInstanceType(InstanceTypeEnum.analyze(instanceParam.getInstanceType().getValue()));
        lightingInstanceStatusQueryParam.setDemandTemplateType(instanceParam.getDemandTemplateType());
        List<String> objectIdList = getCircuitAndControllerIdList(lightingInstanceStatusQueryParam);
        List<ExceptionEventBaseInfoVO> exceptionEventBaseInfos =
                abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(objectIdList, null);
        if (CollectionUtils.isEmpty(exceptionEventBaseInfos)) {
            return new ArrayList<>();
        }

        return exceptionEventBaseInfos;
    }

    @Override
    public List<DtpRwdSpaceDTO> getLineOfFlight(LightingInstanceQueryParam lightingInstanceQueryParam) {
        lightingInstanceQueryParam.checkAndSet();
        String projectId = lightingInstanceQueryParam.getProjectId();
        DemandTemplateTypeEnum templateType = lightingInstanceQueryParam.getDemandTemplateType();
        String instanceId = lightingInstanceQueryParam.getInstanceId();
        DemandInstanceTypeEnum instanceType = lightingInstanceQueryParam.getInstanceType();

        List<DtpRwdSpaceDTO> result = new ArrayList<>();

        // 查询照明回路
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuitsByInstance(
                projectId, templateType, instanceType, instanceId);
        if (CollectionUtils.isNotEmpty(circuits)) {
            // 查询照明回路关联的控制模块
            Set<String> circuitIds = circuits.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());
            Map<String, Set<DtpRwdCircuitDTO>> controllerIdAndCircuits = demandLightingStrategy
                    .mapControllerIdAndCircuits(projectId, templateType, null, circuitIds);
            if (MapUtils.isNotEmpty(controllerIdAndCircuits)) {
                // 查询控制模块所在空间
                List<DtpRwdSpaceDTO> spacesByController = demandLightingStrategy.listSpacesByController(
                        projectId, templateType, controllerIdAndCircuits.keySet(), null);
                if (CollectionUtils.isNotEmpty(spacesByController)) {
                    result.addAll(spacesByController);
                }
            }
        }

        // 查询控制模块
        List<DtpRwdObjectDTO> controllers = demandLightingStrategy.listControllersByInstance(
                projectId, templateType, instanceType, instanceId);
        if (CollectionUtils.isNotEmpty(controllers)) {
            // 查询控制模块关联的照明回路
            Set<String> controllerIds = controllers.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            Map<String, DtpRwdObjectDTO> circuitIdAndControllers = demandLightingStrategy
                    .mapCircuitIdAndController(projectId, templateType, null, controllerIds);
            if (MapUtils.isNotEmpty(circuitIdAndControllers)) {
                // 查询照明回路服务空间
                List<DtpRwdSpaceDTO> spacesByCircuit = demandLightingStrategy.listSpacesByCircuit(
                        projectId, templateType, circuitIdAndControllers.keySet(), null);
                if (CollectionUtils.isNotEmpty(spacesByCircuit)) {
                    result.addAll(spacesByCircuit);
                }
            }
        }
        return new ArrayList<>(result.stream()
                .filter(item -> !StringUtils.equals(item.getId(), instanceId))
                .collect(Collectors.toMap(DtpRwdSpaceDTO::getId, Function.identity(), (o1, o2) -> o1)).values());
    }


    /**
     * hover空间查照明回路和照明控制模块的异常数据
     *
     * @param hoverQueryParam 实例类型和id
     * @return 异常数据
     */
    @Override
    @SuppressWarnings({"AlibabaUndefineMagicConstant", "AlibabaMethodTooLong"})
    public LightingHoverSpaceAbnormalDataVO getSpaceOrLampFuncHoverInfo(LightingHoverQueryParam hoverQueryParam) {

        StopWatch stopWatch = StopWatch.create("hover空间查照明回路和照明控制模块的异常数据");

        String groupCode = ContextUtil.getGroupCode();
        String projectId = hoverQueryParam.getProjectId();
        DemandTemplateTypeEnum templateType = hoverQueryParam.getDemandTemplateType();
        String instanceId = hoverQueryParam.getSpaceId();
        String lampFuncType = hoverQueryParam.getLampFuncType();
        boolean lampNotBlack = StringUtils.isNotBlank(lampFuncType);

        LightingHoverSpaceAbnormalDataVO hoverSpaceAbnormalDataVO = new LightingHoverSpaceAbnormalDataVO();
        List<String> objectIdList;
        LightingHoverTagVO lightingHoverTagVO;
        stopWatch.start("获取灯具功能类型标签DTO");
        // 夜景照明与空间不一样的处理逻辑
        if (lampNotBlack) {
            // 照明回路
            List<DtpRwdCircuitDTO> dtpRwdCircuitList = demandLightingStrategy.listCircuits(
                    projectId, templateType, null);
            // 过滤lampFuncType相等的夜景照明的照明回路
            objectIdList = dtpRwdCircuitList.stream()
                    .filter(x -> Objects.equals(x.getLampFuncType(), lampFuncType))
                    .map(DtpRwdObjectDTO::getId)
                    .collect(Collectors.toList());

            // 夜景照明特有标签，统一对象类
            LightingLampFuncTypeTagDTO lightingLampFuncTypeTagDTO = mapLampFuncTypeAndTag(projectId, templateType,
                    Lists.newArrayList(lampFuncType)).getOrDefault(lampFuncType, new LightingLampFuncTypeTagDTO());
            lightingHoverTagVO = lightingConverter.toHoverTagVo(lightingLampFuncTypeTagDTO);
        } else {
            // 通用空间处理逻辑
            LightingInstanceStatusQueryParam instanceParam = new LightingInstanceStatusQueryParam();
            instanceParam.setProjectId(projectId);
            instanceParam.setDemandTemplateType(templateType);
            instanceParam.setInstanceId(instanceId);
            instanceParam.setInstanceType(InstanceTypeEnum.analyze(DemandInstanceTypeEnum.SPACE.getValue()));
            objectIdList = getCircuitAndControllerIdList(instanceParam);

            // 标签统一对象类
            LightingSpaceTagDTO lightingSpaceTagDTO = mapSpaceIdAndTag(projectId, templateType,
                    Lists.newArrayList(instanceId)).getOrDefault(instanceId, new LightingSpaceTagDTO());
            hoverSpaceAbnormalDataVO.setWorkPlaneLux(lightingSpaceTagDTO.getWorkPlaneLux());
            lightingHoverTagVO = lightingConverter.toHoverTagVo(lightingSpaceTagDTO);
        }
        stopWatch.stop();
        stopWatch.start("查询信息点数据对象");
        Map<String, Map<String, Map<String, String>>> groupMapInfo = groupMapInfo(groupCode, projectId, objectIdList);

        // 查询到的结果集重组数据
        ArrayList<JSONObject> deviceInfo = new ArrayList<>();
        if (MapUtils.isNotEmpty(groupMapInfo)) {
            groupMapInfo.forEach((id, infos) -> {
                if (MapUtils.isEmpty(infos)) {
                    return;
                }
                ArrayList<String> infoList = new ArrayList<>();
                JSONObject jsonObject = new JSONObject().fluentPut("id", id);
                infos.forEach((infoCode, valueAndName) -> {
                    if (MapUtils.isEmpty(valueAndName)) {
                        return;
                    }
                    valueAndName.forEach((value, name) -> {
                        if (infoCode.equals(InfoCodeEnum.ONLINE_STATUS.getCode()) && "0".equals(value)) {
                            infoList.add(infoCode);
                        }
                        if (infoCode.equals(InfoCodeEnum.ORDER_FAIL_ALARM.getCode()) && "1".equals(value)) {
                            infoList.add(infoCode);
                        }
                        jsonObject.fluentPut("name", name);
                    });
                });
                if (CollectionUtils.isEmpty(infoList)) {
                    return;
                }
                deviceInfo.add(jsonObject.fluentPut("infoCodeList", infoList));
            });
        }

        // 当查询为空间时加入空间id
        if (!lampNotBlack) {
            objectIdList.add(instanceId);
        }
        stopWatch.stop();
        stopWatch.start("查询异常事件信息");

        // 异常事件信息
        List<ExceptionEventBaseInfoVO> exceptionEventBaseInfos =
                abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(objectIdList, null);

        stopWatch.stop();
        logProjectInstance(projectId).debug("查询空间标签计时器",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));

        hoverSpaceAbnormalDataVO.setTags(lightingHoverTagVO);
        hoverSpaceAbnormalDataVO.setExceptionEventBaseInfoList(exceptionEventBaseInfos);
        hoverSpaceAbnormalDataVO.setDeviceInfo(deviceInfo);
        return hoverSpaceAbnormalDataVO;
    }

    /**
     * 先按id分组，再按信息点分组，再按数值分组，名称为value
     *
     * @param groupCode    集团code
     * @param projectId    项目ID
     * @param objectIdList 对象id
     * @return {@code Map<String,Map<String,Map<String,String>>>} 先按id分组，再按信息点分组，再按数值分组，名称为value
     * @author yuecaipu
     * @since V5_241201
     */
    public abstract Map<String, Map<String, Map<String, String>>> groupMapInfo(String groupCode, String projectId, List<String> objectIdList);


    /**
     * 查询灯具功能类型和标签映射
     *
     * @param projectId     项目ID
     * @param templateType  模版类型
     * @param lampFuncTypes 灯具功能类型
     * @return 空间ID -> 灯具功能类型标签
     * @since 5.240501.0
     */
    public Map<String, LightingLampFuncTypeTagDTO> mapLampFuncTypeAndTag(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> lampFuncTypes) {
        if (CollectionUtils.isEmpty(lampFuncTypes)) {
            return Maps.newHashMap();
        }
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuits(projectId, templateType, null);
        if (CollectionUtils.isEmpty(circuits)) {
            return Maps.newHashMap();
        }
        Map<String, Set<String>> lampFuncTypeAndCircuitIds = new HashMap<>(Math.max(lampFuncTypes.size(), 16));
        List<String> allCircuitIds = new ArrayList<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            String lampFuncType = circuit.getLampFuncType();

            allCircuitIds.add(circuit.getId());
            if (StringUtils.isNotBlank(lampFuncType)) {
                MapExecutor.dynamicPut(lampFuncTypeAndCircuitIds, lampFuncType, circuit.getId(), HashSet::new);
            }
        }

        Map<String, List<IotDataCommon>> circuitIdAndFeedbackInfos = queryObjectIdAndFeedbackInfos(
                projectId, allCircuitIds, Lists.newArrayList(
                        InfoCodeEnum.RUN_STATUS.getCode(),
                        InfoCodeEnum.ONLINE_STATUS.getCode(),
                        InfoCodeEnum.ORDER_FAIL_ALARM.getCode(),
                        InfoCodeEnum.MANUAL_AUTO_STATUS.getCode()));

        // 灯具功能类型 -> 开关状态
        Map<String, DemandLightingTypeSettingModeEnum> lampFuncTypeAndOpenCloseStatus =
                mapTypeAndOpenCloseStatus(projectId, lampFuncTypeAndCircuitIds, circuitIdAndFeedbackInfos);
        // 灯具功能类型 -> 运行偏差类型
        Map<String, Set<LightingRunningDeviationTypeEnum>> lampFuncTypeAndRunningDemandDeviationType =
                mapTypeAndRunningDemandDeviationTypes(projectId, templateType, lampFuncTypeAndCircuitIds);

        // 灯具功能类型 -> 异常状态列表
        Map<String, List<LightingAbnormalStatusTypeEnum>> lampFuncTypeAndAbnormalStatuses =
                mapTypeAndAbnormalStatuses(projectId, templateType, lampFuncTypeAndCircuitIds, false);

        // 灯具功能类型 -> 是否自动
        Map<String, List<LightingInterventionTypeEnum>> lampFuncTypeAndInterventionTypes =
                mapTypeAnInterventionTypes(projectId, templateType, lampFuncTypeAndCircuitIds, false);

        Map<String, LightingLampFuncTypeTagDTO> lampFuncTypeAndTag = new HashMap<>(lampFuncTypeAndCircuitIds.size());
        lampFuncTypeAndCircuitIds.forEach((lampFuncType, circuitIds) -> {
            LightingLampFuncTypeTagDTO tag = new LightingLampFuncTypeTagDTO();
            tag.setLampFuncType(lampFuncType);
            tag.setOpenCloseStatus(lampFuncTypeAndOpenCloseStatus.get(lampFuncType));
            tag.setAbnormalStatuses(lampFuncTypeAndAbnormalStatuses.get(lampFuncType));
            tag.setRunningDeviationTypes(lampFuncTypeAndRunningDemandDeviationType.get(lampFuncType));
            tag.setInterventionTypes(lampFuncTypeAndInterventionTypes.get(lampFuncType));
            lampFuncTypeAndTag.put(lampFuncType, tag);
        });
        return lampFuncTypeAndTag;
    }


    /**
     * 查询类型和开关状态映射
     *
     * @param projectId                 项目ID
     * @param typeAndCircuitIds         类型 -> 关联照明回路ID
     * @param circuitIdAndFeedbackInfos 照明回路ID -> IOT反馈信息, 为空时实时查询
     * @return 类型 -> 开关状态
     * @since 5.240402.0
     */
    private Map<String, DemandLightingTypeSettingModeEnum> mapTypeAndOpenCloseStatus(
            @NonNull String projectId, @NonNull Map<String, Set<String>> typeAndCircuitIds,
            @Nullable Map<String, List<IotDataCommon>> circuitIdAndFeedbackInfos) {
        if (MapUtils.isEmpty(typeAndCircuitIds)) {
            return Maps.newHashMap();
        }
        circuitIdAndFeedbackInfos = Optional.ofNullable(circuitIdAndFeedbackInfos).orElseGet(() -> {
            List<String> allCircuitIds = typeAndCircuitIds.values().stream()
                    .flatMap(Collection::stream).distinct().collect(Collectors.toList());
            return queryObjectIdAndFeedbackInfos(projectId, allCircuitIds,
                    Lists.newArrayList(InfoCodeEnum.RUN_STATUS.getCode()));
        });
        Map<String, DemandLightingTypeSettingModeEnum> result = new LinkedHashMap<>(128);
        for (Map.Entry<String, Set<String>> entry : typeAndCircuitIds.entrySet()) {
            String type = entry.getKey();
            Set<String> circuitIds = entry.getValue();

            DemandLightingTypeSettingModeEnum settingMode = querySettingMode(
                    projectId, circuitIds, circuitIdAndFeedbackInfos);
            if (Objects.nonNull(settingMode)) {
                result.put(type, settingMode);
            }
        }
        return result;
    }

    /**
     * 查询空间ID和空间标签映射
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param spaceIds     空间ID
     * @return 空间ID -> 空间标签
     * @since 5.240402.0
     */
    public Map<String, LightingSpaceTagDTO> mapSpaceIdAndTag(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Maps.newHashMap();
        }
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuitsBySpaceIds(
                projectId, templateType, spaceIds, null, null, null);
        if (CollectionUtils.isEmpty(circuits)) {
            return Maps.newHashMap();
        }
        Map<String, Set<String>> spaceIdAndCircuitIds = new HashMap<>(Math.max(spaceIds.size(), 16));
        for (DtpRwdCircuitDTO circuit : circuits) {
            Set<String> relSpaceIds = circuit.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relSpaceIds)) {
                relSpaceIds.forEach(item -> MapExecutor.dynamicPut(spaceIdAndCircuitIds,
                        item, circuit.getId(), HashSet::new));
            }
        }

        Set<String> allCircuitIds = spaceIdAndCircuitIds.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toSet());
        Map<String, LightingCircuitTagVO> circuitIdAndTag = mapCircuitIdAndTag(projectId, templateType, allCircuitIds);

        // 空间ID -> 开关状态
        AtomicReference<Map<String, DemandLightingTypeSettingModeEnum>> spaceIdAndOpenCloseStatus = new AtomicReference<>(Collections.emptyMap());
        // 空间ID -> 运行偏差类型
        AtomicReference<Map<String, Set<LightingRunningDeviationTypeEnum>>> spaceIdAndRunningDemandDeviationTypes = new AtomicReference<>(Collections.emptyMap());
        // 空间ID -> 异常状态列表
        AtomicReference<Map<String, List<LightingAbnormalStatusTypeEnum>>> spaceIdAndAbnormalStatuses = new AtomicReference<>(Collections.emptyMap());
        // 空间ID -> 干预类型
        AtomicReference<Map<String, List<LightingInterventionTypeEnum>>> spaceIdAndInterventionTypes = new AtomicReference<>(Collections.emptyMap());
        // 空间ID -> 照度值
        AtomicReference<Map<String, String>> spaceIdAndWorkPlaneLuxValues = new AtomicReference<>(Collections.emptyMap());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndOpenCloseStatus.set(
                mapTypeAndOpenCloseStatus(projectId, spaceIdAndCircuitIds)), executor));
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndRunningDemandDeviationTypes.set(
                mapTypeAndRunningDemandDeviationTypes(projectId, templateType, spaceIdAndCircuitIds, circuitIdAndTag)), executor));
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndAbnormalStatuses.set(
                mapTypeAndAbnormalStatuses(projectId, templateType, spaceIdAndCircuitIds, true)), executor));
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndInterventionTypes.set(
                mapTypeAnInterventionTypes(projectId, templateType, spaceIdAndCircuitIds, true)), executor));
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndWorkPlaneLuxValues.set(
                mapSpaceIdAndWorkPlaneLuxValues(spaceIds)), executor));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Map<String, LightingSpaceTagDTO> spaceIdAndTag = new HashMap<>(spaceIdAndCircuitIds.size());
        spaceIdAndCircuitIds.forEach((spaceId, circuitIds) -> {
            LightingSpaceTagDTO tag = new LightingSpaceTagDTO();
            tag.setSpaceId(spaceId);
            tag.setOpenCloseStatus(spaceIdAndOpenCloseStatus.get().get(spaceId));
            tag.setAbnormalStatuses(spaceIdAndAbnormalStatuses.get().get(spaceId));
            tag.setRunningDeviationTypes(spaceIdAndRunningDemandDeviationTypes.get().get(spaceId));
            tag.setInterventionTypes(spaceIdAndInterventionTypes.get().get(spaceId));
            tag.setWorkPlaneLux(spaceIdAndWorkPlaneLuxValues.get().get(spaceId));
            tag.setDemandFeedbackAdjustment(circuitIds.stream().anyMatch(item ->
                    Objects.nonNull(circuitIdAndTag.get(item).getFeedbackAdjustSettingConfig())));
            spaceIdAndTag.put(spaceId, tag);
        });
        return spaceIdAndTag;
    }

    /**
     * 查询空间的照度值
     *
     * @param spaceIds 空间id
     * @return 空间id -> 照度值
     */
    private Map<String, String> mapSpaceIdAndWorkPlaneLuxValues(Collection<String> spaceIds) {
        Map<String, List<IotDataCommon>> queryObjectIdAndFeedbackInfos = queryObjectIdAndFeedbackInfos(
                ContextUtil.getProjectId(),
                spaceIds,
                Lists.newArrayList(InfoCodeEnum.WORK_PLANE_LUX.getCode())
        );

        return Optional.ofNullable(queryObjectIdAndFeedbackInfos)
                .map(map -> map.entrySet()
                        .stream()
                        .filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().get(0).getDataValue(),
                                (existing, replacement) -> existing,
                                () -> new HashMap<>(spaceIds.size())
                        )))
                .orElseGet(HashMap::new);
    }


    /**
     * 查询类型和是否手自动映射
     *
     * @param projectId         项目ID
     * @param templateType      模版类型
     * @param typeAndCircuitIds 类型ID -> 关联照明回路ID
     * @param typeIsSpace       类型是否为空间
     * @return 类型 -> 是否自动
     * @since 5.240402.0
     */
    private Map<String, List<LightingInterventionTypeEnum>> mapTypeAnInterventionTypes(
            @NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Map<String, Set<String>> typeAndCircuitIds, boolean typeIsSpace) {
        Set<String> allCircuitIds = typeAndCircuitIds.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toSet());
        // noinspection unchecked
        Map<String, Map<String, LocalDateTime>> objectIdIotSetInfoCodeAndRecoverTime =
                abstractIotDataCacheServer.queryObjectIdAllIotSetInfoCodeAndRecoverTime(allCircuitIds);

        DemandInstanceTypeEnum instanceType = typeIsSpace ? DemandInstanceTypeEnum.SPACE : DemandInstanceTypeEnum.PROJECT;
        Set<String> filterInstanceIds = typeIsSpace ? typeAndCircuitIds.keySet() : Sets.newHashSet(projectId);
        LocalDateTime now = DateUtils.now();
        InterventionConfigQueryParam queryParam = new InterventionConfigQueryParam();
        queryParam.setRunningMonitorSecondType(templateType.toRunningMonitorSecondTypeEnum());
        queryParam.setInstanceType(instanceType);
        queryParam.setFilterInstanceIds(filterInstanceIds);
        queryParam.setDateTimeContainRange(new DateTimeRangeValueObject(now, now));
        queryParam.setInterventionStatus(InterventionStatusEnum.EFFECTING);
        List<? extends InterventionConfigModel<?>> configModels = interventionConfigQueryInfrastructure.of(queryParam);
        Map<String, ? extends List<? extends InterventionConfigModel<?>>> objectIdAndConfigModels = configModels.stream()
                .collect(Collectors.groupingBy(item -> item.getInstance().getInstanceId()));

        Map<String, List<LightingInterventionTypeEnum>> result = new LinkedHashMap<>(typeAndCircuitIds.size());
        for (Map.Entry<String, Set<String>> entry : typeAndCircuitIds.entrySet()) {
            String type = entry.getKey();
            Set<String> circuitIds = entry.getValue();

            for (String circuitId : circuitIds) {
                boolean containsRecoverTime = objectIdIotSetInfoCodeAndRecoverTime.containsKey(circuitId);
                if (containsRecoverTime) {
                    MapExecutor.dynamicPut(result, type, LightingInterventionTypeEnum.MANUAL_CONTROL, ArrayList::new);
                    break;
                }
            }
            List<? extends InterventionConfigModel<?>> models = objectIdAndConfigModels.get(typeIsSpace ? type : projectId);
            if (CollectionUtils.isNotEmpty(models)) {
                // 如果为空间, 则代表空间具有需求干预
                // 如果为灯具类型, 则解析实际配置中是否存在灯具类型的干预
                Optional<? extends InterventionConfigModel<?>> configModelOpt = models.stream()
                        .filter(InterventionConfigModel::isEffectiveByNow).findFirst();
                if (configModelOpt.isPresent()) {
                    if (typeIsSpace || lampFuncTypeHasIntervention(projectId, type, configModelOpt.get())) {
                        MapExecutor.dynamicPut(result, type, LightingInterventionTypeEnum.DEMAND_INTERVENTION, ArrayList::new);
                    }
                }
            }
        }
        return result;
    }


    /**
     * 灯具功能类型是否存在干预
     *
     * @param projectId    项目ID
     * @param lampFuncType 灯具功能类型
     * @param configModel  干预配置模型
     * @return 灯具功能类型是否存在干预
     * @since 5.240702.0
     */
    private boolean lampFuncTypeHasIntervention(
            @NonNull String projectId,
            @NonNull String lampFuncType,
            @NonNull InterventionConfigModel<?> configModel) {
        DemandConfigLightingDataDTO selfConfig = (DemandConfigLightingDataDTO) configModel.getConfig().getSelfConfig();
        List<DemandLightingFuncTypeSettingDTO> globalSettings = selfConfig.getGlobalSettings();
        if (CollectionUtils.isEmpty(globalSettings)) {
            return false;
        }
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addInfoCode(DtpRwdCircuitDTO.Fields.lampFuncType, lampFuncType);
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT);
        Set<String> circuitIds = new HashSet<>();
        Set<DtpRwdCircuitFuncTypeEnum> funcTypes = new HashSet<>();
        for (DemandLightingFuncTypeSettingDTO globalSetting : globalSettings) {
            DemandLightingTypeSettingModeEnum settingMode = globalSetting.getSettingMode();
            DtpRwdCircuitFuncTypeEnum funcType = globalSetting.getFuncType();
            List<DemandLightingCircuitSettingDTO> circuitSettings = globalSetting.getCircuitSettings();
            if (settingMode == DemandLightingTypeSettingModeEnum.PARTIAL_OPEN) {
                circuitIds.addAll(circuitSettings.stream()
                        .map(DemandLightingCircuitSettingDTO::getCircuitId)
                        .collect(Collectors.toSet()));
            } else {
                funcTypes.add(funcType);
            }
        }
        if (CollectionUtils.isNotEmpty(circuitIds)) {
            queryParam.setId(circuitIds);
        }
        if (CollectionUtils.isNotEmpty(funcTypes)) {
            queryParam.addInfoCode(DtpRwdCircuitDTO.Fields.funcType, funcTypes);
        }
        return dtpRwdClientService.countObject(queryParam) > 0;
    }

    /**
     * 查询类型和异常状态类型映射
     *
     * @param projectId           项目ID
     * @param templateType        模版类型
     * @param typeIdAndCircuitIds 类型 -> 关联照明回路ID
     * @param typeIsSpace         类型是否为空间
     * @return 空间ID -> 异常状态类型
     * @see #setDemandDeviationInAbnormalStatus(String, DemandTemplateTypeEnum, Map, Map, Map)
     * @see #setAbnormalEventInAbnormalStatus(Map, Map, Map, boolean)
     * @see #setEquipmentStatusAbnormalInAbnormalStatus(String, Map, Map, Map)
     * @since 5.240402.0
     */
    private Map<String, List<LightingAbnormalStatusTypeEnum>> mapTypeAndAbnormalStatuses(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Map<String, Set<String>> typeIdAndCircuitIds,
            boolean typeIsSpace) {
        if (MapUtils.isEmpty(typeIdAndCircuitIds)) {
            return Maps.newHashMap();
        }
        Map<String, List<LightingAbnormalStatusTypeEnum>> result = new HashMap<>(typeIdAndCircuitIds.size());
        setDemandDeviationInAbnormalStatus(projectId, templateType, result, typeIdAndCircuitIds, null);
        setAbnormalEventInAbnormalStatus(result, typeIdAndCircuitIds, null, typeIsSpace);
        setEquipmentStatusAbnormalInAbnormalStatus(projectId, result, typeIdAndCircuitIds, null);
        return result;
    }


    /**
     * 设置异常状态类型中的设备状态异常
     *
     * @param projectId                 项目ID
     * @param target                    目标源
     * @param typeAndCircuitIds         类型 -> 关联照明回路ID
     * @param circuitIdAndFeedbackInfos 照明回路ID -> IOT反馈信息列表, 为空时实时查询
     * @return 目标源
     * @since 5.240402.0
     */
    private Map<String, List<LightingAbnormalStatusTypeEnum>> setEquipmentStatusAbnormalInAbnormalStatus(
            @NonNull String projectId,
            @NonNull Map<String, List<LightingAbnormalStatusTypeEnum>> target,
            @NonNull Map<String, Set<String>> typeAndCircuitIds,
            @Nullable Map<String, List<IotDataCommon>> circuitIdAndFeedbackInfos) {
        List<String> allCircuitIds = typeAndCircuitIds.values().stream()
                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
        circuitIdAndFeedbackInfos = Optional.ofNullable(circuitIdAndFeedbackInfos).orElseGet(
                () -> queryObjectIdAndFeedbackInfos(projectId, allCircuitIds, Lists.newArrayList(
                        InfoCodeEnum.ONLINE_STATUS.getCode(), InfoCodeEnum.ORDER_FAIL_ALARM.getCode())));
        for (Map.Entry<String, Set<String>> entry : typeAndCircuitIds.entrySet()) {
            String type = entry.getKey();
            Set<String> circuitIds = entry.getValue();

            List<LightingAbnormalStatusTypeEnum> abnormalStatusTypes = Optional.ofNullable(
                    target.get(type)).orElseGet(ArrayList::new);
            boolean isEquipmentStatusAbnormal = false;
            circuitEach:
            for (String circuitId : circuitIds) {
                List<IotDataCommon> infos = circuitIdAndFeedbackInfos.get(circuitId);
                if (CollectionUtils.isNotEmpty(infos)) {
                    for (IotDataCommon info : infos) {
                        String infoCode = info.getInfoCode();
                        String dataValue = info.getDataValue();
                        if (StringUtils.isAnyBlank(infoCode, dataValue)) {
                            LogExecutor.project(projectId).warn(log(), logTag(), "照明回路反馈值异常",
                                    Collections.singletonList(Pair.of("feedbackInfo", info)));
                            continue;
                        }
                        if (StringUtils.equals(infoCode, InfoCodeEnum.ONLINE_STATUS.getCode())
                                && StringUtils.equals(dataValue, "0")) {
                            isEquipmentStatusAbnormal = true;
                            break circuitEach;
                        }
                        if (StringUtils.equals(infoCode, InfoCodeEnum.ORDER_FAIL_ALARM.getCode())
                                && StringUtils.equals(dataValue, "1")) {
                            isEquipmentStatusAbnormal = true;
                            break circuitEach;
                        }
                    }
                }
            }
            if (isEquipmentStatusAbnormal) {
                abnormalStatusTypes.add(LightingAbnormalStatusTypeEnum.EQUIPMENT_STATUS_ABNORMAL);
            }
            target.put(type, abnormalStatusTypes);
        }
        return target;
    }


    /**
     * 设置异常状态类型中的异常事件
     *
     * @param target            目标源
     * @param typeAndCircuitIds 类型 -> 关联照明回路ID
     * @param typeAndRiskEvents 类型 -> 空间&照明回路异常事件列表, 为空时实时查询
     * @param typeIsSpace       类型是否为空间
     * @return 目标源
     * @since 5.240402.0
     */
    private Map<String, List<LightingAbnormalStatusTypeEnum>> setAbnormalEventInAbnormalStatus(
            @NonNull Map<String, List<LightingAbnormalStatusTypeEnum>> target,
            @NonNull Map<String, Set<String>> typeAndCircuitIds,
            @Nullable Map<String, List<ExceptionEventBaseInfoVO>> typeAndRiskEvents,
            boolean typeIsSpace) {
        typeAndRiskEvents = Optional.ofNullable(typeAndRiskEvents)
                .orElseGet(() -> mapTypeAndRiskEvents(typeAndCircuitIds, typeIsSpace));
        for (Map.Entry<String, Set<String>> entry : typeAndCircuitIds.entrySet()) {
            String type = entry.getKey();
            List<LightingAbnormalStatusTypeEnum> abnormalStatusTypes =
                    Optional.ofNullable(target.get(type)).orElseGet(ArrayList::new);
            if (typeAndRiskEvents.containsKey(type)) {
                abnormalStatusTypes.add(LightingAbnormalStatusTypeEnum.ABNORMAL_EVENT);
            }
            target.put(type, abnormalStatusTypes);
        }
        return target;
    }

    /**
     * 查询类型和异常事件列表
     *
     * @param typeAndCircuitIds 类型 -> 关联照明回路ID, 为空时实时查询
     * @param typeIsSpace       类型是否为空间
     * @return 类型 -> 空间&照明回路异常事件
     * @since 5.240402.0
     */
    private Map<String, List<ExceptionEventBaseInfoVO>> mapTypeAndRiskEvents(
            @NonNull Map<String, Set<String>> typeAndCircuitIds, boolean typeIsSpace) {
        List<String> riskEventListIds = typeAndCircuitIds.values().stream()
                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
        if (typeIsSpace) {
            riskEventListIds.addAll(typeAndCircuitIds.keySet());
        }
        Map<String, List<ExceptionEventBaseInfoVO>> objectIdAndRiskEvents =
                abstractCommonServer.mapRiskEvent(riskEventListIds, null);

        Map<String, List<ExceptionEventBaseInfoVO>> result = new LinkedHashMap<>(typeAndCircuitIds.size());
        typeAndCircuitIds.forEach((type, circuitIds) -> {
            List<ExceptionEventBaseInfoVO> riskEvents = new ArrayList<>();
            if (typeIsSpace) {
                List<ExceptionEventBaseInfoVO> spaceRiskEvents = objectIdAndRiskEvents.get(type);
                if (CollectionUtils.isNotEmpty(spaceRiskEvents)) {
                    riskEvents.addAll(spaceRiskEvents);
                }
            }
            for (String circuitId : circuitIds) {
                List<ExceptionEventBaseInfoVO> circuitRiskEvents = objectIdAndRiskEvents.get(circuitId);
                if (CollectionUtils.isNotEmpty(circuitRiskEvents)) {
                    riskEvents.addAll(circuitRiskEvents);
                }
            }
            if (CollectionUtils.isNotEmpty(riskEvents)) {
                result.put(type, riskEvents);
            }
        });
        return result;
    }

    /**
     * 设置异常状态类型中的需求偏离类型
     *
     * @param projectId                    项目ID
     * @param templateType                 模版类型
     * @param target                       目标源
     * @param typeAndCircuitIds            类型 -> 关联照明回路ID
     * @param typeAndRunningDeviationTypes 类型 -> 需求偏离类型, 为空时实时查询
     * @return 目标源
     * @since 5.240402.0
     */
    private Map<String, List<LightingAbnormalStatusTypeEnum>> setDemandDeviationInAbnormalStatus(
            @NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Map<String, List<LightingAbnormalStatusTypeEnum>> target,
            @NonNull Map<String, Set<String>> typeAndCircuitIds,
            @Nullable Map<String, Set<LightingRunningDeviationTypeEnum>> typeAndRunningDeviationTypes) {
        typeAndRunningDeviationTypes = Optional.ofNullable(typeAndRunningDeviationTypes)
                .orElseGet(() -> mapTypeAndRunningDemandDeviationTypes(projectId, templateType, typeAndCircuitIds));
        for (Map.Entry<String, Set<String>> entry : typeAndCircuitIds.entrySet()) {
            String type = entry.getKey();
            List<LightingAbnormalStatusTypeEnum> abnormalStatusTypes =
                    Optional.ofNullable(target.get(type)).orElseGet(ArrayList::new);
            if (typeAndRunningDeviationTypes.containsKey(type)) {
                abnormalStatusTypes.add(LightingAbnormalStatusTypeEnum.DEMAND_DEVIATION);
            }
            target.put(type, abnormalStatusTypes);
        }
        return target;
    }

    /**
     * 查询类型和需求偏离类型
     *
     * @param projectId         项目ID
     * @param templateType      模版类型
     * @param typeAndCircuitIds 类型 -> 关联照明回路ID
     * @return 类型 -> 需求偏离类型
     * @since 5.240402.0
     */
    private Map<String, Set<LightingRunningDeviationTypeEnum>> mapTypeAndRunningDemandDeviationTypes(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Map<String, Set<String>> typeAndCircuitIds) {
        return mapTypeAndRunningDemandDeviationTypes(projectId, templateType, typeAndCircuitIds, null);
    }

    /**
     * 查询类型和需求偏离类型
     *
     * @param projectId         项目ID
     * @param templateType      模版类型
     * @param typeAndCircuitIds 类型 -> 关联照明回路ID
     * @return 类型 -> 需求偏离类型
     * @since 5.241202.0
     */
    private Map<String, Set<LightingRunningDeviationTypeEnum>> mapTypeAndRunningDemandDeviationTypes(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Map<String, Set<String>> typeAndCircuitIds,
            @Nullable Map<String, LightingCircuitTagVO> circuitIdAndTag) {
        if (MapUtils.isEmpty(typeAndCircuitIds)) {
            return Maps.newHashMap();
        }
        // 查询照明回路标签
        if (Objects.isNull(circuitIdAndTag)) {
            Set<String> allCircuitIds = typeAndCircuitIds.values().stream()
                    .flatMap(Collection::stream).collect(Collectors.toSet());
            circuitIdAndTag = mapCircuitIdAndTag(projectId, templateType, allCircuitIds);
        }
        if (MapUtils.isEmpty(circuitIdAndTag)) {
            return Maps.newHashMap();
        }
        Map<String, Set<LightingRunningDeviationTypeEnum>> result = new LinkedHashMap<>(typeAndCircuitIds.size());
        for (Map.Entry<String, Set<String>> entry : typeAndCircuitIds.entrySet()) {
            String type = entry.getKey();
            Set<String> circuitIds = entry.getValue();

            // 查询需求偏离类型
            Set<LightingRunningDeviationTypeEnum> runningDeviationTypes = queryRunningDeviationType(
                    projectId, templateType, circuitIds, circuitIdAndTag);
            if (CollectionUtils.isNotEmpty(runningDeviationTypes)) {
                result.put(type, runningDeviationTypes);
            }
        }
        return result;
    }

    /**
     * 查询运行偏差类型
     *
     * @param projectId       项目ID
     * @param templateType    模版类型
     * @param circuitIds      照明回路ID
     * @param circuitIdAndTag 照明回路ID -> 照明回路标签, 为空时实时查询
     * @return 运行偏差类型
     * @since 5.240501.0
     */
    private Set<LightingRunningDeviationTypeEnum> queryRunningDeviationType(
            @NonNull String projectId, @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<String> circuitIds, @Nullable Map<String, LightingCircuitTagVO> circuitIdAndTag) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Sets.newHashSet();
        }
        circuitIdAndTag = Optional.ofNullable(circuitIdAndTag).orElseGet(
                () -> mapCircuitIdAndTag(projectId, templateType, circuitIds));
        if (MapUtils.isEmpty(circuitIdAndTag)) {
            return Sets.newHashSet();
        }
        Set<LightingRunningDeviationTypeEnum> runningDeviationTypes = new HashSet<>();
        for (String circuitId : circuitIds) {
            LightingCircuitTagVO circuitTag = circuitIdAndTag.get(circuitId);
            if (Objects.nonNull(circuitTag)) {
                if (Objects.nonNull(circuitTag.getRunningDeviationType())) {
                    runningDeviationTypes.add(circuitTag.getRunningDeviationType());
                }
            }
        }
        return runningDeviationTypes;
    }

    private Map<String, DemandLightingTypeSettingModeEnum> mapTypeAndOpenCloseStatus(
            @NonNull String projectId, @NonNull Map<String, Set<String>> typeAndCircuitIds) {
        if (MapUtils.isEmpty(typeAndCircuitIds)) {
            return Maps.newHashMap();
        }
        List<String> allCircuitIds = typeAndCircuitIds.values().stream()
                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
        Map<String, List<IotDataCommon>> circuitIdAndFeedbackInfos = queryObjectIdAndFeedbackInfos(
                projectId, allCircuitIds, Lists.newArrayList(
                        InfoCodeEnum.RUN_STATUS.getCode(),
                        InfoCodeEnum.ONLINE_STATUS.getCode(),
                        InfoCodeEnum.ORDER_FAIL_ALARM.getCode(),
                        InfoCodeEnum.MANUAL_AUTO_STATUS.getCode()));
        circuitIdAndFeedbackInfos = Optional.ofNullable(circuitIdAndFeedbackInfos).orElseGet(
                () -> queryObjectIdAndFeedbackInfos(projectId, allCircuitIds, Lists.newArrayList(InfoCodeEnum.RUN_STATUS.getCode())));
        Map<String, DemandLightingTypeSettingModeEnum> result = new LinkedHashMap<>(128);
        for (Map.Entry<String, Set<String>> entry : typeAndCircuitIds.entrySet()) {
            String type = entry.getKey();
            Set<String> circuitIds = entry.getValue();

            DemandLightingTypeSettingModeEnum settingMode = querySettingMode(
                    projectId, circuitIds, circuitIdAndFeedbackInfos);
            if (Objects.nonNull(settingMode)) {
                result.put(type, settingMode);
            }
        }
        return result;
    }

    /**
     * 查询开关状态
     *
     * @param projectId                 项目ID
     * @param circuitIds                照明回路ID
     * @param circuitIdAndFeedbackInfos 照明回路ID -> IOT反馈信息, 为空时实时查询
     * @return 开关状态
     * @since 5.240501.0
     */
    private DemandLightingTypeSettingModeEnum querySettingMode(
            @NonNull String projectId, @NonNull Set<String> circuitIds,
            @Nullable Map<String, List<IotDataCommon>> circuitIdAndFeedbackInfos) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return null;
        }
        circuitIdAndFeedbackInfos = Optional.ofNullable(circuitIdAndFeedbackInfos).orElseGet(
                () -> queryObjectIdAndFeedbackInfos(projectId, circuitIds,
                        Lists.newArrayList(InfoCodeEnum.RUN_STATUS.getCode())));
        if (MapUtils.isEmpty(circuitIdAndFeedbackInfos)) {
            return null;
        }
        DemandLightingTypeSettingModeEnum settingMode = null;
        for (String circuitId : circuitIds) {
            List<IotDataCommon> infos = circuitIdAndFeedbackInfos.get(circuitId);
            if (CollectionUtils.isEmpty(infos)) {
                continue;
            }
            for (IotDataCommon info : infos) {
                String infoCode = info.getInfoCode();
                String dataValue = info.getDataValue();
                if (StringUtils.isAnyBlank(infoCode, dataValue)) {
                    LogExecutor.project(projectId).warn(log(), logTag(), "照明回路反馈值异常",
                            Collections.singletonList(Pair.of("feedbackInfo", info)));
                    continue;
                }
                if (StringUtils.equals(infoCode, InfoCodeEnum.RUN_STATUS.getCode())) {
                    if (Objects.isNull(settingMode)) {
                        if (StringUtils.equals("0", dataValue)) {
                            settingMode = DemandLightingTypeSettingModeEnum.ALL_CLOSE;
                        }
                        if (StringUtils.equals("1", dataValue)) {
                            settingMode = DemandLightingTypeSettingModeEnum.ALL_OPEN;
                        }
                        continue;
                    }
                    if (settingMode == DemandLightingTypeSettingModeEnum.ALL_CLOSE
                            && StringUtils.equals("1", dataValue)) {
                        return DemandLightingTypeSettingModeEnum.PARTIAL_OPEN;
                    }
                    if (settingMode == DemandLightingTypeSettingModeEnum.ALL_OPEN
                            && StringUtils.equals("0", dataValue)) {
                        return DemandLightingTypeSettingModeEnum.PARTIAL_OPEN;
                    }
                }
            }
        }
        return Objects.nonNull(settingMode) ? settingMode : DemandLightingTypeSettingModeEnum.ALL_CLOSE;
    }

    /**
     * 查询实例下的照明回路和照明控制模块ID
     *
     * @param instanceParam 查询参数
     * @return 实例下的照明回路和照明控制模块ID
     * @since 5.240402.0
     */
    private List<String> getCircuitAndControllerIdList(LightingInstanceStatusQueryParam instanceParam) {
        String projectId = instanceParam.getProjectId();
        DemandTemplateTypeEnum templateType = instanceParam.getDemandTemplateType();
        String instanceId = instanceParam.getInstanceId();
        List<String> objectIdList = new ArrayList<>();
        String instanceType = instanceParam.getInstanceType().getValue();
        if (!instanceType.equals(InstanceTypeEnum.EQUIPMENT.getValue())) {
            // 照明控制模块
            List<DtpRwdObjectDTO> dtpRwdObjectList = demandLightingStrategy.listControllersByInstance(
                    projectId, templateType, DemandInstanceTypeEnum.analyze(instanceType), instanceId);
            // 照明回路
            List<DtpRwdCircuitDTO> dtpRwdCircuitList = demandLightingStrategy.listCircuitsByInstance(
                    projectId, templateType, DemandInstanceTypeEnum.analyze(instanceType), instanceId);
            objectIdList.addAll(dtpRwdObjectList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
            objectIdList.addAll(dtpRwdCircuitList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
        } else {
            objectIdList.add(instanceId);
        }
        return objectIdList;
    }

    /**
     * 回路、控制模块对象统计
     *
     * @param projectId    项目ID
     * @param objectIdList 回路、控制模块对象集合
     * @return {@link LightingSidebarDeviceStateVO}
     * @author yuecaipu
     * @since v5.240802
     */
    public LightingSidebarDeviceStateVO countEquipmentStatus(String projectId, List<String> objectIdList) {
        // 数据集转对象id
        List<String> infoCodeList = Lists.newArrayList(
                InfoCodeEnum.MANUAL_AUTO_STATUS.getCode(),
                InfoCodeEnum.ONLINE_STATUS.getCode(),
                InfoCodeEnum.ORDER_FAIL_ALARM.getCode()
        );
        Map<String, Map<String, Map<String, Long>>> result = groupByInfoValueResultMap(projectId, objectIdList, infoCodeList);
        LightingSidebarDeviceStateVO lightingSidebarDeviceStateVO = new LightingSidebarDeviceStateVO();
        if (!result.isEmpty()) {
            // 输出结果
            result.forEach((claasCode, infoCodeMap) -> {
                if (claasCode.equals(DtpRwdClassCodeEnum.LIGHTING_CONTROLLER.getValue())) {
                    infoCodeMap.forEach((infoCode, stateMap) -> {
                        if (InfoCodeEnum.ONLINE_STATUS.value().equals(infoCode)) {
                            stateMap.forEach((state, count) -> {
                                if ("0".equals(state)) {
                                    lightingSidebarDeviceStateVO.setControl(
                                            LightingSidebarControlVO.builder().offline(LightingSidebarStatisticalJumpVo.builder().code("offline").number(count).build()).build());
                                }
                            });
                        }
                    });
                } else {
                    LightingSidebarCircuitVO sidebarCircuitVO = new LightingSidebarCircuitVO();
                    infoCodeMap.forEach((infoCode, stateMap) -> {
                        if (InfoCodeEnum.MANUAL_AUTO_STATUS.value().equals(infoCode)) {
                            stateMap.forEach((state, count) -> {
                                if ("0".equals(state)) {
                                    sidebarCircuitVO.setHand(LightingSidebarStatisticalJumpVo.builder().code("hand").number(count).build());
                                } else {
                                    sidebarCircuitVO.setAuto(LightingSidebarStatisticalJumpVo.builder().code("auto").number(count).build());
                                }
                            });
                        }
                        if (InfoCodeEnum.ONLINE_STATUS.value().equals(infoCode)) {
                            stateMap.forEach((state, count) -> {
                                if ("0".equals(state)) {
                                    sidebarCircuitVO.setOffline(LightingSidebarStatisticalJumpVo.builder().code("offline").number(count).build());
                                }
                            });
                        }
                        if (InfoCodeEnum.ORDER_FAIL_ALARM.value().equals(infoCode)) {
                            stateMap.forEach((state, count) -> {
                                if ("1".equals(state)) {
                                    sidebarCircuitVO.setInconformity(LightingSidebarStatisticalJumpVo.builder().code("inconformity").number(count).build());
                                }
                            });
                        }
                    });
                    lightingSidebarDeviceStateVO.setLightingSidebarCircuitVo(sidebarCircuitVO);
                }
            });
        }
        return lightingSidebarDeviceStateVO;
    }

    /**
     * 查询实时数据，并统计
     *
     * @param projectId    项目ID
     * @param objectIdList 回路ID集合
     * @param infoCodeList 信息点集合
     * @return {@code Map<String,Map<String,Map<String,Long>>>} key: classCode:infoCode:dataValue:  value: 聚合统计count
     * @author yuecaipu
     * @since V5_241201
     */
    @Override
    public Map<String, Map<String, Map<String, Long>>> groupByInfoValueResultMap(String projectId, Collection<String> objectIdList, List<String> infoCodeList) {
        // noinspection unchecked
        List<IotDataCommon> objectDataList = this.abstractIotDataCacheServer.queryIotRealDataListByList(ContextUtil.getGroupCode(), projectId, objectIdList, infoCodeList);
        if (CollectionUtils.isNotEmpty(objectDataList)) {
            return objectDataList.stream()
                    .collect(Collectors.groupingBy(
                            IotDataCommon::getClassCode,
                            Collectors.groupingBy(
                                    IotDataCommon::getInfoCode,
                                    Collectors.groupingBy(
                                            IotDataCommon::getDataValue,
                                            Collectors.counting()
                                    )
                            )
                    ));
        }
        return Collections.emptyMap();
    }

    /**
     * 查询当日运行模式
     *
     * @param templateType 模板类型
     * @return DemandTemplateVO
     */
    public DemandTemplateVO queryCurrentDemandTemplate(DemandTemplateTypeEnum templateType) {
        // 当日运行模式
        DemandTemplateDateSettingQueryParam demandDateQueryParam = new DemandTemplateDateSettingQueryParam();
        demandDateQueryParam.setBeginDate(DateUtils.getLocalDateNow());
        demandDateQueryParam.setEndDate(DateUtils.getLocalDateTomorrow());
        demandDateQueryParam.setProjectId(ContextUtil.getProjectId());
        demandDateQueryParam.setTemplateType(templateType);

        List<DemandTemplateDateSettingVO> demandTemplateDateSettingList = demandCalendarQueryService.listDateSetting(demandDateQueryParam);
        if (CollectionUtils.isNotEmpty(demandTemplateDateSettingList)) {
            return demandTemplateDateSettingList.get(0).getRelTemplate();
        }
        return null;
    }

    @Override
    public List<LightingSidebarLightLoopTotalVO> listLightLoopTotal(LightingInstanceQueryParam lightingInstanceQueryParam) {
        String projectId = lightingInstanceQueryParam.getProjectId();

        // 照明回路列表
        DemandTemplateTypeEnum templateType = lightingInstanceQueryParam.getDemandTemplateType();
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuitsByInstance(projectId, templateType,
                lightingInstanceQueryParam.getInstanceType(), lightingInstanceQueryParam.getInstanceId());
        if (CollectionUtils.isEmpty(circuits)) {
            return new ArrayList<>();
        }
        Set<String> circuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        // 场景需求开启关闭
        Map<String, DemandConfigLightingResultDataDTO> circuitIdAndConfigResult =
                mapCircuitIdAndConfigResult(projectId, templateType, circuitIds);

        // iot实际运行状态
        Map<String, Map<String, Object>> circuitIdAndInfoCodeValues = mapCircuitIdAndInfoCodeValues(circuitIds,
                InfoCodeEnum.RUN_STATUS.value(), InfoCodeEnum.CONTROL_DEVIATION_STATUS.value());

        // 统一多开少开
        Map<String, LightingCircuitTagVO> circuitTagMap = mapCircuitIdAndTag(circuitIds, circuitIdAndConfigResult, circuitIdAndInfoCodeValues);

        // 先根据功能类型分组，再按灯具类型分组
        Map<String, Map<String, List<DtpRwdCircuitDTO>>> groupedData =
                circuits.stream().filter(item -> Objects.nonNull(item.getLampFuncType())).collect(
                        Collectors.groupingBy(
                                DtpRwdCircuitDTO::getFuncType,
                                Collectors.groupingBy(
                                        DtpRwdCircuitDTO::getLampFuncType
                                )
                        )
                );
        return listLightLoopTotalByDemand(circuitIdAndInfoCodeValues, circuitTagMap, circuitIdAndConfigResult, groupedData);
    }


    @Override
    public List<LightingSidebarManualAutomaticControlVO> listManualAutomaticControl(LightingInstanceQueryParam lightingInstanceQueryParam) {
        // 1. 获取需求模板类型，并查询关联的照明回路配置数据
        DemandTemplateTypeEnum templateType = lightingInstanceQueryParam.getDemandTemplateType();
        List<DtpRwdCircuitDTO> dtpRwdCircuitDtoList = demandLightingStrategy.listCircuitsByInstance(
                ContextUtil.getProjectId(),  // 从上下文获取当前项目ID
                templateType,  // 需求模板类型（如时间表控制/场景控制等）
                lightingInstanceQueryParam.getInstanceType(),  // 实例类型（如楼层/区域）
                lightingInstanceQueryParam.getInstanceId()  // 具体实例ID
        );
        // 空值保护，无回路配置时直接返回空列表
        if (CollectionUtils.isEmpty(dtpRwdCircuitDtoList)) {
            return new ArrayList<>();
        }

        // 2. 提取所有照明回路的唯一标识列表
        List<String> objectIdList = dtpRwdCircuitDtoList.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toList());

        // 3. 获取IOT设备状态信息（重点获取手动/自动状态码）
        String manualAutoStatusCode = InfoCodeEnum.MANUAL_AUTO_STATUS.getCode();
        // 通过信息编码获取设备状态值的映射（key: 对象ID, value: 状态值）
        Map<String, Map<String, String>> mapInfoCodeObjectValue = mapInfoCodeObjectValue(
                objectIdList,
                Lists.newArrayList(manualAutoStatusCode)
        );
        // 提取手动/自动状态的键值对映射
        Map<String, String> manualAutoStatusMap = mapInfoCodeObjectValue.getOrDefault(
                manualAutoStatusCode,
                new HashMap<>()
        );

        // 4. 对回路数据进行多维度分组：功能类型 -> 灯具类型
        // 数据结构示例：Map<功能类型, Map<灯具类型, 回路列表>>
        Map<String, Map<String, List<DtpRwdCircuitDTO>>> groupedData =
                dtpRwdCircuitDtoList.stream().filter(item -> Objects.nonNull(item.getLampFuncType())).collect(
                        Collectors.groupingBy(
                                DtpRwdCircuitDTO::getFuncType,
                                Collectors.groupingBy(
                                        DtpRwdCircuitDTO::getLampFuncType
                                )
                        )
                );

        // 5. 根据分组结构和状态映射生成最终设备控制信息
        return listLightEquipmentControlByDemand(groupedData, manualAutoStatusMap);
    }

    @Override
    public Collection<LightingSpaceWithStatusVO> listSpaceWithStatus(LightingSpaceWithStatusQueryParam param) {
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        if (param.getInstanceType() == null) {
            param.setInstanceType(DemandInstanceTypeEnum.PROJECT);
        }
        // 查询照明回路
        List<DtpRwdCircuitDTO> circuitList = demandLightingStrategy.listCircuitsByInstance(
                projectId, templateType, param.getInstanceType(), param.getInstanceId());
        // 查询控制模块
        List<DtpRwdObjectDTO> controllerList = demandLightingStrategy.listControllersByInstance(
                projectId, templateType, param.getInstanceType(), param.getInstanceId());
        Map<String, DtpRwdCircuitDTO> circuitMap = circuitList.stream().collect(Collectors.toMap(DtpRwdCircuitDTO::getId, Function.identity(), (v1, v2) -> v1));
        Map<String, DtpRwdObjectDTO> controllerMap = controllerList.stream().collect(Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (v1, v2) -> v1));
        // 查询照明回路id去查询照明回路服务的空间
        Set<String> circuitIds = circuitList.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());
        List<DtpRwdSpaceDTO> circuitSpaceList = demandLightingStrategy.listSpacesByCircuit(projectId, templateType, circuitIds, null);
        Set<String> spaceIds = circuitSpaceList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        // 查询控制模块所在的空间
        Set<String> controllerIdList = controllerList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<DtpRwdSpaceDTO> controllerSpaceList = demandLightingStrategy.listSpacesByController(projectId, templateType, controllerIdList, null);

        Map<String, LightingSpaceTagDTO> spaceIdAndTag = mapSpaceIdAndTag(projectId, templateType, spaceIds);

        Map<String, LightingSpaceWithStatusVO> voMap = new HashMap<>(16);
        // 处理照明回路
        circuitSpaceList.forEach(space -> {
            LightingSpaceWithStatusVO spaceWithStatusVo = lightingConverter.toSpaceWithStatusVo(space);
            Set<String> relObjIds = space.getRelObjIds();
            List<DtpRwdCircuitDTO> dtpRwdCircuits = MapUtil.valuesOfKeys(circuitMap, Objects.requireNonNull(relObjIds).iterator()).stream().filter(Objects::nonNull).collect(Collectors.toList());
            List<String> funcTypes = dtpRwdCircuits.stream().map(DtpRwdCircuitDTO::getFuncType).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            List<String> lampFuncTypes = dtpRwdCircuits.stream().map(DtpRwdCircuitDTO::getLampFuncType).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            spaceWithStatusVo.setFuncTypes(funcTypes);
            spaceWithStatusVo.setLampFuncTypes(lampFuncTypes);
            // 查询空间标签
            LightingSpaceTagDTO spaceTag = spaceIdAndTag.get(space.getId());
            LightingSpaceWithStatusVO.mergeTag(spaceWithStatusVo, spaceTag);
            voMap.put(space.getId(), spaceWithStatusVo);
        });
        // 处理控制模块
        controllerSpaceList.forEach(space -> {
            LightingSpaceWithStatusVO spaceWithStatusVo = voMap.get(space.getId());
            if (spaceWithStatusVo == null) {
                spaceWithStatusVo = lightingConverter.toSpaceWithStatusVo(space);
                // 查询空间标签
                LightingSpaceTagDTO spaceTag = spaceIdAndTag.get(space.getId());
                LightingSpaceWithStatusVO.mergeTag(spaceWithStatusVo, spaceTag);
                voMap.put(space.getId(), spaceWithStatusVo);
            }
            Set<String> relObjIds = space.getRelObjIds();
            List<DtpRwdObjectDTO> relObjs = MapUtil.valuesOfKeys(controllerMap, Objects.requireNonNull(relObjIds).iterator()).stream().filter(Objects::nonNull).collect(Collectors.toList());
            spaceWithStatusVo.setRelControllers(relObjs);
        });
        // 只返回全开，全关和部分开启的数据
        return voMap.values().stream()
                .filter(e -> (e.getOpenCloseStatus() == DemandLightingTypeSettingModeEnum.ALL_OPEN
                        || e.getOpenCloseStatus() == DemandLightingTypeSettingModeEnum.ALL_CLOSE
                        || e.getOpenCloseStatus() == DemandLightingTypeSettingModeEnum.PARTIAL_OPEN)
                        || CollectionUtil.isNotEmpty(e.getRelControllers()))
                .collect(Collectors.toList());
    }

    @Override
    public LightingNightWithStatusVO getLampFuncTypeWithStatus(
            @NonNull LightingLampFuncTypeWithStatusQueryParam param) {
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();

        Map<String, DtpRwdCircuitDTO> circuitIdAndData = demandLightingStrategy
                .mapCircuitIdAndData(projectId, templateType, null);

        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuits(projectId, templateType, null);
        if (CollectionUtils.isEmpty(circuits)) {
            return null;
        }

        Map<String, Set<String>> lampFuncTypeAndRelFuncTypes = new HashMap<>(16);
        Set<String> lampFuncTypes = new HashSet<>();
        for (DtpRwdCircuitDTO circuit : circuits) {
            String funcType = circuit.getFuncType();
            String lampFuncType = circuit.getLampFuncType();

            if (StringUtils.isNotBlank(lampFuncType)) {
                lampFuncTypes.add(lampFuncType);
                MapExecutor.dynamicPut(lampFuncTypeAndRelFuncTypes, lampFuncType, funcType, HashSet::new);
            }
        }

        // 查询灯具功能类型和标签映射
        Map<String, LightingLampFuncTypeTagDTO> lampFuncTypeAndTag =
                mapLampFuncTypeAndTag(projectId, templateType, lampFuncTypes);

        // 查询灯具类型带状态分组
        List<LightingLampFuncTypeWithStatusVO> lampFuncTypeWithStatuses = queryLampFuncTypeWithStatuses(
                projectId, templateType, lampFuncTypes, lampFuncTypeAndRelFuncTypes, lampFuncTypeAndTag);
        lampFuncTypeWithStatuses.removeIf(item -> DemandLightingStrategy.SPECIAL_NIGHT_LIGHTING_LAMP_FUNC_TYPES
                .contains(item.getLampFuncType()));

        // 查询楼层带状态分组
        List<DtpRwdFloorDTO> floorsByCircuit = demandLightingStrategy.listFloorsByCircuit(
                projectId, templateType, circuitIdAndData.keySet(), null);
        List<LightingFloorWithStatusVO> floorWithStatuses = new ArrayList<>();
        for (DtpRwdFloorDTO floorByCircuit : floorsByCircuit) {
            Set<String> relCircuitIds = floorByCircuit.getRelObjIds();
            if (CollectionUtils.isEmpty(relCircuitIds)) {
                continue;
            }
            Set<String> relLampFuncTypes = new HashSet<>();
            for (String relCircuitId : relCircuitIds) {
                DtpRwdCircuitDTO relCircuit = circuitIdAndData.get(relCircuitId);
                if (Objects.nonNull(relCircuit)) {
                    String lampFuncType = relCircuit.getLampFuncType();
                    if (StringUtils.isNotBlank(lampFuncType)) {
                        relLampFuncTypes.add(lampFuncType);
                    }
                }
            }
            List<LightingLampFuncTypeWithStatusVO> relLampFuncTypeWithStatuses = queryLampFuncTypeWithStatuses(
                    projectId, templateType, relLampFuncTypes, lampFuncTypeAndRelFuncTypes, lampFuncTypeAndTag);

            LightingFloorWithStatusVO floorWithStatus = lightingConverter.toFloorWithStatusVo(floorByCircuit);
            floorWithStatus.setLampFuncTypes(relLampFuncTypeWithStatuses);
            floorWithStatuses.add(floorWithStatus);
        }

        LightingNightWithStatusVO result = new LightingNightWithStatusVO();
        result.setLampFuncTypes(lampFuncTypeWithStatuses);
        result.setFloors(floorWithStatuses);
        return result;
    }


    @Override
    public LightingControllerDetailVO getControllerDetail(@NonNull LightingControllerDetailQueryParam param) {
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        String spaceId = param.getSpaceId();
        String controllerId = param.getControllerId();

        // noinspection deprecation
        DtpRwdSpaceDTO space = getOne(dtpRwdClientService.listSpace(RWD_QUERY_CACHE, true,
                projectId, new DtpRwdObjectQueryParam().setId(spaceId)));
        // noinspection deprecation
        DtpRwdFloorDTO floor = getOne(true, dtpRwdClientService.listFloorBySpaceId(RWD_QUERY_CACHE, true,
                projectId, Lists.newArrayList(spaceId), new DtpRwdObjectQueryParam()));

        DtpRwdObjectDTO controller = getOne(dtpRwdClientService.listCircuitControllerInBySpaceIds(RWD_QUERY_CACHE, true,
                projectId, Lists.newArrayList(spaceId), new DtpRwdObjectQueryParam().setId(controllerId)));

        Map<String, LightingControllerTagVO> controllerIdAndTag =
                mapControllerIdAndTag(projectId, Lists.newArrayList(controllerId));
        Map<String, List<ExceptionEventBaseInfoVO>> objectIdAndAbnormalEvents =
                mapObjectIdAndAbnormalEvents(Collections.singleton(controllerId));
        List<DtpRwdCircuitDTO> relCircuits = demandLightingStrategy.listCircuitsByController(
                projectId, templateType, Lists.newArrayList(controllerId), null);
        DtpRwdObjectDTO relDistributionCabinet = demandLightingStrategy.mapControllerIdAndDistributionCabinet(
                projectId, Lists.newArrayList(controllerId)).get(controllerId);

        List<LightingCircuitDetailVO> circuitDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(relCircuits)) {
            Map<String, DtpRwdObjectDTO> circuitIdAndController = new HashMap<>(Math.max(relCircuits.size(), 16));
            Map<String, DtpRwdSpaceDTO> circuitIdAndElectricWel = new HashMap<>(Math.max(relCircuits.size(), 16));
            for (DtpRwdCircuitDTO relCircuit : relCircuits) {
                circuitIdAndController.put(relCircuit.getId(), controller);
                circuitIdAndElectricWel.put(relCircuit.getId(), space);
            }
            CommonLightingCircuitDetailContext circuitDetailContext = initCircuitDetailContext(
                    projectId, templateType, relCircuits, circuitIdAndController, circuitIdAndElectricWel);
            fillAbnormalEvents(circuitDetailContext);
            fillDynamicInfoCodeConfig(circuitDetailContext);
            circuitDetails = circuitDetailContext.buildCircuitDetails();
        }

        LightingControllerDetailVO result = lightingConverter.toControllerDetailVo(controller);
        result.setTag(controllerIdAndTag.get(controllerId));
        result.setAbnormalEvents(objectIdAndAbnormalEvents.get(controllerId));
        result.setRelCircuits(circuitDetails);
        result.setRelDistributionCabinet(dtpRwdConvert.toObjectVo(relDistributionCabinet));
        result.setRelSpace(dtpRwdConvert.toObjectVo(space));
        result.setRelFloor(dtpRwdConvert.toObjectVo(floor));
        return result;
    }

    // region AI Generate

    /**
     * 根据场景和任务配置填充功能类型到回路配置的映射
     * <p>
     * 1. 场景和任务的时间段可能会有重叠
     * 2. 重叠时优先使用任务中的 settingMode
     * 3. 使用 {@link org.apache.commons.lang3.Range} 进行时间段判断
     * 4. 生成目标日期 0 点到次日 0 点的每个时间段的配置，并标记是否存在场景/任务配置
     *
     * @param circuits 回路列表
     * @param scenes   场景列表
     * @param tasks    任务列表
     * @param date     目标日期
     * @return 功能类型配置列表
     * @since 7.260101.0
     */
    private List<LihgtingDemandTargetVO.TimeFuncTypeConfig> fillFuncType2CircuitSetting(
            List<DtpRwdCircuitDTO> circuits,
            List<LihgtingDemandTargetVO.Scene> scenes,
            List<InterventionTaskConfigVO> tasks,
            LocalDate date) {
        if (CollectionUtils.isEmpty(circuits)) {
            return Collections.emptyList();
        }

        // 1. 初始化基础数据结构
        Map<String, String> circuitId2FuncType = circuits.stream().collect(Collectors.toMap(
                DtpRwdObjectDTO::getId, DtpRwdCircuitDTO::getFuncType));
        Map<String, List<TimeRangeConfig>> circuitId2TimeRangeConfigs = new HashMap<>();

        // 1.1 初始化所有回路ID到时间范围配置的映射
        for (DtpRwdCircuitDTO circuit : circuits) {
            circuitId2TimeRangeConfigs.put(circuit.getId(), new ArrayList<>());
        }

        // 2. 获取目标日期时间范围（0点到次日 0 点）
        LocalDateTime dateStart = date.atStartOfDay();
        LocalDateTime nextDateStart = dateStart.plusDays(1);
        // 创建前闭后开的时间范围 [dateStart, nextDateStart)，使用前闭后闭的Range表示
        Range<ChronoLocalDateTime<?>> dateRange = Range.between(dateStart, nextDateStart.minusNanos(1));

        // 3. 处理场景配置
        this.processSceneConfigs(scenes, circuitId2TimeRangeConfigs);

        // 4. 处理任务配置（任务配置优先级更高）
        this.processTaskConfigs(tasks, circuitId2TimeRangeConfigs, dateRange);

        // 5. 合并时间段，处理重叠，生成每个时间段的配置
        List<TimeSegmentConfig> timeSegmentConfigs = this.mergeAndSplitTimeRanges(
                circuitId2TimeRangeConfigs, circuitId2FuncType, dateStart, nextDateStart);

        // 6. 构建结果
        return this.buildTimeSegmentResult(timeSegmentConfigs);
    }

    /**
     * 处理场景配置
     *
     * @param scenes                     场景列表
     * @param circuitId2TimeRangeConfigs 回路 ID 到时间段配置的映射
     * @since 7.260101.0
     */
    private void processSceneConfigs(
            List<LihgtingDemandTargetVO.Scene> scenes,
            Map<String, List<TimeRangeConfig>> circuitId2TimeRangeConfigs) {
        if (CollectionUtils.isEmpty(scenes)) {
            return;
        }

        for (LihgtingDemandTargetVO.Scene scene : scenes) {
            // 创建场景时间范围（前闭后开的语义，用前闭后闭的Range表示）
            // 注意：场景的结束时间是不包含的，所以需要减1纳秒
            Range<ChronoLocalDateTime<?>> sceneTimeRange = Range.between(
                    scene.getBeginDateTime(), scene.getEndDateTime().minusNanos(1));

            // 处理场景中的功能类型配置
            if (CollectionUtils.isEmpty(scene.getFuncTypeConfigs())) {
                continue;
            }
            for (LihgtingDemandTargetVO.FuncTypeConfig funcTypeConfig : scene.getFuncTypeConfigs()) {
                if (CollectionUtils.isEmpty(funcTypeConfig.getCircuitSettings())) {
                    continue;
                }

                for (LihgtingDemandTargetVO.FuncTypeConfig.CircuitConfig circuitConfig : funcTypeConfig.getCircuitSettings()) {
                    String circuitId = circuitConfig.getCircuitId();
                    DemandLightingCircuitSettingModeEnum settingMode = circuitConfig.getSettingMode();

                    if (StringUtils.isNotBlank(circuitId) && settingMode != null) {
                        circuitId2TimeRangeConfigs.computeIfAbsent(circuitId, k -> new ArrayList<>())
                                .add(new TimeRangeConfig(ConfigSourceType.SCENE, sceneTimeRange, settingMode));
                    }
                }
            }
        }
    }

    /**
     * 处理任务配置
     *
     * @param tasks                      任务列表
     * @param circuitId2TimeRangeConfigs 回路 ID 到时间段配置的映射
     * @param dateRange                  目标日期时间范围
     * @since 7.260101.0
     */
    private void processTaskConfigs(
            List<InterventionTaskConfigVO> tasks,
            Map<String, List<TimeRangeConfig>> circuitId2TimeRangeConfigs,
            Range<ChronoLocalDateTime<?>> dateRange) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        for (InterventionTaskConfigVO task : tasks) {
            // 解析任务配置
            InterventionTaskConfigDTO.Lighting lightingConfig =
                    this.lightingInterventionTaskConfigHandler.parseConfig(task.getConfigData());

            // 解析任务时间范围
            LinkedList<Range<ChronoLocalDateTime<?>>> taskTimeRanges = lightingConfig.getExecuteTimeRanges();

            // 获取任务配置中的回路 ID 和配置模式
            Set<String> taskCircuitIds = lightingConfig.getConfig().getCircuitIds();
            DemandLightingCircuitSettingModeEnum taskSettingMode = lightingConfig.getConfig().getSettingMode();

            if (CollectionUtils.isEmpty(taskCircuitIds) || taskSettingMode == null
                    || CollectionUtils.isEmpty(taskTimeRanges)) {
                continue;
            }

            for (String circuitId : taskCircuitIds) {
                for (Range<ChronoLocalDateTime<?>> taskTimeRange : taskTimeRanges) {
                    // 只处理与目标日期有交集的时间段
                    if (!taskTimeRange.isOverlappedBy(dateRange)) {
                        continue;
                    }

                    // 计算与目标日期的交集
                    LocalDateTime effectiveStart = taskTimeRange.getMinimum().isAfter(dateRange.getMinimum())
                            ? (LocalDateTime) taskTimeRange.getMinimum() : (LocalDateTime) dateRange.getMinimum();
                    LocalDateTime effectiveEnd = taskTimeRange.getMaximum().isBefore(dateRange.getMaximum())
                            ? (LocalDateTime) taskTimeRange.getMaximum() : (LocalDateTime) dateRange.getMaximum();
                    // 注意：这里effectiveStart和effectiveEnd都是从前闭后闭的Range中取出的Maximum，
                    // 已经是减1纳秒后的值，所以创建新Range时不需要再减1纳秒
                    Range<ChronoLocalDateTime<?>> effectiveRange = Range.between(effectiveStart, effectiveEnd);

                    circuitId2TimeRangeConfigs.computeIfAbsent(circuitId, k -> new ArrayList<>())
                            .add(new TimeRangeConfig(ConfigSourceType.TASK, effectiveRange, taskSettingMode));
                }
            }
        }
    }

    /**
     * 合并时间段配置
     *
     * @param circuitId2TimeRangeConfigs 回路 ID 到时间段配置的映射
     * @param circuitId2FuncType         回路 ID 到功能类型的映射
     * @param dateStart                  目标日期开始时间
     * @param nextDateStart              次日开始时间
     * @return 时间段配置列表
     * @since 7.260101.0
     */
    private List<TimeSegmentConfig> mergeAndSplitTimeRanges(
            Map<String, List<TimeRangeConfig>> circuitId2TimeRangeConfigs,
            Map<String, String> circuitId2FuncType,
            LocalDateTime dateStart,
            LocalDateTime nextDateStart) {
        // 1. 收集所有唯一的时间点
        Set<LocalDateTime> timePoints = new TreeSet<>();
        timePoints.add(dateStart);
        timePoints.add(nextDateStart);

        // 创建目标日期的时间范围（前闭后开的语义 [dateStart, nextDateStart)，使用前闭后闭的Range表示）
        Range<ChronoLocalDateTime<?>> dateRange = Range.between(dateStart, nextDateStart.minusNanos(1));

        for (List<TimeRangeConfig> configs : circuitId2TimeRangeConfigs.values()) {
            for (TimeRangeConfig config : configs) {
                // 检查配置的时间范围是否与目标日期有交集
                if (config.timeRange.isOverlappedBy(dateRange)) {
                    LocalDateTime startTime = (LocalDateTime) config.timeRange.getMinimum();
                    LocalDateTime endTime = (LocalDateTime) config.timeRange.getMaximum();

                    // 计算有效的开始时间（如果开始时间在目标日期之前，截断到目标日期0点）
                    LocalDateTime effectiveStart = startTime.isBefore(dateStart) ? dateStart : startTime;
                    // 计算有效的结束时间（如果结束时间在目标日期之后，截断到次日0点-1ns）
                    // 注意：这里使用nextDateStart.minusNanos(1)，因为时间段是前闭后开的
                    LocalDateTime effectiveEnd = endTime.isAfter(nextDateStart.minusNanos(1))
                            ? nextDateStart.minusNanos(1) : endTime;

                    // 添加有效的开始时间（不添加目标日期0点，因为已经默认添加）
                    if (!effectiveStart.isEqual(dateStart)) {
                        timePoints.add(effectiveStart);
                    }
                    // 添加有效的结束时间（不添加次日0点-1ns，因为已经默认添加）
                    // 注意：这里需要判断是否等于nextDateStart.minusNanos(1)而不是nextDateStart
                    if (!effectiveEnd.isEqual(nextDateStart.minusNanos(1))) {
                        timePoints.add(effectiveEnd.plusNanos(1));
                    }
                }
            }
        }

        // 2. 将时间点列表转换为时间段
        List<LocalDateTime> sortedTimePoints = new ArrayList<>(timePoints);
        List<TimeSegmentConfig> result = new ArrayList<>();

        for (int i = 0; i < sortedTimePoints.size() - 1; i++) {
            LocalDateTime segmentStart = sortedTimePoints.get(i);
            LocalDateTime segmentEnd = sortedTimePoints.get(i + 1);

            // 为每个时间段构建配置
            TimeSegmentConfig segmentConfig = this.buildTimeSegmentConfigForRange(
                    segmentStart, segmentEnd, circuitId2TimeRangeConfigs, circuitId2FuncType);
            result.add(segmentConfig);
        }

        return result;
    }

    /**
     * 为指定时间范围构建时间段配置
     *
     * @param segmentStart               时间段开始时间
     * @param segmentEnd                 时间段结束时间
     * @param circuitId2TimeRangeConfigs 回路 ID 到时间段配置的映射
     * @param circuitId2FuncType         回路 ID 到功能类型的映射
     * @return 时间段配置
     * @since 7.260101.0
     */
    private TimeSegmentConfig buildTimeSegmentConfigForRange(
            LocalDateTime segmentStart,
            LocalDateTime segmentEnd,
            Map<String, List<TimeRangeConfig>> circuitId2TimeRangeConfigs,
            Map<String, String> circuitId2FuncType) {

        // 创建时间段范围（前闭后开的语义 [segmentStart, segmentEnd)，使用前闭后闭的Range表示）
        Range<ChronoLocalDateTime<?>> segmentRange = Range.between(segmentStart, segmentEnd.minusNanos(1));

        // 判断是否存在场景/任务配置
        boolean hasSceneConfig = false;
        boolean hasTaskConfig = false;
        
        // 存储该时间段内有任务配置的回路ID
        Set<String> circuitIdsWithTaskConfig = new HashSet<>();
        // 存储该时间段内有场景配置的回路ID
        Set<String> circuitIdsWithSceneConfig = new HashSet<>();

        // 第一遍遍历：统计该时间段内有哪些配置
        for (Map.Entry<String, List<TimeRangeConfig>> entry : circuitId2TimeRangeConfigs.entrySet()) {
            String circuitId = entry.getKey();
            List<TimeRangeConfig> configs = entry.getValue();

            for (TimeRangeConfig config : configs) {
                // 检查配置的时间范围是否与当前时间段有重叠
                if (config.timeRange.isOverlappedBy(segmentRange)) {
                    // 记录配置来源
                    if (config.sourceType == ConfigSourceType.TASK) {
                        hasTaskConfig = true;
                        circuitIdsWithTaskConfig.add(circuitId);
                    } else if (config.sourceType == ConfigSourceType.SCENE) {
                        hasSceneConfig = true;
                        circuitIdsWithSceneConfig.add(circuitId);
                    }
                }
            }
        }

        // 按功能类型组织回路配置
        Map<String, List<LihgtingDemandTargetVO.FuncTypeConfig.CircuitConfig>> funcType2CircuitConfigs = new HashMap<>();

        // 第二遍遍历：根据优先级规则为每个回路设置配置模式
        for (Map.Entry<String, List<TimeRangeConfig>> entry : circuitId2TimeRangeConfigs.entrySet()) {
            String circuitId = entry.getKey();
            List<TimeRangeConfig> configs = entry.getValue();
            String funcType = circuitId2FuncType.get(circuitId);

            if (StringUtils.isBlank(funcType)) {
                continue;
            }

            // 查找当前时间段内有效的配置
            List<TimeRangeConfig> activeConfigs = new ArrayList<>();
            for (TimeRangeConfig config : configs) {
                // 检查配置的时间范围是否与当前时间段有重叠
                if (config.timeRange.isOverlappedBy(segmentRange)) {
                    activeConfigs.add(config);
                }
            }

            // 根据业务规则确定配置模式
            DemandLightingCircuitSettingModeEnum mode;
            
            // 规则1：优先使用任务配置（如果该回路有任务配置，直接使用）
            if (circuitIdsWithTaskConfig.contains(circuitId)) {
                mode = this.getHighestPriorityMode(activeConfigs);
            }
            // 规则2：没有任务配置时，判断是否有场景配置
            else if (circuitIdsWithSceneConfig.contains(circuitId)) {
                // 该回路有场景配置，使用场景配置
                mode = this.getHighestPriorityMode(activeConfigs);
            }
            // 规则3：没有任务配置也没有场景配置，判断当前时间段是否存在场景配置
            else if (hasSceneConfig) {
                // 当前时间段存在场景配置，但该回路不在场景中，设置为关
                mode = DemandLightingCircuitSettingModeEnum.CLOSE;
            }
            // 规则4：当前时间段既无任务配置也无场景配置，设置为未配置
            else {
                mode = DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED;
            }

            funcType2CircuitConfigs
                    .computeIfAbsent(funcType, k -> new ArrayList<>())
                    .add(new LihgtingDemandTargetVO.FuncTypeConfig.CircuitConfig(circuitId, mode));
        }

        return new TimeSegmentConfig(segmentStart, segmentEnd, funcType2CircuitConfigs, hasSceneConfig, hasTaskConfig);
    }

    /**
     * 构建结果
     *
     * @param timeSegmentConfigs 时间段配置列表
     * @return 功能类型配置列表
     * @since 7.260101.0
     */
    private List<LihgtingDemandTargetVO.TimeFuncTypeConfig> buildTimeSegmentResult(
            List<TimeSegmentConfig> timeSegmentConfigs) {
        return timeSegmentConfigs.stream()
                .map(segment -> {
                    List<LihgtingDemandTargetVO.FuncTypeConfig> funcTypeConfigs = segment.funcType2CircuitConfigs.entrySet().stream()
                            .map(entry -> new LihgtingDemandTargetVO.FuncTypeConfig(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());

                    return new LihgtingDemandTargetVO.TimeFuncTypeConfig(
                            segment.startTime,
                            segment.endTime,
                            funcTypeConfigs,
                            segment.hasSceneConfig,
                            segment.hasTaskConfig
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取最高优先级的配置模式
     *
     * @param activeConfigs 当前活动的配置列表
     * @return 最高优先级的配置模式
     * @since 7.260101.0
     */
    private DemandLightingCircuitSettingModeEnum getHighestPriorityMode(List<TimeRangeConfig> activeConfigs) {
        if (CollectionUtils.isEmpty(activeConfigs)) {
            return null;
        }

        DemandLightingCircuitSettingModeEnum highestMode = null;
        ConfigSourceType highestSourceType = null;

        for (TimeRangeConfig config : activeConfigs) {
            // 任务配置优先于场景配置
            if (highestSourceType == null || config.sourceType.priority > highestSourceType.priority) {
                highestMode = config.settingMode;
                highestSourceType = config.sourceType;
            } else if (config.sourceType == highestSourceType) {
                // 相同来源类型，比较配置模式优先级
                if (highestMode == null || DemandLightingCircuitSettingModeEnum.shouldUpdate(highestMode, config.settingMode)) {
                    highestMode = config.settingMode;
                }
            }
        }
        return highestMode;
    }

    /**
     * 配置来源类型
     *
     * @since 7.260101.0
     */
    private enum ConfigSourceType {
        /**
         * 场景配置
         */
        SCENE(1),
        /**
         * 任务配置（优先级更高）
         */
        TASK(2);

        private final int priority;

        ConfigSourceType(int priority) {
            this.priority = priority;
        }
    }

    /**
     * 时间段配置
     *
     * @since 7.260101.0
     */
    private static class TimeRangeConfig {
        private final ConfigSourceType sourceType;
        private final Range<ChronoLocalDateTime<?>> timeRange;
        private final DemandLightingCircuitSettingModeEnum settingMode;

        public TimeRangeConfig(ConfigSourceType sourceType, Range<ChronoLocalDateTime<?>> timeRange,
                               DemandLightingCircuitSettingModeEnum settingMode) {
            this.sourceType = sourceType;
            this.timeRange = timeRange;
            this.settingMode = settingMode;
        }
    }

    /**
     * 时间段配置（包含功能类型和配置来源信息）
     *
     * @since 7.260101.0
     */
    private static class TimeSegmentConfig {
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final Map<String, List<LihgtingDemandTargetVO.FuncTypeConfig.CircuitConfig>> funcType2CircuitConfigs;
        private final boolean hasSceneConfig;
        private final boolean hasTaskConfig;

        public TimeSegmentConfig(
                LocalDateTime startTime,
                LocalDateTime endTime,
                Map<String, List<LihgtingDemandTargetVO.FuncTypeConfig.CircuitConfig>> funcType2CircuitConfigs,
                boolean hasSceneConfig,
                boolean hasTaskConfig) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.funcType2CircuitConfigs = funcType2CircuitConfigs;
            this.hasSceneConfig = hasSceneConfig;
            this.hasTaskConfig = hasTaskConfig;
        }
    }

    // endregion

    /**
     * 查询模版场景
     *
     * @param template           需求模板
     * @param date               需求模板时间
     * @param circuitIds         回路 ID 列表
     * @param circuitId2FuncType 回路 ID 到功能类型的映射
     * @since 7.260101.0
     */
    private List<LihgtingDemandTargetVO.Scene> queryScenes(
            DemandTemplate template,
            LocalDate date,
            List<String> circuitIds,
            Map<String, String> circuitId2FuncType) {
        // 查询场景
        List<DemandSceneEntity> scenes = demandSceneGateway.listValidScenesByDate(
                template.getDemandTemplateType(), date);
        if (CollectionUtils.isEmpty(scenes)) {
            return Collections.emptyList();
        }

        // 查询配置结果
        List<Long> sceneIds = scenes.stream()
                .map(DemandSceneEntity::getId)
                .collect(Collectors.toList());
        Map<Long, List<LihgtingDemandTargetVO.FuncTypeConfig>> sceneId2FuncTypeConfigs =
                buildSceneConfigsMapping(template.getId(), sceneIds, circuitIds, circuitId2FuncType);

        return scenes.stream()
                .map(scene -> buildSceneVO(scene, sceneId2FuncTypeConfigs))
                .collect(Collectors.toList());
    }

    /**
     * 构建场景配置映射
     *
     * @param templateId         模板ID
     * @param sceneIds           场景ID列表
     * @param circuitIds         回路ID列表
     * @param circuitId2FuncType 回路ID到功能类型的映射
     * @return 场景ID到功能类型配置的映射
     * @since 7.260101.0
     */
    private Map<Long, List<LihgtingDemandTargetVO.FuncTypeConfig>> buildSceneConfigsMapping(
            Long templateId,
            List<Long> sceneIds,
            List<String> circuitIds,
            Map<String, String> circuitId2FuncType) {

        DemandConfigLightingResultQueryDaoParam resultsQueryParam = new DemandConfigLightingResultQueryDaoParam();
        resultsQueryParam.setRelDemandTemplateId(templateId);
        resultsQueryParam.setRelDemandLightingSceneIds(sceneIds);
        resultsQueryParam.setCircuitIds(circuitIds);
        List<DemandConfigLightingResult> configResults = this.demandConfigLightingResultService.list(resultsQueryParam);

        if (CollectionUtils.isEmpty(configResults)) {
            return new HashMap<>();
        }
        // 按场景ID分组
        Map<Long, List<DemandConfigLightingResult>> sceneId2Configs = configResults.stream()
                .collect(Collectors.groupingBy(DemandConfigLightingResult::getRelDemandLightingSceneId));
        // 构建每个场景的功能类型配置
        Map<Long, List<LihgtingDemandTargetVO.FuncTypeConfig>> sceneId2FuncTypeConfigs = new HashMap<>();
        sceneId2Configs.forEach((sceneId, configs) ->
                sceneId2FuncTypeConfigs.put(sceneId, buildFuncTypeConfigs(configs, circuitId2FuncType))
        );
        return sceneId2FuncTypeConfigs;
    }

    /**
     * 构建功能类型配置列表
     *
     * @param configs            配置结果列表
     * @param circuitId2FuncType 回路ID到功能类型的映射
     * @return 功能类型配置列表
     * @since 7.260101.0
     */
    private List<LihgtingDemandTargetVO.FuncTypeConfig> buildFuncTypeConfigs(
            List<DemandConfigLightingResult> configs,
            Map<String, String> circuitId2FuncType) {
        Map<String, List<LihgtingDemandTargetVO.FuncTypeConfig.CircuitConfig>> funcType2CircuitConfigs = new HashMap<>();
        for (DemandConfigLightingResult config : configs) {
            String circuitId = config.getCircuitId();
            String funcType = circuitId2FuncType.get(circuitId);

            if (StringUtils.isBlank(funcType)) {
                continue;
            }
            DemandLightingCircuitSettingModeEnum settingMode = config.getConfigData().getCircuitSettingMode();
            // 需求配置中未配置为关
            if (settingMode == DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED) {
                settingMode = DemandLightingCircuitSettingModeEnum.CLOSE;
            }
            LihgtingDemandTargetVO.FuncTypeConfig.CircuitConfig circuitConfig =
                    new LihgtingDemandTargetVO.FuncTypeConfig.CircuitConfig(circuitId, settingMode);
            MapExecutor.dynamicPut(funcType2CircuitConfigs, funcType, circuitConfig, ArrayList::new);
        }
        return funcType2CircuitConfigs.entrySet().stream()
                .map(entry -> new LihgtingDemandTargetVO.FuncTypeConfig(
                        entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 构建场景VO
     *
     * @param scene                   场景实体
     * @param sceneId2FuncTypeConfigs 场景ID到功能类型配置的映射
     * @return 场景VO
     */
    private LihgtingDemandTargetVO.Scene buildSceneVO(
            DemandSceneEntity scene,
            Map<Long, List<LihgtingDemandTargetVO.FuncTypeConfig>> sceneId2FuncTypeConfigs) {
        LihgtingDemandTargetVO.Scene resultScene = new LihgtingDemandTargetVO.Scene();
        resultScene.setId(scene.getId());
        resultScene.setName(scene.getSceneName());
        resultScene.setBeginDateTime(scene.getRealBeginTime());
        resultScene.setEndDateTime(scene.getRealEndTime());
        resultScene.setFuncTypeConfigs(sceneId2FuncTypeConfigs.get(scene.getId()));
        return resultScene;
    }

    /**
     * 查询干预任务配置
     *
     * @param runningMonitorSecondType 运行监控二级类型
     * @param instanceType             实例类型
     * @param instanceId               实例 ID
     * @param circuits                 回路列表
     * @param date                     目标日期
     * @return 干预任务配置列表
     * @since 7.260101.0
     */
    private List<InterventionTaskConfigVO> queryInterventionTasks(
            RunningMonitorSecondTypeEnum runningMonitorSecondType,
            DemandInstanceTypeEnum instanceType,
            String instanceId,
            List<DtpRwdCircuitDTO> circuits,
            LocalDate date) {
        InterventionTaskConfigQueryParam taskConfigQueryParam = new InterventionTaskConfigQueryParam();
        taskConfigQueryParam.setRunningMonitorType(runningMonitorSecondType.toRunningMonitorTypeEnum());
        taskConfigQueryParam.setRunningMonitorSecondType(runningMonitorSecondType);
        taskConfigQueryParam.setInstanceType(instanceType);
        taskConfigQueryParam.setInstanceIds(Sets.newHashSet(instanceId));
        taskConfigQueryParam.setBeginTime(date.atStartOfDay());
        // 创建目标日期时间范围（前闭后开的语义 [00:00:00, 23:59:59.999999999]，这里LocalTime.MAX就是23:59:59.999999999）
        // 注意：这里使用LocalTime.MAX是正确的，因为它等同于次日00:00:00.minusNanos(1)
        Range<ChronoLocalDateTime<?>> dateRange = Range.between(date.atStartOfDay(), date.atTime(LocalTime.MAX));
        return this.lightingInterventionTaskConfigHandler.queryList(taskConfigQueryParam, circuits).stream()
                .filter(config -> {
                    InterventionTaskConfigDTO.Lighting configData = this.lightingInterventionTaskConfigHandler
                            .parseConfig(config.getConfigData());
                    List<Range<ChronoLocalDateTime<?>>> timeRanges = configData.getExecuteTimeRanges();
                    return CollectionUtils.isNotEmpty(timeRanges) && timeRanges.stream()
                            .anyMatch(range -> range.isOverlappedBy(dateRange));
                })
                .collect(Collectors.toList());
    }

    /**
     * 填充动态信息点配置
     *
     * @param context 上下文
     * @since 5.240402.0
     */
    public void fillDynamicInfoCodeConfig(CommonLightingCircuitDetailContext context) {
        String projectId = context.getProjectId();

        Collection<DtpRwdCircuitDTO> circuits = context.getCircuits();
        Set<String> circuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        // 查询照明回路信息点定义
        List<DtpKnowledgeInfoCodeDTO> circuitInfoCodeDefined = dtpKnowledgeClientService.listInfoCodes(
                true, false, DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value());

        // 查询照明回路运行信息点配置列表
        List<RcInfoCodeCfg> circuitRunningInfoCodes = localRedisCacheHandler.getInfoCodeConfigs(
                DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(), DemandInfoCodeTypeEnum.LIGHTING, false);
        circuitRunningInfoCodes.removeIf(item -> !item.isDynamicParameter());
        // 查询照明回路ID -> 运行信息点反馈值列表
        Map<String, List<IotDataCommon>> circuitIdAndRunningFeedbackInfos =
                queryObjectIdAndFeedbackInfosByConfig(projectId, circuitIds, circuitRunningInfoCodes);
        // 生成运行信息点参数
        Map<String, List<InfoCodeValueVO>> circuitIdAndRunningInfoCodeParameters =
                mapCircuitIdAndDynamicInfoCodeParameters(projectId, circuitIds, circuitRunningInfoCodes,
                        circuitIdAndRunningFeedbackInfos, circuitInfoCodeDefined);
        context.setCircuitRunningInfoCodes(circuitRunningInfoCodes);
        context.setCircuitIdAndRunningFeedbackInfos(circuitIdAndRunningFeedbackInfos);
        context.setCircuitIdAndRunningInfoCodeParameters(circuitIdAndRunningInfoCodeParameters);

        // 查询照明回路控制信息点配置列表
        List<RcInfoCodeCfg> circuitControlInfoCodes = localRedisCacheHandler.getInfoCodeConfigs(
                DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(), DemandInfoCodeTypeEnum.LIGHTING, true);
        // 查询照明回路ID -> 控制信息点反馈值列表
        Map<String, List<IotDataCommon>> circuitIdAndControlFeedbackInfos =
                queryObjectIdAndFeedbackInfosByConfig(projectId, circuitIds, circuitControlInfoCodes);
        // 生成控制信息点参数
        Map<String, List<InfoCodeValueVO>> circuitIdAndControlInfoCodeParameters =
                mapCircuitIdAndDynamicInfoCodeParameters(projectId, circuitIds, circuitControlInfoCodes,
                        circuitIdAndControlFeedbackInfos, circuitInfoCodeDefined);
        context.setCircuitControlInfoCodes(circuitControlInfoCodes);
        context.setCircuitIdAndControlFeedbackInfos(circuitIdAndControlFeedbackInfos);
        context.setCircuitIdAndControlInfoCodeParameters(circuitIdAndControlInfoCodeParameters);
        // 填充判断参数信息点
        // noinspection unchecked
        List<IotDataCommon> infoCodeVal = abstractIotDataCacheServer.queryIotRealDataListByList(projectId, circuitIds, Arrays.asList(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode()
                , InfoCodeEnum.ONLINE_STATUS.getCode()));
        Map<String, IotDataCommon> restrictInfos = new HashMap<>();
        if (CollectionUtils.isNotEmpty(infoCodeVal)) {
            infoCodeVal.forEach(i -> restrictInfos.put(i.getObjectId() + i.getInfoCode(), i));
        }
        context.setRestrictInfos(restrictInfos);
    }


    /**
     * 查询对象ID和IOT反馈信息映射
     *
     * @param projectId 项目ID
     * @param objectIds 对象ID
     * @param infoCodes 信息点
     * @return 对象ID -> IOT反馈信息
     * @since 5.240402.0
     */
    private Map<String, List<IotDataCommon>> queryObjectIdAndFeedbackInfosByConfig(
            @NonNull String projectId,
            @Nullable Collection<String> objectIds,
            @Nullable Collection<RcInfoCodeCfg> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Maps.newHashMap();
        }
        List<String> queryObjectIds = new ArrayList<>(objectIds);
        List<String> queryInfoCodes = infoCodes.stream()
                .map(RcInfoCodeCfg::getInfoCode).distinct()
                .collect(Collectors.toList());
        // noinspection unchecked
        List<IotDataCommon> list = abstractIotDataCacheServer.queryIotRealDataListByList(projectId, queryObjectIds, queryInfoCodes);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.groupingBy(IotDataCommon::getObjectId, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * 查询对象ID和运行参数映射
     *
     * @param projectId                项目ID
     * @param objectIds                照明对象ID
     * @param infoCodeConfigs          信息点配置, 为空时实时查询
     * @param objectIdAndFeedbackInfos 对象ID -> IOT反馈信息, 为空时实时查询
     * @param knowledgeInfoCodes       数据字典信息点定义, 为空时实时查询
     * @return 对象ID -> 动态信息点参数列表
     * @since 5.240402.0
     */
    private Map<String, List<InfoCodeValueVO>> mapCircuitIdAndDynamicInfoCodeParameters(
            @NonNull String projectId,
            @NonNull Collection<String> objectIds,
            @Nullable List<RcInfoCodeCfg> infoCodeConfigs,
            @Nullable Map<String, List<IotDataCommon>> objectIdAndFeedbackInfos,
            @Nullable List<DtpKnowledgeInfoCodeDTO> knowledgeInfoCodes) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Maps.newHashMap();
        }
        infoCodeConfigs = Optional.ofNullable(infoCodeConfigs).orElseGet(() -> {
            List<RcInfoCodeCfg> configs = localRedisCacheHandler.getInfoCodeConfigs(
                    DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value(), DemandInfoCodeTypeEnum.LIGHTING, null);
            configs.removeIf(item -> !item.isDynamicParameter());
            return configs;
        });
        if (CollectionUtils.isEmpty(infoCodeConfigs)) {
            return Maps.newHashMap();
        }
        return mapCircuitIdAndInfoCodeValues(projectId, objectIds,
                infoCodeConfigs, objectIdAndFeedbackInfos, knowledgeInfoCodes);
    }


    /**
     * 将IOT信息点反馈信息设置到动态信息点参数中
     *
     * @param projectId                项目ID
     * @param objectIds                照明对象ID
     * @param infoCodeConfigs          动态信息点配置
     * @param objectIdAndFeedbackInfos 对象ID -> IOT反馈信息, 为空时实时查询
     * @param knowledgeInfoCodes       数据字典信息点定义, 为空时实时查询
     * @return 对象ID -> 动态信息点参数列表
     * @since 5.240402.0
     */
    private Map<String, List<InfoCodeValueVO>> mapCircuitIdAndInfoCodeValues(
            @NonNull String projectId,
            @NonNull Collection<String> objectIds,
            @NonNull Collection<RcInfoCodeCfg> infoCodeConfigs,
            @Nullable Map<String, List<IotDataCommon>> objectIdAndFeedbackInfos,
            @Nullable List<DtpKnowledgeInfoCodeDTO> knowledgeInfoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodeConfigs)) {
            return Maps.newHashMap();
        }
        // 查询孪生规则
        List<DtpRwdTwinObjectRuleResponse> circuitTwinRules = dtpRwdClientService.listTwinObjectRule(
                Lists.newArrayList(new DtpRwdTwinObjectRuleQueryParam(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT)));
        Map<String, Set<String>> circuitIdAndInfoCodes = circuitTwinRules.stream()
                .collect(Collectors.groupingBy(DtpRwdTwinObjectRuleResponse::getObjectId,
                        Collectors.mapping(DtpRwdTwinObjectRuleResponse::getInfoCode, Collectors.toSet())));

        LinkedHashMap<String, RcInfoCodeCfg> infoCodeAndConfig = infoCodeConfigs.stream().collect(Collectors.toMap(
                RcInfoCodeCfg::getInfoCode, Function.identity(), (o1, o2) -> o1, LinkedHashMap::new));
        Set<String> infoCodes = infoCodeAndConfig.keySet();

        objectIdAndFeedbackInfos = Optional.ofNullable(objectIdAndFeedbackInfos).orElseGet(
                () -> queryObjectIdAndFeedbackInfos(projectId,
                        new ArrayList<>(objectIds), new ArrayList<>(infoCodes)));
        knowledgeInfoCodes = Optional.ofNullable(knowledgeInfoCodes).orElseGet(() ->
                dtpKnowledgeClientService.listInfoCodes(true, false,
                        DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value()));
        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndDefined = knowledgeInfoCodes.stream()
                .collect(Collectors.toMap(DtpKnowledgeInfoCodeDTO::getCode, Function.identity(),
                        (o1, o2) -> o1, LinkedHashMap::new));

        Map<String, List<InfoCodeValueVO>> result = new LinkedHashMap<>(Math.max(objectIds.size(), 16));
        for (String objectId : objectIds) {
            Set<String> twinInfoCode = circuitIdAndInfoCodes.get(objectId);
            // 对象无孪生规则信息点直接跳过
            if (CollectionUtils.isEmpty(twinInfoCode)) {
                continue;
            }
            Map<String, InfoCodeValueVO> infoCodeAndData = new LinkedHashMap<>(Math.max(infoCodes.size(), 16));
            // 填充信息点配置至结果集中
            for (String infoCode : infoCodes) {
                // 信息点配置没有孪生规则时跳过
                if (!twinInfoCode.contains(infoCode)) {
                    continue;
                }
                DtpKnowledgeInfoCodeDTO defined = infoCodeAndDefined.get(infoCode);
                if (Objects.isNull(defined)) {
                    LogExecutor.project(projectId).warn(log(), logTag(), "信息点未在数据字典定义",
                            Collections.singletonList(Pair.of("configInfoCode", infoCode)));
                    continue;
                }
                InfoCodeValueVO infoCodeValue = dtpKnowledgeConverter.toRunningParameterVo(defined);
                RcInfoCodeCfg config = infoCodeAndConfig.get(infoCode);
                String infoCodeName = config.getDynamicInfoCodeName(infoCodeValue.getInfoCodeName());
                infoCodeValue.setInfoCodeName(infoCodeName);
                infoCodeAndData.put(infoCode, infoCodeValue);
            }
            List<IotDataCommon> feedbackInfos = objectIdAndFeedbackInfos.get(objectId);
            // 当存在IOT反馈信息点时将反馈值填充至结果集中
            if (CollectionUtils.isNotEmpty(feedbackInfos)) {
                for (IotDataCommon feedbackInfo : feedbackInfos) {
                    String infoCode = feedbackInfo.getInfoCode();
                    String dataValue = feedbackInfo.getDataValue();
                    LocalDateTime dataTime = feedbackInfo.toDataTime();
                    if (StringUtils.isAnyBlank(infoCode)) {
                        LogExecutor.project(projectId).warn(log(), logTag(), "反馈值异常",
                                Collections.singletonList(Pair.of("feedbackInfo", feedbackInfo)));
                        continue;
                    }
                    InfoCodeValueVO infoCodeValue = infoCodeAndData.get(infoCode);
                    if (Objects.nonNull(infoCodeValue)) {
                        infoCodeValue.setInfoCodeValue(dataValue).setDataTime(dataTime);
                        Map<String, String> additionalMapValue = generateAdditionalMapValue(objectId, infoCodeValue);
                        if (MapUtils.isNotEmpty(additionalMapValue)) {
                            infoCodeValue.setAdditionalMapValue(additionalMapValue);
                        }
                    }
                }
            }
            if (MapUtils.isNotEmpty(infoCodeAndData)) {
                result.put(objectId, new ArrayList<>(infoCodeAndData.values()));
            }
        }
        return result;
    }


    /**
     * 生成 信息点值的附加信息
     *
     * @param objectId 对象ID
     * @param source   数据源
     * @return 信息点值的附加信息
     * @since 5.240402.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> generateAdditionalMapValue(
            @NonNull String objectId,
            @Nullable InfoCodeValueVO source) {
        if (Objects.isNull(source)) {
            return Maps.newHashMap();
        }
        String infoCode = source.getInfoCode();
        Object infoCodeValue = source.getInfoCodeValue();
        if (Objects.isNull(infoCodeValue)) {
            return Maps.newHashMap();
        }

        Map<String, String> additionalMapValue = new HashMap<>(16);
        // 当手自动状态为手动时设置恢复时间
        if (StringUtils.equals(infoCode, InfoCodeEnum.MANUAL_AUTO_SET.value())
                && Objects.equals(infoCodeValue, "0")) {
            Map<String, Map<String, LocalDateTime>> objectIdAllIotSetInfoCodeAndRecoverTime = abstractIotDataCacheServer
                    .queryObjectIdAllIotSetInfoCodeAndRecoverTime(Lists.newArrayList(objectId));
            LocalDateTime recoverTime = MapExecutor.dynamicGet(objectIdAllIotSetInfoCodeAndRecoverTime,
                    objectId, InfoCodeEnum.MANUAL_AUTO_SET.getCode());
            if (Objects.nonNull(recoverTime)) {
                additionalMapValue.put("recoveryDateTime", recoverTime.format(DatePattern.NORM_DATETIME_FORMATTER));
            }
        }
        return additionalMapValue;
    }

    /**
     * 查询对象ID和IOT反馈信息映射
     *
     * @param projectId 项目ID
     * @param objectIds 对象ID
     * @param infoCodes 信息点
     * @return 对象ID -> IOT反馈信息
     * @since 5.240402.0
     */
    private Map<String, List<IotDataCommon>> queryObjectIdAndFeedbackInfos(
            @NonNull String projectId,
            @Nullable Collection<String> objectIds,
            @Nullable Collection<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            return Maps.newHashMap();
        }
        List<String> queryObjectIds = new ArrayList<>(objectIds);
        List<String> queryInfoCodes = new ArrayList<>(infoCodes);
        // noinspection unchecked
        List<IotDataCommon> list = abstractIotDataCacheServer.queryIotRealDataListByList(projectId, queryObjectIds, queryInfoCodes);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream()
                .collect(Collectors.groupingBy(IotDataCommon::getObjectId, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * 查询照明控制模块标签
     *
     * @param projectId     项目ID
     * @param controllerIds 照明控制模块ID, 为空时查询所有
     * @return 空间标签
     * @since 5.240402.0
     */
    public Map<String, LightingControllerTagVO> mapControllerIdAndTag(
            @NonNull String projectId, @NonNull Collection<String> controllerIds) {
        if (CollectionUtils.isEmpty(controllerIds)) {
            return Maps.newHashMap();
        }
        Map<String, IotDataCommon> controllerIdAndData = queryObjectIdAndFeedbackInfo(
                projectId, controllerIds, InfoCodeEnum.ONLINE_STATUS.getCode());

        DtpKnowledgeInfoCodeDTO infoCode = dtpKnowledgeClientService.getInfoCode(false,
                DtpRwdClassCodeEnum.LIGHTING_CONTROLLER.value(), InfoCodeEnum.ONLINE_STATUS.getCode());

        Map<String, LightingControllerTagVO> result = new HashMap<>(Math.min(controllerIds.size(), 16));
        for (String controllerId : controllerIds) {
            IotDataCommon data = controllerIdAndData.get(controllerId);
            // 默认为离线
            String dataValue = "0";
            LocalDateTime dataTime = null;
            if (Objects.nonNull(data)) {
                dataValue = data.getDataValue();
                dataTime = data.toDataTime();
            }
            InfoCodeValueVO runningParameter = dtpKnowledgeConverter.toRunningParameterVo(infoCode, dataValue, dataTime);
            LightingControllerTagVO controllerTag = new LightingControllerTagVO();
            controllerTag.setOnlineStatus(runningParameter);
            result.put(controllerId, controllerTag);
        }
        return result;
    }

    /**
     * 查询对象ID和IOT反馈信息映射
     *
     * @param projectId 项目ID
     * @param objectIds 对象ID
     * @param infoCode  信息点
     * @return 对象ID -> IOT反馈信息
     * @since 5.240402.0
     */
    private Map<String, IotDataCommon> queryObjectIdAndFeedbackInfo(
            @NonNull String projectId,
            @Nullable Collection<String> objectIds,
            @Nullable String infoCode) {
        if (CollectionUtils.isEmpty(objectIds) || StringUtils.isBlank(infoCode)) {
            return Maps.newHashMap();
        }
        List<String> queryObjectIds = new ArrayList<>(objectIds);
        List<String> queryInfoCodes = Lists.newArrayList(infoCode);
        // noinspection unchecked
        List<IotDataCommon> list = abstractIotDataCacheServer.queryIotRealDataListByList(projectId, queryObjectIds, queryInfoCodes);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(IotDataCommon::getObjectId, Function.identity(), (o1, o2) -> o1));
    }

    /**
     * 查询照明回路的绑点信息
     *
     * @return 对象+ 信息点 的绑点信息
     */
    public Set<String> getLtltccExistRulesSet() {
        // 查询照明回路的绑点信息
        List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), Lists.newArrayList(new ClassCode(DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.value())));
        // 转换成 SET
        Set<String> objectsExistRulesSet = new HashSet<>();
        objectsExistRules.forEach(item -> objectsExistRulesSet.add(item.getObjectId() + item.getInfoCode()));
        return objectsExistRulesSet;
    }

    /**
     * 填充异常事件
     *
     * @param context 上下文
     * @since 5.240402.0
     */
    private void fillAbnormalEvents(@NonNull CommonLightingCircuitDetailContext context) {
        Collection<DtpRwdCircuitDTO> circuits = context.getCircuits();
        Set<String> circuitIds = circuits.stream().map(DtpRwdCircuitDTO::getId).collect(Collectors.toSet());

        // 查询照明回路异常事件
        Map<String, List<ExceptionEventBaseInfoVO>> circuitIdAndAbnormalEvents =
                mapObjectIdAndAbnormalEvents(circuitIds);

        context.setCircuitIdAndAbnormalEvents(circuitIdAndAbnormalEvents);
    }

    /**
     * 填充异常事件
     *
     * @param context 上下文
     * @since 5.240402.0
     */
    private void fillAbnormalEvents(@NonNull CommonLightingSpaceDetailContext context) {
        String projectId = context.getQueryParam().getProjectId();

        // 填充照明回路详情异常事件
        fillAbnormalEvents(context.getCircuitDetailContext());

        Collection<DtpRwdObjectDTO> controllers = context.getControllers();
        Set<String> controllerIds = controllers.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        // 查询控制模块异常事件
        Map<String, List<ExceptionEventBaseInfoVO>> controllerIdAndAbnormalEvents =
                mapObjectIdAndAbnormalEvents(controllerIds);

        context.setControllerIdAndAbnormalEvents(controllerIdAndAbnormalEvents);
    }


    /**
     * 查询对象ID和异常事件映射
     *
     * @param objectIds 对象ID
     * @return 对象ID -> 异常事件
     * @since 5.240402.0
     */
    protected Map<String, List<ExceptionEventBaseInfoVO>> mapObjectIdAndAbnormalEvents(
            @Nullable Collection<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Maps.newHashMap();
        }
        return this.abstractCommonServer.mapRiskEvent(objectIds, null);
    }

    @Override
    public LightingSidebarIllumInfoVO getIllumInfo(ProjectVO projectInfo) {
        // 先查项目照度实例化规则
        String groupCode = ContextUtil.getGroupCode();
        String projectId = projectInfo.getProjectId();
        boolean hasProjectIllumRule = true;
        Map<String, Set<String>> classCodeAndObjectIds = Collections.singletonMap(DtpRwdClassCodeEnum.PROJECT.value(), Collections.singleton(projectId));

        Map<String, Set<String>> rules = this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(classCodeAndObjectIds, Collections.singleton(InfoCodeEnum.OUTDOOR_ILLUMINANCE.value()));
        if (rules.isEmpty() || rules.get(projectId).isEmpty() || !rules.get(projectId).contains(InfoCodeEnum.OUTDOOR_ILLUMINANCE.value())) {
            hasProjectIllumRule = false;
        }

        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setClassCodeEnum(DtpRwdClassCodeEnum.WESSIL);

        // 照度传感器
        List<DtpRwdObjectDTO> objectList = this.dtpRwdClientService.listObject(RWD_QUERY_CACHE, false, projectId, param);
        ArrayList<LightingSidebarSensorInfoVO> sidebarSensorInfos = new ArrayList<>();
        // 设备非空查询设备的传感器照度值
        if (CollectionUtils.isNotEmpty(objectList)) {
            Map<String, String> idNameMap =
                    objectList.stream().collect(HashMap::new,
                            (map, item) -> map.put(item.getId(), item.getLocalName()),
                            HashMap::putAll
                    );
            // 查询照度传感器的所在空间（MechInArch/Eq2Sp)气味空间的“室内外类型（isOutdoorSpace）”=2
            List<DtpRwdObjectDTO> spaces = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setClassCodeEnum(DtpRwdClassCodeEnum.SPACE)
                    .setRelationTo(new DtpRwdObjectRelationQueryParam(
                            DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjFrom(idNameMap.keySet()))
                    .addInfoCode(InfoProperty.IS_OUTDOOR_SPACE.getCode(), "2")
                    .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds, InfoProperty.IS_OUTDOOR_SPACE.getCode()));
            if (CollectionUtil.isNotEmpty(spaces)) {
                Map<String, Set<String>> relObjIds = DtpRwdObjectDTO.mapRelObjIdAndIds(spaces);

                idNameMap.forEach((objectId, localName) -> {
                    if (relObjIds.containsKey(objectId)) {
                        IotDataCommon infoCodeData
                                = (IotDataCommon) this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, objectId,
                                DtpRwdClassCodeEnum.WESSIL.getValue(), InfoCodeEnum.DEVICE_ILLUM.value());

                        // 创建基础构建器，避免重复设置公共字段
                        LightingSidebarSensorInfoVO.LightingSidebarSensorInfoVOBuilder builder = LightingSidebarSensorInfoVO.builder()
                                .instanceId(objectId)
                                .classCode(DtpRwdClassCodeEnum.WESSIL.getValue())
                                .infoCode(InfoCodeEnum.DEVICE_ILLUM.value())
                                .instanceName(localName);

                        // 根据传感器数据是否存在设置相应字段
                        if (Objects.nonNull(infoCodeData)) {
                            builder.dataValue(infoCodeData.getDataValue())
                                   .illum(infoCodeData.getDataValue());
                        }

                        sidebarSensorInfos.add(builder.build());
                    }
                });
                log.info("集团{}, 项目{}, 获取到的设备信息{}, 照度传感器数据{}", groupCode, projectId, idNameMap, sidebarSensorInfos);
            }
        }


        LightingSidebarIllumInfoVO build = new LightingSidebarIllumInfoVO();
        build.setLightingSidebarSensorInfoVO(sidebarSensorInfos);
        build.setUnit("lx");

        if (hasProjectIllumRule) {
            IotDataCommon infoCodeData
                    = (IotDataCommon) this.abstractIotDataCacheServer.queryIotRealDataVO(groupCode, projectId, projectId, "", InfoCodeEnum.OUTDOOR_ILLUMINANCE.value());
            log.info("集团{}, 项目{}, 获取到的照度传感器平均值数据{}", groupCode, projectId, infoCodeData);
            if (Objects.nonNull(infoCodeData)) {
                build.setOutdoorIlluminance(infoCodeData.getDataValue());
            }else{
                build.setOutdoorIlluminance("");
            }
        }
        return build;
    }

    /**
     * 初始化空间详情上下文
     *
     * @param param 查询参数
     * @return 空间详情上下文
     * @since 5.240402.0
     */
    private CommonLightingSpaceDetailContext initSpaceDetailContext(@NonNull LightingSpaceDetailQueryParam param) {
        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getTemplateType();
        String spaceId = param.getSpaceId();

        CommonLightingSpaceDetailContext context = new CommonLightingSpaceDetailContext();
        context.setQueryParam(param);

        // 查询空间
        StopWatch stopWatch = StopWatch.create("初始化空间详情上下文");
        stopWatch.start("查询空间");
        DtpRwdSpaceDTO space = getOne(true, dtpRwdClientService.listSpace(new DtpRwdObjectQueryParam()
                .setId(spaceId).addWithColumn(DtpRwdSpaceDTO.Fields.class)));
        context.setSpace(space);
        stopWatch.stop();
        // 查询空间标签
        stopWatch.start("查询空间标签");
        LightingSpaceTagDTO spaceTag = mapSpaceIdAndTag(projectId, templateType,
                Lists.newArrayList(spaceId)).get(spaceId);
        context.setSpaceTag(spaceTag);
        stopWatch.stop();
        // 查询照明回路
        stopWatch.start("查询照明回路");
        List<DtpRwdCircuitDTO> circuits = demandLightingStrategy.listCircuitsByInstance(
                projectId, templateType, DemandInstanceTypeEnum.SPACE, spaceId);
        stopWatch.stop();

        Set<String> allFloorIds = Sets.newHashSet(space.getFloorId());
        Map<String, DtpRwdObjectDTO> circuitIdAndController = Collections.emptyMap();
        Map<String, Set<DtpRwdCircuitDTO>> controllerIdAndCircuits = Collections.emptyMap();
        List<DtpRwdObjectDTO> controllers = new ArrayList<>();
        Map<String, DtpRwdSpaceDTO> controllerIdAndSpace = Collections.emptyMap();
        Map<String, String> controllerIdAndFloorId = Collections.emptyMap();
        Map<String, DtpRwdFloorDTO> controllerIdAndFloor = new HashMap<>();
        Map<String, LightingControllerTagVO> controllerIdAndTag = Collections.emptyMap();
        Map<String, DtpRwdObjectDTO> controllerIdAndDistributionCabinet = Collections.emptyMap();

        // 查询空间下照明控制模块
        stopWatch.start("查询空间下照明控制模块");
        List<DtpRwdObjectDTO> controllersInSpace = demandLightingStrategy.listControllersBySpaceIds(
                projectId, templateType, Lists.newArrayList(spaceId), null);
        Map<String, DtpRwdObjectDTO> controllerIdAndData = controllersInSpace.stream().collect(
                Collectors.toMap(DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
        stopWatch.stop();
        if (CollectionUtils.isNotEmpty(circuits)) {
            Set<String> circuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            // 查询控制模块和照明回路之间的关系
            stopWatch.start("查询控制模块和照明回路之间的关系");
            controllerIdAndCircuits = demandLightingStrategy.mapControllerIdAndCircuits(
                    projectId, templateType, null, circuitIds);
            stopWatch.stop();

            // 查询照明回路和控制模块之间的关系
            stopWatch.start("查询照明回路和控制模块之间的关系");
            Set<String> allControllerIds = new HashSet<>(controllerIdAndData.keySet());
            allControllerIds.addAll(controllerIdAndCircuits.keySet());
            circuitIdAndController = demandLightingStrategy.mapCircuitIdAndController(
                    projectId, templateType, null, allControllerIds);
            // 添加照明回路关联的控制模块
            circuitIdAndController.values().forEach(item -> controllerIdAndData.put(item.getId(), item));
            stopWatch.stop();
        }
        if (MapUtils.isNotEmpty(controllerIdAndData)) {
            Set<String> allControllerIds = new HashSet<>();
            for (Map.Entry<String, DtpRwdObjectDTO> entry : controllerIdAndData.entrySet()) {
                allControllerIds.add(entry.getKey());
                controllers.add(entry.getValue());
            }
            // 查询控制模块所在空间
            stopWatch.start("查询控制模块所在空间");
            controllerIdAndSpace = demandLightingStrategy.mapControllerIdAndSpace(
                    projectId, templateType, allControllerIds, null);
            // 填充照明回路关联的楼层ID
            controllerIdAndFloorId = new HashMap<>();
            for (Map.Entry<String, DtpRwdSpaceDTO> entry : controllerIdAndSpace.entrySet()) {
                controllerIdAndFloorId.put(entry.getKey(), entry.getValue().getFloorId());
                allFloorIds.add(entry.getValue().getFloorId());
            }
            stopWatch.stop();
            // 查询控制模块ID -> 控制模块标签
            stopWatch.start("查询控制模块标签");
            controllerIdAndTag = mapControllerIdAndTag(projectId, allControllerIds);
            stopWatch.stop();
            // 查询低压末端配电柜
            stopWatch.start("查询低压末端配电柜");
            controllerIdAndDistributionCabinet = demandLightingStrategy
                    .mapControllerIdAndDistributionCabinet(projectId, allControllerIds);
            stopWatch.stop();
        }
        // 查询楼层
        stopWatch.start("查询楼层");
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(new DtpRwdObjectQueryParam()
                .setId(allFloorIds).addWithColumn(DtpRwdFloorDTO.Fields.class));
        Map<String, DtpRwdFloorDTO> floorIdAndData = floors.stream().collect(Collectors.toMap(
                DtpRwdObjectDTO::getId, Function.identity(), (o1, o2) -> o1));
        DtpRwdFloorDTO floor = floorIdAndData.get(space.getFloorId());
        if (Objects.nonNull(floor)) {
            context.setFloor(floor);
        }
        controllerIdAndFloorId.forEach((controllerId, floorId) -> {
            DtpRwdFloorDTO relFloor = floorIdAndData.get(floorId);
            if (Objects.nonNull(relFloor)) {
                controllerIdAndFloor.put(controllerId, floor);
            }
        });
        stopWatch.stop();
        context.setControllers(controllers)
                .setControllerIdAndSpace(controllerIdAndSpace)
                .setControllerIdAndFloor(controllerIdAndFloor)
                .setControllerIdAndTag(controllerIdAndTag)
                .setControllerIdAndCircuits(controllerIdAndCircuits)
                .setControllerIdAndDistributionCabinet(controllerIdAndDistributionCabinet);
        // 查询照明回路详情上下文
        stopWatch.start("查询照明回路详情上下文");
        CommonLightingCircuitDetailContext circuitDetailContext = initCircuitDetailContext(
                projectId, templateType, circuits, circuitIdAndController, controllerIdAndSpace);
        stopWatch.stop();
        // 查询回路绑点信息
        stopWatch.start("查询回路绑点信息");
        circuitDetailContext.setLtltccExistRulesSet(getLtltccExistRulesSet());
        stopWatch.stop();
        context.setCircuitDetailContext(circuitDetailContext);
        logProjectInstance().debug(stopWatch.getId(), Pair.of("data", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return context;
    }

    /**
     * 初始化照明回路详情上下文
     *
     * @param projectId               项目ID
     * @param templateType            模版类型
     * @param circuits                照明回路
     * @param circuitIdAndController  照明回路ID -> 关联控制模块, 为 {@code null} 时实时查询
     * @param circuitIdAndElectricWel 照明回路ID -> 关联电井, 为 {@code null} 时实时查询
     * @return 照明回路详情上下文
     * @since 5.240402.0
     */
    private CommonLightingCircuitDetailContext initCircuitDetailContext(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<DtpRwdCircuitDTO> circuits,
            @Nullable Map<String, DtpRwdObjectDTO> circuitIdAndController,
            @Nullable Map<String, DtpRwdSpaceDTO> circuitIdAndElectricWel) {
        CommonLightingCircuitDetailContext context = new CommonLightingCircuitDetailContext();
        context.setProjectId(projectId);
        context.setTemplateType(templateType);
        context.setCircuits(circuits);

        if (CollectionUtils.isEmpty(circuits)) {
            return context;
        }

        Set<String> circuitIds = circuits.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        // 查询照明回路ID -> 照明回路标签
        Map<String, LightingCircuitTagVO> circuitIdAndTag = mapCircuitIdAndTag(projectId, templateType, circuitIds);
        // 查询照明回路和低压末端配电柜之间的关系
        Map<String, DtpRwdObjectDTO> circuitIdAndDistributionCabinet = demandLightingStrategy
                .mapCircuitIdAndDistributionCabinet(projectId, circuitIds);
        context.setCircuitIdAndTag(circuitIdAndTag);
        context.setCircuitIdAndDistributionCabinet(circuitIdAndDistributionCabinet);

        // 当照明回路ID关联控制模块为null时, 实时查询照明回路关联控制模块
        if (Objects.isNull(circuitIdAndController)) {
            circuitIdAndController = demandLightingStrategy.mapCircuitIdAndController(
                    projectId, templateType, circuitIds, null);
        }
        context.setCircuitIdAndController(circuitIdAndController);

        // 当不存在照明回路ID和电井映射且关联控制模块存在时, 实时查询照明回路关联电井
        if (Objects.isNull(circuitIdAndElectricWel)) {
            circuitIdAndElectricWel = Collections.emptyMap();
            if (MapUtils.isNotEmpty(circuitIdAndController)) {
                Set<String> controllerIds = circuitIdAndController.values().stream()
                        .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                circuitIdAndElectricWel = demandLightingStrategy.mapControllerIdAndSpace(
                        projectId, templateType, controllerIds, null);
            }
        }
        context.setCircuitIdAndElectricWel(circuitIdAndElectricWel);
        return context;
    }


    /**
     * 查询灯具功能类型带状态VO
     *
     * @param projectId                   项目ID
     * @param templateType                模版类型
     * @param lampFuncTypes               灯具功能类型
     * @param lampFuncTypeAndRelFuncTypes 灯具功能类型 -> 关联功能类型
     * @param lampFuncTypeAndTag          灯具功能类型 -> 灯具功能类型标签
     * @return 灯具功能类型带状态VO
     * @since 5.240501.0
     */
    private List<LightingLampFuncTypeWithStatusVO> queryLampFuncTypeWithStatuses(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<String> lampFuncTypes,
            @NonNull Map<String, Set<String>> lampFuncTypeAndRelFuncTypes,
            @Nullable Map<String, LightingLampFuncTypeTagDTO> lampFuncTypeAndTag) {
        if (CollectionUtils.isEmpty(lampFuncTypes)) {
            return Lists.newArrayList();
        }

        // 查询灯具功能类型信息点定义
        Map<String, String> lampFuncTypeDefine = demandLightingStrategy
                .mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.lampFuncType);

        // 查询灯具功能类型和标签映射
        lampFuncTypeAndTag = Optional.ofNullable(lampFuncTypeAndTag)
                .orElseGet(() -> mapLampFuncTypeAndTag(projectId, templateType, lampFuncTypes));

        List<LightingLampFuncTypeWithStatusVO> result = new ArrayList<>();
        for (String lampFuncType : lampFuncTypes) {
            String lampFuncTypeName = lampFuncTypeDefine.get(lampFuncType);
            LightingLampFuncTypeTagDTO lampFuncTypeTag = lampFuncTypeAndTag.get(lampFuncType);
            Set<String> relFuncTypes = lampFuncTypeAndRelFuncTypes.get(lampFuncType);

            LightingLampFuncTypeWithStatusVO resultItem = new LightingLampFuncTypeWithStatusVO();
            resultItem.setLampFuncType(lampFuncType);
            resultItem.setLampFuncTypeName(lampFuncTypeName);
            resultItem.setTag(lampFuncTypeTag);
            resultItem.setRelFuncTypes(relFuncTypes);
            result.add(resultItem);
        }
        return result;
    }

    /**
     * 查询照明回路的信息点
     *
     * @param circuitIds 照明回路
     * @param infoCodes  信息点, 为空时查询所有相关信息点
     * @return 照明回路ID -> 信息点值
     * @since 7.250402.0
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Object>> mapCircuitIdAndInfoCodeValues(
            @NonNull Set<String> circuitIds, @Nullable String... infoCodes) {
        if (CollectionUtils.isEmpty(circuitIds) || ArrayUtils.isEmpty(infoCodes)) {
            return Maps.newHashMap();
        }
        Set<String> finalInfoCodes = Optional.ofNullable(infoCodes)
                .map(item -> Arrays.stream(item).filter(Objects::nonNull).collect(Collectors.toSet()))
                .orElseGet(HashSet::new);
        if (CollectionUtils.isEmpty(finalInfoCodes)) {
            return Maps.newHashMap();
        }
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer
                .listInfoCodeValueInOneHour(circuitIds, finalInfoCodes);
        return InfoCodeValueVO.listToObjectInfoCodeValueMap(infoCodeValues);
    }


    /**
     * 获取回路开启关闭状态映射
     *
     * @param objectIdList 回路ID集合
     * @param infoCodeList 信息点集合
     * @return {@link Map<String,Map<String,String>>} ,<信息点，,<回路ID，回路状态>>
     */
    public Map<String, Map<String, String>> mapInfoCodeObjectValue(Collection<String> objectIdList, List<String> infoCodeList) {
        List<IotDataCommon> objectDataList =
                this.abstractIotDataCacheServer.queryIotRealDataListByList(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), objectIdList, infoCodeList);
        Map<String, Map<String, String>> infoCodeObjectValueMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(objectDataList)) {
            Map<String, List<IotDataCommon>> infoCodeAndValueMap =
                    objectDataList.stream().collect(Collectors.groupingBy(IotDataCommon::getInfoCode));
            Map<String, String> objectIdAndValueMap = new HashMap<>(16);
            infoCodeAndValueMap.forEach((infoCode, dataValues) -> {
                for (IotDataCommon iotDataCommon : dataValues) {
                    objectIdAndValueMap.put(iotDataCommon.getObjectId(), iotDataCommon.getDataValue());
                }
                infoCodeObjectValueMap.put(infoCode, objectIdAndValueMap);
            });
        }
        return infoCodeObjectValueMap;
    }

    /**
     * 场景下的全量设备统计多开少开和需求开启，实际开启
     *
     * @param circuitIdAndInfoCodeValues 回路IOT
     * @param circuitTagMap              标签map集合
     * @param circuitIdAndConfigResult   需求场景设定
     * @param groupedData                照明回路信息
     * @return 照明功能类型列表
     */
    @SuppressWarnings({"AlibabaAvoidComplexCondition", "AlibabaMethodTooLong"})
    private List<LightingSidebarLightLoopTotalVO> listLightLoopTotalByDemand(
            Map<String, Map<String, Object>> circuitIdAndInfoCodeValues,
            Map<String, LightingCircuitTagVO> circuitTagMap,
            Map<String, DemandConfigLightingResultDataDTO> circuitIdAndConfigResult,
            Map<String, Map<String, List<DtpRwdCircuitDTO>>> groupedData) {

        // 功能类型，灯具类型的名称
        Map<String, String> funcTypeMap = demandLightingStrategy.mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.funcType);
        Map<String, String> lampFuncTypeMap = demandLightingStrategy.mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.lampFuncType);

        List<LightingSidebarLightLoopTotalVO> resultList = new ArrayList<>();


        for (Map.Entry<String, Map<String, List<DtpRwdCircuitDTO>>> entry : groupedData.entrySet()) {
            ArrayList<LightingSidebarLightLoopTotalChildrenVO> result = new ArrayList<>();
            // 总数
            int allDemandOnNumber = 0;
            int allRealOnNumber = 0;
            int allLessOpenNumber = 0;
            int allMoreOpenNumber = 0;
            for (Map.Entry<String, List<DtpRwdCircuitDTO>> lampFuncTypeEntry : entry.getValue().entrySet()) {
                // 统计当前灯具类型下的需求和实际数量
                String lampFuncType = lampFuncTypeEntry.getKey();
                List<DtpRwdCircuitDTO> circuits = lampFuncTypeEntry.getValue();
                int demandOnNumber = 0;
                int realOnNumber = 0;
                int lessOpenNumber = 0;
                int moreOpenNumber = 0;
                for (DtpRwdCircuitDTO circuit : circuits) {
                    String circuitId = circuit.getId();
                    DemandConfigLightingResultDataDTO configResult = circuitIdAndConfigResult.get(circuitId);
                    Map<String, Object> infoCodeAndValue = circuitIdAndInfoCodeValues.get(circuitId);
                    DtpKnowledgeRunStatusEnum runStatus = DtpKnowledgeRunStatusEnum.analyze(infoCodeAndValue);

                    DemandLightingCircuitSettingModeEnum modeEnum = DemandLightingCircuitSettingModeEnum.CLOSE;
                    if (Objects.nonNull(configResult) && Objects.nonNull(configResult.getCircuitSettingMode())) {
                        modeEnum = configResult.getCircuitSettingMode();
                    }
                    if (modeEnum == DemandLightingCircuitSettingModeEnum.OPEN) {
                        demandOnNumber++;
                    }
                    if (runStatus == DtpKnowledgeRunStatusEnum.RUNNING) {
                        realOnNumber++;
                    }

                    // 多开少开单条回路统计
                    LightingCircuitTagVO tagInfo = circuitTagMap.get(circuitId);
                    if (Objects.nonNull(tagInfo)) {
                        LightingRunningDeviationTypeEnum runningTag = tagInfo.getRunningDeviationType();
                        if (Objects.nonNull(runningTag)) {
                            switch (runningTag) {
                                case LESS_OPEN:
                                    lessOpenNumber++;
                                    break;
                                case MORE_OPEN:
                                    moreOpenNumber++;
                                    break;
                                default:
                                    throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(runningTag.getClass().getSimpleName(), runningTag);
                            }
                        }
                    }
                }

                allDemandOnNumber += demandOnNumber;
                allRealOnNumber += realOnNumber;
                // 此处代表有偏差才将灯具类型加入响应结果集，且加入总数
                if ((lessOpenNumber != 0 || moreOpenNumber != 0)) {
                    allLessOpenNumber += lessOpenNumber;
                    allMoreOpenNumber += moreOpenNumber;
                    // 照明灯具类型
                    LightingSidebarLightLoopTotalChildrenVO lampInfo = LightingSidebarLightLoopTotalChildrenVO.builder()
                            .lampFuncType(lampFuncType)
                            .lampFuncTypeName(StringUtils.isNotBlank(lampFuncType) ? lampFuncTypeMap.get(lampFuncType) : DemandLightingTypeSettingModeEnum.NOT_CONFIGURED.getReasonPhrase())
                            .demandOnNumber(demandOnNumber).realOnNumber(realOnNumber)
                            .lessOpenNumber(lessOpenNumber).moreOpenNumber(moreOpenNumber)
                            .build();
                    result.add(lampInfo);
                }
            }

            // 照明功能类型
            LightingSidebarLightLoopTotalVO lightingSidebarLightLoopTotalVO = LightingSidebarLightLoopTotalVO.builder()
                    .funcType(entry.getKey())
                    .funcTypeName(funcTypeMap.get(entry.getKey()))
                    .demandOnNumber(allDemandOnNumber).realOnNumber(allRealOnNumber)
                    .moreOpenNumber(allMoreOpenNumber).lessOpenNumber(allLessOpenNumber)
                    .children(result).build();

            if (CollectionUtils.isNotEmpty(result)) {
                resultList.add(lightingSidebarLightLoopTotalVO);
            }
        }

        return resultList;
    }

    /**
     * 根据照明需求策略，处理分组数据并生成照明设备侧边栏状态VO列表
     * 1. 获取功能类型和灯具功能的代码-名称映射
     * 2. 对分组数据进行流式处理，逐级解析设备信息
     * 3. 通过processFuncTypeGroup方法进行具体业务处理
     * 4. 过滤无效结果并返回最终列表
     *
     * @param groupedData         分组后的照明设备数据（三级映射结构：功能类型 -> 灯具功能类型 -> 照明回路列表）
     * @param manualAutoStatusMap 设备手动/自动状态映射表（设备ID -> 状态值）
     * @return 处理后的照明设备状态VO列表，可用于侧边栏展示
     */
    private List<LightingSidebarManualAutomaticControlVO> listLightEquipmentControlByDemand(
            Map<String, Map<String, List<DtpRwdCircuitDTO>>> groupedData,
            Map<String, String> manualAutoStatusMap) {

        // 获取功能类型
        Map<String, String> funcTypeMap = demandLightingStrategy.mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.funcType);
        // 获取灯具功能类型
        Map<String, String> lampFuncTypeMap = demandLightingStrategy.mapCircuitInfoCodeAndName(DtpRwdCircuitDTO.Fields.lampFuncType);

        return groupedData.entrySet().stream()
                // 转换处理每个功能类型分组（funcType维度处理）
                .map(funcTypeEntry -> processFuncTypeGroup(
                        funcTypeEntry,
                        funcTypeMap,
                        lampFuncTypeMap,
                        manualAutoStatusMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    /**
     * 处理功能类型分组的照明设备状态信息
     * 1. 从功能类型分组中提取灯具功能类型分组的回路数据
     * 2. 对每个灯具功能类型分组进行统计计算，并汇总总统计信息
     * 3. 构建功能类型维度的侧边栏设备状态展示对象
     *
     * @param funcTypeEntry       功能类型分组条目（key: 功能类型编码，value: 按灯具功能类型分组的回路DTO列表）
     * @param funcTypeMap         功能类型编码与名称的映射
     * @param lampFuncTypeMap     灯具功能类型编码与名称的映射
     * @param manualAutoStatusMap 手动/自动状态编码映射
     * @return LightingSidebarEquipmentStatusVO 处理后的功能类型组状态视图对象
     */
    private LightingSidebarManualAutomaticControlVO processFuncTypeGroup(
            Map.Entry<String, Map<String, List<DtpRwdCircuitDTO>>> funcTypeEntry,
            Map<String, String> funcTypeMap,
            Map<String, String> lampFuncTypeMap,
            Map<String, String> manualAutoStatusMap) {

        // 获取当前功能类型编码及对应的灯具类型分组数据
        String funcType = funcTypeEntry.getKey();
        Map<String, List<DtpRwdCircuitDTO>> lampTypeGroups = funcTypeEntry.getValue();

        // 初始化返回结构：灯具类型结果列表和总统计对象
        List<LightingSidebarManualAutomaticControlVO> lampTypeResults = new ArrayList<>();
        LightingManualAutomaticControlStatistic totalStats = new LightingManualAutomaticControlStatistic();

        // 遍历每个灯具功能类型分组
        lampTypeGroups.forEach((lampFuncType, lampList) -> {
            // 1. 计算当前灯具类型的统计信息（手动/自动数量等）
            LightingManualAutomaticControlStatistic stats = calculateStats(lampList, manualAutoStatusMap);

            // 2. 将当前分组统计结果累加到总统计
            totalStats.accumulate(stats);

            // 3. 构建灯具功能类型维度的VO对象并加入结果列表
            lampTypeResults.add(buildLampTypeVO(lampFuncType, lampFuncTypeMap, stats));
        });

        // 返回最终结果：若无数据返回null，否则构建功能类型组VO
        return lampTypeResults.isEmpty() ? null :
                buildFuncTypeVO(funcType, funcTypeMap, totalStats, lampTypeResults);
    }

    /**
     * 计算照明设备的手动/自动状态统计信息
     * 根据状态映射表(statusMap)对灯具列表进行分组统计，生成包含数量统计和ID列表的统计对象
     *
     * @param lampList  照明设备DTO列表，包含需要统计的照明设备基本信息
     * @param statusMap 设备状态映射表，key: 设备ID，value: 状态标识（"1"表示自动状态，"0"表示手动状态）
     * @return LightingHandAutomaticStatistic 统计结果对象，包含：
     * - 自动状态设备数量及其ID列表
     * - 手动状态设备数量及其ID列表
     */
    private LightingManualAutomaticControlStatistic calculateStats(List<DtpRwdCircuitDTO> lampList, Map<String, String> statusMap) {
        // 使用流API将设备列表按状态分为两组（true/false）
        Map<Boolean, List<String>> partitioned = lampList.stream()
                .filter(x -> statusMap.containsKey(x.getId()))
                // partitioningBy根据条件分组，映射设备ID到列表
                .collect(Collectors.partitioningBy(
                        // 分组条件：检查设备ID在statusMap中对应的值是否为"1"
                        dto -> "1".equals(statusMap.get(dto.getId())),
                        // 映射转换：将满足条件的DTO对象转换为设备ID，并收集到List中
                        Collectors.mapping(DtpRwdCircuitDTO::getId, Collectors.toList())
                ));

        // 构建统计结果对象
        return new LightingManualAutomaticControlStatistic(
                // 自动状态（true组）设备数量
                partitioned.getOrDefault(true, Collections.emptyList()).size(),
                // 自动状态设备ID列表（当无数据时返回空列表）
                partitioned.getOrDefault(true, Collections.emptyList()),
                // 手动状态（false组）设备数量
                partitioned.getOrDefault(false, Collections.emptyList()).size(),
                // 手动状态设备ID列表（当无数据时返回空列表）
                partitioned.getOrDefault(false, Collections.emptyList())
        );
    }

    /**
     * 构建灯具功能类型侧边栏设备状态值对象
     *
     * @param lampFuncType 灯具功能类型编码（可能为空）
     * @param typeMap      类型本地化映射表（key: 类型编码，value: 本地化名称）
     * @param stats        手动/自动统计数据（包含自动模式数量、手动模式数量及对应设备ID列表）
     * @return 包含以下信息的VO对象：
     * - 灯具功能类型编码
     * - 本地化的类型名称（通过getLocalizedTypeName获取）
     * - 自动模式统计信息（数量+ID列表）
     * - 手动模式统计信息（数量+ID列表）
     */
    private LightingSidebarManualAutomaticControlVO buildLampTypeVO(String lampFuncType,
                                                                    Map<String, String> typeMap, LightingManualAutomaticControlStatistic stats) {
        return LightingSidebarManualAutomaticControlVO.builder()
                .lampFuncType(lampFuncType)
                .lampFuncTypeName(getLocalizedTypeName(lampFuncType, typeMap))
                .auto(new LightingManualAutoVo(stats.getAutoCount(), stats.getAutoIds()))
                .manualOperation(new LightingManualAutoVo(stats.getManualCount(), stats.getManualIds()))
                .build();
    }

    /**
     * 构建功能类型侧边栏设备状态值对象（支持树形结构）
     *
     * @param funcType   功能类型编码
     * @param typeMap    类型本地化映射表（key: 类型编码，value: 本地化名称）
     * @param totalStats 当前分类下的总统计信息（自动/手动模式总数）
     * @param children   子节点列表（用于构建设备状态树形结构）
     * @return 包含以下信息的VO对象：
     * - 功能类型编码
     * - 功能类型名称（直接从typeMap获取，未配置时显示"未配置"）
     * - 自动模式统计信息（总数+ID列表）
     * - 手动模式统计信息（总数+ID列表）
     * - 子节点列表（形成树形结构）
     */
    private LightingSidebarManualAutomaticControlVO buildFuncTypeVO(String funcType,
                                                                    Map<String, String> typeMap,
                                                                    LightingManualAutomaticControlStatistic totalStats,
                                                                    List<LightingSidebarManualAutomaticControlVO> children) {
        return LightingSidebarManualAutomaticControlVO.builder()
                .funcType(funcType)
                .funcTypeName(typeMap.getOrDefault(funcType, DemandLightingTypeSettingModeEnum.NOT_CONFIGURED.getReasonPhrase()))
                .auto(new LightingManualAutoVo(totalStats.getAutoCount(), totalStats.getAutoIds()))
                .manualOperation(new LightingManualAutoVo(totalStats.getManualCount(), totalStats.getManualIds()))
                .children(children)
                .build();
    }

    /**
     * 获取类型名称
     *
     * @param typeCode 类型编码（可能为null或空）
     * @param typeMap  类型本地化映射表（key: 类型编码，value: 本地化名称）
     * @return 返回以下值按优先级：
     * 当typeCode有效且在typeMap中存在时 -> 配置的名称
     * 当typeCode为空或空白时 -> "未配置"
     */
    private String getLocalizedTypeName(String typeCode, Map<String, String> typeMap) {
        return Optional.ofNullable(typeCode)
                .filter(StringUtils::isNotBlank)
                .map(code -> typeMap.getOrDefault(code, DemandLightingTypeSettingModeEnum.NOT_CONFIGURED.getReasonPhrase()))
                .orElse(DemandLightingTypeSettingModeEnum.NOT_CONFIGURED.getReasonPhrase());
    }


    /**
     * 回路运行偏差计算
     *
     * @param projectId    项目ID
     * @param templateType 需求类模版型
     * @param circuitIds   回路集合
     * @return {@code Map<String,LightingCircuitTagVO>}
     * @author yuecaipu
     * @since V5_241201
     */
    public Map<String, LightingCircuitTagVO> mapCircuitIdAndTag(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<String> circuitIds) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Maps.newHashMap();
        }
        // 回路需求配置
        Map<String, DemandConfigLightingResultDataDTO> circuitIdAndConfigResult =
                mapCircuitIdAndConfigResult(projectId, templateType, circuitIds);

        Map<String, Map<String, Object>> circuitIdAndInfoCodeValues = mapCircuitIdAndInfoCodeValues(
                circuitIds, InfoCodeEnum.CONTROL_DEVIATION_STATUS.value());

        // 回路标签计算
        return mapCircuitIdAndTag(circuitIds, circuitIdAndConfigResult, circuitIdAndInfoCodeValues);
    }


    /**
     * 回路运行偏差计算
     *
     * @param circuitIds                 回路集合
     * @param circuitIdAndConfigResult   回路配置设定map
     * @param circuitIdAndInfoCodeValues 照明回路信息点值
     * @return {@code Map<String,LightingCircuitTagVO>}
     * @author yuecaipu
     * @since V5_241201
     */
    public Map<String, LightingCircuitTagVO> mapCircuitIdAndTag(
            Collection<String> circuitIds,
            Map<String, DemandConfigLightingResultDataDTO> circuitIdAndConfigResult,
            Map<String, Map<String, Object>> circuitIdAndInfoCodeValues) {
        Map<String, LightingCircuitTagVO> result = new HashMap<>(1024);
        for (String circuitId : circuitIds) {
            Map<String, Object> infoCodeAndValue = circuitIdAndInfoCodeValues.get(circuitId);
            LightingCircuitTagVO circuitTag = new LightingCircuitTagVO();
            DemandConfigLightingResultDataDTO circuitSettingMode = circuitIdAndConfigResult.get(circuitId);
            // 运行偏差状态
            DtpKnowledgeControlDeviationStatusEnum controlDeviationStatusEnum =
                    DtpKnowledgeControlDeviationStatusEnum.analyze(infoCodeAndValue);
            circuitTag.setRunningDeviationType(LightingRunningDeviationTypeEnum.of(controlDeviationStatusEnum));
            if (Objects.nonNull(circuitSettingMode)) {
                DemandLightingCircuitSettingModeEnum settingMode = circuitSettingMode.getCircuitSettingMode();
                if (Objects.nonNull(settingMode)) {
                    if (settingMode == DemandLightingCircuitSettingModeEnum.FEEDBACK_ADJUSTMENT) {
                        circuitTag.setFeedbackAdjustSettingConfig(circuitSettingMode.getFeedbackAdjustSettings());
                    }
                }
            }
            result.put(circuitId, circuitTag);
        }
        return result;
    }

    /**
     * 查询照明回路ID和需求配置模式映射
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param circuitIds   照明回路ID
     * @return 照明回路ID -> 需求配置模式
     * @since 5.240501.0
     */
    public Map<String, DemandConfigLightingResultDataDTO> mapCircuitIdAndConfigResult(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> circuitIds) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Maps.newHashMap();
        }
        Map<String, DemandConfigLightingResultDataDTO> result = new HashMap<>(Math.max(circuitIds.size(), 16));
        circuitIds.forEach(item -> result.put(item, new DemandConfigLightingResultDataDTO()
                .setCircuitSettingMode(DemandLightingCircuitSettingModeEnum.CLOSE)));

        // 获取当前干预需求配置
        Map<String, DemandLightingCircuitSettingModeEnum> interventionConfigs = interventionConfigResourceService
                .mapCircuitIdAndSettingMode(projectId, templateType.toRunningMonitorSecondTypeEnum());
        interventionConfigs.forEach((circuitId, settingMode) -> result.put(circuitId,
                new DemandConfigLightingResultDataDTO().setCircuitSettingMode(settingMode)));
        // 获取当前场景自动需求配置
        LightingSceneDTO scene = demandLightingStrategy.getSceneByNow(projectId, templateType);
        if (Objects.isNull(scene)) {
            return result;
        }
        List<DemandConfigLightingResult> lightingResults = demandConfigLightingResultService.list(projectId,
                new DemandConfigLightingResultQueryDaoParam()
                        .setRelDemandTemplateId(scene.getRelDemandTemplateId())
                        .setRelDemandLightingSceneId(scene.getSceneId()));
        if (CollectionUtils.isEmpty(lightingResults)) {
            return result;
        }
        Map<String, DemandConfigLightingResultDataDTO> circuitIdAndConfig = lightingResults.stream()
                .collect(Collectors.toMap(DemandConfigLightingResult::getCircuitId,
                        DemandConfigLightingResult::getConfigData, (o1, o2) -> o1));


        for (String circuitId : circuitIds) {
            // 干预优先级干预自动需求
            if (interventionConfigs.containsKey(circuitId)) {
                continue;
            }
            DemandConfigLightingResultDataDTO config = circuitIdAndConfig.get(circuitId);
            if (Objects.isNull(config)) {
                continue;
            }
            DemandLightingCircuitSettingModeEnum circuitSettingMode = config.getCircuitSettingMode();
            if (Objects.nonNull(circuitSettingMode)
                    && circuitSettingMode != DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED) {
                result.put(circuitId, config);
            }
        }
        return result;
    }

    public Map<String, DemandStatusDTO> mapCircuitIdAndDemandStatus(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> circuitIds) {
        if (CollectionUtils.isEmpty(circuitIds)) {
            return Collections.emptyMap();
        }
        // 配置默认关闭
        Map<String, DemandStatusDTO> result = new HashMap<>(Math.max(circuitIds.size(), 16));
        circuitIds.forEach(item -> result.put(item, new DemandStatusDTO(item, DemandLightingCircuitSettingModeEnum.CLOSE)));

        // 获取当前干预需求配置
        Map<String, DemandLightingCircuitSettingModeEnum> interventionConfigs = interventionConfigResourceService
                .mapCircuitIdAndSettingMode(projectId, templateType.toRunningMonitorSecondTypeEnum());
        interventionConfigs.forEach((k, v) -> {
            DemandStatusDTO dto = result.get(k);
            dto.setSettingMode(v);
            dto.setDemandSource(DemandSourceEnum.DEMAND_INTERVENTION);
        });
        // 获取当前场景自动需求配置
        LightingSceneDTO scene = demandLightingStrategy.getSceneByNow(projectId, templateType);
        if (!Objects.isNull(scene)) {

            List<DemandConfigLightingResult> lightingResults = demandConfigLightingResultService.list(projectId,
                    new DemandConfigLightingResultQueryDaoParam()
                            .setRelDemandTemplateId(scene.getRelDemandTemplateId())
                            .setRelDemandLightingSceneId(scene.getSceneId()));
            if (CollectionUtils.isNotEmpty(lightingResults)) {
                Map<String, DemandConfigLightingResultDataDTO> circuitIdAndConfig = lightingResults.stream()
                        .collect(Collectors.toMap(DemandConfigLightingResult::getCircuitId,
                                DemandConfigLightingResult::getConfigData, (o1, o2) -> o1));


                for (String circuitId : circuitIds) {
                    // 干预优先级干预自动需求
                    if (interventionConfigs.containsKey(circuitId)) {
                        continue;
                    }
                    DemandConfigLightingResultDataDTO config = circuitIdAndConfig.get(circuitId);
                    if (Objects.isNull(config)) {
                        continue;
                    }
                    DemandLightingCircuitSettingModeEnum circuitSettingMode = config.getCircuitSettingMode();
                    if (Objects.nonNull(circuitSettingMode)) {
                        DemandStatusDTO dto = result.get(circuitId);
                        dto.setSettingMode(circuitSettingMode);
                        dto.setDemandSource(DemandSourceEnum.DEMAND_CONFIG);
                    }
                }
            }
        }

        // 手动状态
        // noinspection unchecked
        List<IotDataCommon> list = abstractIotDataCacheServer.queryIotRealDataListByList(projectId, circuitIds, Collections.singletonList(InfoCodeEnum.MANUAL_AUTO_SET.getCode()));
        if (!CollectionUtils.isEmpty(list)) {
            for (IotDataCommon iotDataCommon : list) {
                if (Objects.equals(iotDataCommon.getDataValue(), "0")) {
                    result.get(iotDataCommon.getObjectId()).setDemandSource(DemandSourceEnum.MANUAL_CONTROL);
                }
            }
        }
        return result;
    }

    /**
     * 前后需求变化开关结果计算
     *
     * @param lightingSceneResultList 需求变化
     * @author yuecaipu
     * @since v5.240901
     */
    private void openOffResultCompute(List<LightingSidebarDemandResult> lightingSceneResultList) {
        if (CollectionUtils.isNotEmpty(lightingSceneResultList)) {
            if (lightingSceneResultList.size() > 1) {
                for (int i = 0; i < lightingSceneResultList.size(); i++) {
                    LightingSidebarDemandResult thisDemand;
                    LightingSidebarDemandResult nextDemand;
                    // 非最后一个场景，则为当前减下一个场景，为最后一个场景，则为上一个场景减当前场景
                    if (i < lightingSceneResultList.size() - 1) {
                        thisDemand = lightingSceneResultList.get(i);
                        nextDemand = lightingSceneResultList.get(i + 1);
                    } else {
                        thisDemand = lightingSceneResultList.get(i - 1);
                        nextDemand = lightingSceneResultList.get(i);
                    }
                    // 当前场景，需求结果集
                    Map<String, List<DemandConfigLightingResult>> thisMap = getDemandResultList(thisDemand);
                    // 下一场景，需求结果集
                    Map<String, List<DemandConfigLightingResult>> nextMap = getDemandResultList(nextDemand);

                    // 当前需求场景和下一场景比较的切换的数量
                    Map<String, Long> resultMap = compareDeviceStatuses(thisMap, nextMap);
                    if (i < lightingSceneResultList.size() - 1) {
                        thisDemand.setOpenLoopNumber(resultMap.get(NEW_OPEN));
                        thisDemand.setOffLoopNumber(resultMap.get(NEW_CLOSE));
                    } else {
                        nextDemand.setOpenLoopNumber(resultMap.get(NEW_OPEN));
                        nextDemand.setOffLoopNumber(resultMap.get(NEW_CLOSE));
                    }
                }
            }
        }
    }

    /**
     * 对象中取出照明结果表
     *
     * @param demandInfo 照明需求场景信息
     * @return 按id进行分组后的需求结果集
     */
    private Map<String, List<DemandConfigLightingResult>> getDemandResultList(LightingSidebarDemandResult demandInfo) {
        List<DemandConfigLightingResult> nextList = demandInfo.getDemandConfigLightingResultList();
        Map<String, List<DemandConfigLightingResult>> resultMap = new HashMap<>(nextList.size());
        if (CollectionUtils.isNotEmpty(nextList)) {
            resultMap = nextList.stream().collect(Collectors.groupingBy(DemandConfigLightingResult::getCircuitId));
        }
        return resultMap;
    }


    private Map<String, Long> compareDeviceStatuses(Map<String, List<DemandConfigLightingResult>> currentMap,
                                                    Map<String, List<DemandConfigLightingResult>> nextMap) {
        // 使用EnumMap来映射状态改变
        Map<String, Long> statusChanges = new HashMap<>(16);
        statusChanges.put(NEW_OPEN, 0L);
        statusChanges.put(NEW_CLOSE, 0L);
        if (MapUtils.isEmpty(currentMap) && MapUtils.isNotEmpty(nextMap)) {
            nextMap.forEach((id, nextSceneList) -> {
                if (nextSceneList != null) {
                    if (CollectionUtils.isNotEmpty(nextSceneList)) {
                        DemandConfigLightingResult nextLightingResult = nextSceneList.get(0);
                        if (nextLightingResult.getConfigData().getCircuitSettingMode() == DemandLightingCircuitSettingModeEnum.OPEN) {
                            statusChanges.put(NEW_OPEN, statusChanges.get(NEW_OPEN) + 1);
                        }
                    }
                }
            });
            return statusChanges;
        }
        if (MapUtils.isNotEmpty(currentMap) && MapUtils.isEmpty(nextMap)) {
            statusChanges.put(NEW_CLOSE, (long) (int) currentMap.values().stream().flatMap(List::stream)
                    .filter(x -> x.getConfigData().getCircuitSettingMode() == DemandLightingCircuitSettingModeEnum.OPEN).count());
            return statusChanges;
        }

        // 假设两个场景中的设备ID都是一致的
        for (Map.Entry<String, List<DemandConfigLightingResult>> currentScene : currentMap.entrySet()) {
            String deviceId = currentScene.getKey();
            DemandConfigLightingResult currentLightingResult = currentScene.getValue().get(0);
            // 从场景B中获取相同ID的设备
            List<DemandConfigLightingResult> nextSceneList = nextMap.get(deviceId);

            if (nextSceneList != null) {
                if (CollectionUtils.isNotEmpty(nextSceneList)) {
                    DemandConfigLightingResult nextLightingResult = nextSceneList.get(0);
                    // 检查状态变化
                    if (currentLightingResult.getConfigData().getCircuitSettingMode() == DemandLightingCircuitSettingModeEnum.OPEN
                            && (nextLightingResult.getConfigData().getCircuitSettingMode() == DemandLightingCircuitSettingModeEnum.CLOSE
                            || nextLightingResult.getConfigData().getCircuitSettingMode() == DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED)) {
                        // 当前场景为开启，下一场景为关闭或未知，认为是新关闭
                        statusChanges.put(NEW_CLOSE, statusChanges.get(NEW_CLOSE) + 1);
                    } else if ((currentLightingResult.getConfigData().getCircuitSettingMode() == DemandLightingCircuitSettingModeEnum.CLOSE
                            || currentLightingResult.getConfigData().getCircuitSettingMode() == DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED)
                            && nextLightingResult.getConfigData().getCircuitSettingMode() == DemandLightingCircuitSettingModeEnum.OPEN) {
                        // 当前场景为关闭或未知，下一场景为开启，认为是新开启
                        statusChanges.put(NEW_OPEN, statusChanges.get(NEW_OPEN) + 1);
                    }
                }

            }
        }
        return statusChanges;
    }


    public String logTag() {
        return "照明服务";
    }
}
