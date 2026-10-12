package com.persagy.apccontrol.basic.service.duty.strategy;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.QuerySystemCardParam;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.dashboard.SystemCardVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.duty.AirConditioningColdSourceCardDataVO;
import com.persagy.apccontrol.basic.model.vo.duty.AirConditioningColdSourceChildCardDataVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.ImportanceDegreeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.dashboard.DashboardSystemGraphService;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.iot.IotConverter;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 空调冷源值班首页看板
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
public class AirConditioningHeatSourcePageCardDataHandler
        extends AbstractHomePageCardDataHandler<AirConditioningColdSourceCardDataVO> {

    private static final String MENU_CODE = "ACAH";
    private static final DtpRwdClassCodeEnum SYSTEM_CLASS_CODE = DtpRwdClassCodeEnum.ACAH_SYSTEM;
    private static final Set<String> QUERY_SYSTEM_IOT_INFO_CODES = Sets.newHashSet(
            "heatWaterMainReturnTemp",
            "2heatWaterMainSupplyTemp", "2heatWaterMainReturnTemp"
    );

    private final IotConverter iotConverter;
    private final DemandCalendarQueryService demandCalendarQueryService;
    private final DashboardSystemGraphService dashboardSystemGraphService;
    private final DtpRwdClientService dtpRwdClientService;
    private final AbstractCommonServer abstractCommonServer;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    private static final Logger LOGGER = LoggerFactory.getLogger(AirConditioningHeatSourcePageCardDataHandler.class);
    private static final String LOG_TAG = "运行值班首页-空调热源";

    @Override
    public @NonNull RunningMonitorTypeEnum getRunningMonitorType() {
        return RunningMonitorTypeEnum.AIR_CONDITIONING_HEAT_SOURCE;
    }

    @Override
    @SneakyThrows
    public AirConditioningColdSourceCardDataVO execute(HomePageParam param) {
        // 查询系统
        List<DtpRwdObjectDTO> systems = querySystems();
        if (CollectionUtils.isEmpty(systems)) {
            return AirConditioningColdSourceCardDataVO.empty();
        }
        // 设置查询IOT时间
        CommonConst.checkSystemReplacementTime(RunningMonitorTypeEnum.AIR_CONDITIONING_HEAT_SOURCE);
        // 查询关系图
        Map<String, SystemCardVO> systemIdAndRelationGraph = querySystemIdAndRelationGraph();
        // 查询系统对象实例化规则
        Map<String, Set<String>> systemIdAndExistsRuleInfoCodes = querySystemIdAndExistsRuleInfoCodes(systems);
        // 查询系统对象IOT信息点值
        Map<String, Map<String, InfoCodeValueVO>> systemIdIotInfoCodeAndValues = querySystemIdInfoCodeAndValues(systems);
        // 查询信息点IOT定义
        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndDefined = dtpKnowledgeClientService
                .mapInfoCodeAndData(false, false, SYSTEM_CLASS_CODE.getValue());

        // 查询设备组ID -> 设备组下设备ID
        Map<String, Set<String>> groupIdAndEquipmentIds = queryGroupIdAndEquipmentIds(systemIdAndRelationGraph.values().stream()
                .filter(item -> Objects.nonNull(item) && CollectionUtils.isNotEmpty(item.getGraphObjectIds()))
                .flatMap(item -> item.getGraphObjectIds().stream())
                .collect(Collectors.toSet()));
        // 查询对象ID -> 对象异常事件
        Map<String, List<ExceptionEventBaseInfoVO>> objectIdAndExceptionEvents =
                queryObjectIdAndExceptionEvents(systemIdAndRelationGraph, groupIdAndEquipmentIds);
        
        List<AirConditioningColdSourceChildCardDataVO> childCards = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (DtpRwdObjectDTO system : systems) {
            String systemId = system.getId();

            AirConditioningColdSourceChildCardDataVO childCard = new AirConditioningColdSourceChildCardDataVO();
            childCards.add(childCard);

            futures.add(CompletableFuture.runAsync(() -> {
                Boolean isExistsSystemGraph = null;
                String graphTypeCode = null;
                Collection<String> graphObjectIds = Collections.emptyList();
                Map<String, InfoCodeValueVO> infoCodeAndValue = new LinkedHashMap<>();

                SystemCardVO relationGraph = systemIdAndRelationGraph.get(systemId);
                if (Objects.nonNull(relationGraph)) {
                    isExistsSystemGraph = relationGraph.getIsExistsSystemGraph();
                    graphTypeCode = relationGraph.getGraphTypeCode();
                    graphObjectIds = relationGraph.getGraphObjectIds();
                }
                // 统计异常事件数量
                int exceptionEventCount = statisticExceptionEventCount(systemId,
                        graphObjectIds, groupIdAndEquipmentIds, objectIdAndExceptionEvents);
                // 填充系统信息点
                Set<String> existsInfoCodes = systemIdAndExistsRuleInfoCodes.get(systemId);
                if (CollectionUtils.isNotEmpty(existsInfoCodes)) {
                    for (String existsInfoCode : existsInfoCodes) {
                        DtpKnowledgeInfoCodeDTO infoCodeDefine = infoCodeAndDefined.get(existsInfoCode);
                        InfoCodeValueVO infoCodeValue = systemIdIotInfoCodeAndValues.getOrDefault(systemId,
                                Collections.emptyMap()).get(existsInfoCode);
                        InfoCodeValueVO infoCodeValueVo = iotConverter.toInfoCodeValueVo(Optional.ofNullable(
                                infoCodeValue).orElseGet(InfoCodeValueVO::new), infoCodeDefine);
                        infoCodeValueVo.setObjectId(systemId);
                        infoCodeValueVo.setInfoCode(existsInfoCode);
                        infoCodeAndValue.put(existsInfoCode, infoCodeValueVo);
                    }
                }
                childCard.setSystem(system);
                childCard.setIsExistsSystemGraph(isExistsSystemGraph);
                childCard.setGraphTypeCode(graphTypeCode);
                childCard.setExceptionEventCount(exceptionEventCount);
                childCard.setInfoCodeAndValue(infoCodeAndValue);
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        
        // 查询需求模板
        CompletableFuture<DemandTemplateDateSettingVO> demandTemplateFuture = CompletableFuture.supplyAsync(() -> 
            demandCalendarQueryService.getDateSetting(
                DemandTemplateTypeEnum.AIRCONDITIONING, DateUtils.now().toLocalDate()),
            BusinessThreadPoolExecutor.ttlExecutor());
        
        // 等待所有任务完成
        CompletableFuture<Void> allTasksFuture = CompletableFuture.allOf(
            Stream.concat(futures.stream(), Stream.of(demandTemplateFuture))
                  .toArray(CompletableFuture[]::new));
                  
        try {
            allTasksFuture.join();
        } catch (CompletionException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "Error occurred during async execution", e);
        }
        
        AirConditioningColdSourceCardDataVO result = new AirConditioningColdSourceCardDataVO();
        result.setDemandTemplate(Objects.nonNull(demandTemplateFuture.getNow(null)) ? 
                                 demandTemplateFuture.getNow(null).getRelTemplate() : null);
                                 
        // 卡片排序
        List<AirConditioningColdSourceChildCardDataVO> sorted = childCards.stream().sorted(
                Comparator.comparing(
                        (AirConditioningColdSourceChildCardDataVO e) -> e.getSystem().getUndefinedProperties().get(InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.getCode()).toString(),
                        Comparator.nullsLast(String::compareTo).reversed()
                ).thenComparing((AirConditioningColdSourceChildCardDataVO e) -> e.getSystem().getLocalName(), Comparator.nullsLast(COLLATOR))
        ).collect(Collectors.toList());
        
        result.setChildCards(sorted);
        return result;
    }

    /**
     * 查询系统ID信息点规则映射
     *
     * @param systems 系统
     * @return 系统ID -> [信息点编码 -> 信息点规则]
     * @since 7.250601.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, Set<String>> querySystemIdAndExistsRuleInfoCodes(
            @NonNull List<DtpRwdObjectDTO> systems) {
        Set<String> systemIds = systems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Map<String, Set<String>> classCodeAndObjectIds = Collections.singletonMap(
                SYSTEM_CLASS_CODE.getValue(), systemIds);
        Map<String, Set<String>> systemIdAndExistsRuleInfoCodes = abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(classCodeAndObjectIds, QUERY_SYSTEM_IOT_INFO_CODES);
        if (MapUtils.isEmpty(systemIdAndExistsRuleInfoCodes)) {
            return Collections.emptyMap();
        }
        return systemIdAndExistsRuleInfoCodes;
    }

    /**
     * 查询系统ID信息点值映射
     *
     * @param systems 系统
     * @return 系统ID -> [信息点编码 -> 信息点值]
     * @since 7.250601.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, InfoCodeValueVO>> querySystemIdInfoCodeAndValues(
            @NonNull List<DtpRwdObjectDTO> systems) {
        Set<String> systemIds = systems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer
                .listInfoCodeValueInOneHour(systemIds, QUERY_SYSTEM_IOT_INFO_CODES);
        if (CollectionUtils.isEmpty(infoCodeValues)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, InfoCodeValueVO>> result = infoCodeValues.stream().collect(Collectors.groupingBy(
                InfoCodeValueVO::getObjectId, Collectors.toMap(InfoCodeValueVO::getInfoCode,
                        Function.identity(), (o1, o2) -> o1)));
        return result;
    }

    /**
     * 查询对象ID和异常事件映射
     *
     * @param systemIdAndRelationGraph 系统ID -> 关联系统图
     * @param groupIdAndEquipmentIds   设备组ID -> 设备组下设备ID
     * @return 对象ID -> 异常事件
     * @since 7.250601.0
     */
    private Map<String, List<ExceptionEventBaseInfoVO>> queryObjectIdAndExceptionEvents(
            @NonNull Map<String, SystemCardVO> systemIdAndRelationGraph,
            @NonNull Map<String, Set<String>> groupIdAndEquipmentIds) {
        if (MapUtils.isEmpty(systemIdAndRelationGraph)) {
            return Maps.newHashMap();
        }
        Set<String> objectIds = new HashSet<>();
        for (Map.Entry<String, SystemCardVO> entry : systemIdAndRelationGraph.entrySet()) {
            objectIds.add(entry.getKey());
            Collection<String> graphObjectIds = entry.getValue().getGraphObjectIds();
            if (CollectionUtils.isNotEmpty(graphObjectIds)) {
                for (String graphObjectId : graphObjectIds) {
                    Set<String> equipmentIdsInGroup = groupIdAndEquipmentIds.get(graphObjectId);
                    if (CollectionUtils.isNotEmpty(equipmentIdsInGroup)) {
                        objectIds.addAll(equipmentIdsInGroup);
                    }
                }
                objectIds.addAll(graphObjectIds);
            }
        }
        Map<String, List<ExceptionEventBaseInfoVO>> objectIdAndExceptionEvents =
                abstractCommonServer.mapRiskEvent(objectIds, null);
        if (MapUtils.isEmpty(objectIdAndExceptionEvents)) {
            return Collections.emptyMap();
        }
        Iterator<Map.Entry<String, List<ExceptionEventBaseInfoVO>>> iterator =
                objectIdAndExceptionEvents.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<ExceptionEventBaseInfoVO>> entry = iterator.next();
            Iterator<ExceptionEventBaseInfoVO> eventIterator = entry.getValue().iterator();
            while (eventIterator.hasNext()) {
                ExceptionEventBaseInfoVO exceptionEvent = eventIterator.next();
                ImportanceDegreeEnum importanceDegree = exceptionEvent.getImportanceDegree();
                if (importanceDegree != ImportanceDegreeEnum.HIGH) {
                    eventIterator.remove();
                }
            }
            if (CollectionUtils.isEmpty(entry.getValue())) {
                iterator.remove();
            }
        }
        return objectIdAndExceptionEvents;
    }

    /**
     * 统计异常事件数量
     *
     * @param systemId                   系统 ID
     * @param graphObjectIds             系统图对象ID
     * @param groupIdAndEquipmentIds     设备组ID -> 设备组下设备ID
     * @param objectIdAndExceptionEvents 对象ID -> 异常事件
     * @return 异常事件数量
     * @since 7.250601.0
     */
    private int statisticExceptionEventCount(
            @NonNull String systemId,
            @NonNull Collection<String> graphObjectIds,
            @NonNull Map<String, Set<String>> groupIdAndEquipmentIds,
            @NonNull Map<String, List<ExceptionEventBaseInfoVO>> objectIdAndExceptionEvents) {
        if (MapUtils.isEmpty(objectIdAndExceptionEvents)) {
            return 0;
        }
        Set<String> objectIds = new HashSet<>();
        objectIds.add(systemId);
        if (CollectionUtils.isNotEmpty(graphObjectIds)) {
            objectIds.addAll(graphObjectIds);
            for (String graphObjectId : graphObjectIds) {
                Set<String> equipmentIdsInGroup = groupIdAndEquipmentIds.get(graphObjectId);
                if (CollectionUtils.isNotEmpty(equipmentIdsInGroup)) {
                    objectIds.addAll(equipmentIdsInGroup);
                }
            }
        }
        int exceptionEventCount = 0;
        for (String objectId : objectIds) {
            List<ExceptionEventBaseInfoVO> events = objectIdAndExceptionEvents.get(objectId);
            if (CollectionUtils.isEmpty(events)) {
                continue;
            }
            exceptionEventCount += events.size();
        }
        return exceptionEventCount;
    }

    /**
     * 查询设备组下的设备
     *
     * @param graphObjectIds 系统图对象ID
     * @return 设备组ID -> 设备组下的设备
     * @since 7.250601.0
     */
    private Map<String, Set<String>> queryGroupIdAndEquipmentIds(@NonNull Collection<String> graphObjectIds) {
        if (CollectionUtils.isEmpty(graphObjectIds)) {
            return Maps.newHashMap();
        }
        // 查询设备组
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(graphObjectIds);
        queryParam.setObjType(DtpRwdObjectTypeEnum.EQUIPMENT_GROUP.value());
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        Set<String> equipmentGroupIds = dtpRwdClientService.listObject(queryParam).stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(equipmentGroupIds)) {
            return Maps.newHashMap();
        }
        // 查询设备组下的设备
        queryParam = new DtpRwdObjectQueryParam();
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(equipmentGroupIds));
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
        return DtpRwdObjectDTO.mapRelObjIdAndIds(dtpRwdClientService.listEquipment(queryParam));
    }

    /**
     * 查询系统ID和关联系统图
     *
     * @return 系统ID -> 关联系统图
     * @since 7.250601.0
     */
    private Map<String, SystemCardVO> querySystemIdAndRelationGraph() {
        List<SystemCardVO> systemCards = dashboardSystemGraphService.listSystemByFirstMenuCode(
                MENU_CODE, new QuerySystemCardParam());
        if (CollectionUtils.isEmpty(systemCards)) {
            return Maps.newHashMap();
        }
        return systemCards.stream().collect(Collectors.toMap(SystemCardVO::getId,
                Function.identity(), (o1, o2) -> o1));
    }

    /**
     * 查询系统
     *
     * @return 系统
     * @since 7.250601.0
     */
    private List<DtpRwdObjectDTO> querySystems() {
        return dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                .setClassCodeEnum(SYSTEM_CLASS_CODE)
                .filterMonitoringSurveillanceStatus()
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName, InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.getCode()));
    }
}
