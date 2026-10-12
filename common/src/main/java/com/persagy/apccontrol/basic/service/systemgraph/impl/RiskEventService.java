package com.persagy.apccontrol.basic.service.systemgraph.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.ObjectClassCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.IotInfoCodeFieldVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.ObjectIotVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphInformationVo;
import com.persagy.apccontrol.basic.outside.dto.riskevent.CheckDeviceInfoDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventEquipDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
import com.persagy.apccontrol.basic.outside.enums.ExceptionEventStatus;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.client.DtpKnowledgeServerClient;
import com.persagy.apccontrol.basic.outside.feign.client.ExceptionEventClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.ExceptionEventProblemParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.ExceptionEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.ExceptionEventOriginalProblemResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.ExceptionEventResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectInfoCodeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.structure.event.converter.AbnormalEventConverter;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.basic.utils.number.NumericalUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author houyuanzhi
 * @date 2024/2/20 16:24
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RiskEventService implements LogComponent {


    private final NacosSwitchConfig nacosSwitchConfig;
    private final ExceptionEventClient exceptionEventClient;
    private final DtpKnowledgeServerClient dmpKnowledgeClient;
    private final DtpRwdClientService dtpRwdClientService;
    private final CommonConfigService commonConfigService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final AbnormalEventConverter abnormalEventConverter;
    @Lazy
    @Autowired
    private AbstractCommonServer abstractCommonServer;


    // 信息点对象上限
    private static final int OBJECT_LIMIT = 20;

    /**
     * 查询空间的异常事件，
     * 如果传递时间则查询指定时间点范围内空间的异常事件，否则是所有未解决的异常事件
     *
     * @param spaceIds  空间ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 空间和设备的异常事件
     * @since 5.241101.0
     */
    public List<ExceptionEventResponse> listAbnormalEventBySpaceIdsWithEquipmentsAppointTime(@NonNull Collection<String> spaceIds,
                                                                                             @Nullable LocalDateTime startTime,
                                                                                             @Nullable LocalDateTime endTime,
                                                                                             @Nullable String objectType) {
        if (!nacosSwitchConfig.getYihengServerSwitch() || CollectionUtils.isEmpty(spaceIds)) {
            return Lists.newArrayList();
        }
        ExceptionEventQueryParam queryParam = new ExceptionEventQueryParam();
        queryParam.setExceptionEventStatus(ExceptionEventStatus.UNRESOLVED);
        queryParam.setSpaceIds(spaceIds);
        if (Objects.nonNull(startTime)) {
            queryParam.setCreateStartTime(startTime);
        }
        if (Objects.nonNull(endTime)) {
            queryParam.setCreateEndTime(endTime);
        }
        if (Objects.nonNull(objectType)) {
            queryParam.setObjectType(objectType);
        }
        List<ExceptionEventResponse> responses = exceptionEventClient.listExceptionEvents(queryParam);
        if (CollectionUtils.isEmpty(responses)) {
            return Lists.newArrayList();
        }
        return responses;
    }

    /**
     * 查询指定时间点范围内空间的异常事件(仅返回有异常空间的id)
     *
     * @param spaceIds  空间ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 空间id
     * @since 5.241201.0
     */
    public List<String> listAbnormalStateBySpaceIdsAppointTime(@NonNull Collection<String> spaceIds,
                                                               @Nullable LocalDateTime startTime,
                                                               @Nullable LocalDateTime endTime,
                                                               @Nullable List<String> firstRiskControlObjectives,
                                                               @Nullable List<String> secondRiskControlObjectives) {
        List<ExceptionEventResponse> listAbnormalEvent =
                listAbnormalEventBySpaceIdsWithEquipmentsAppointTime(spaceIds, startTime, endTime, ObjectTypeEnum.SPACE.value());
        if (CollectionUtils.isEmpty(listAbnormalEvent)) {
            return Lists.newArrayList();
        }
        ArrayList<String> resultIds = new ArrayList<>();
        listAbnormalEvent.forEach(abnormalEvent -> {
            if (filterAbnormalEvent(firstRiskControlObjectives, secondRiskControlObjectives, abnormalEvent)) {
                return;
            }
            // 满足条件的且为空间的异常事件
            abnormalEvent.getInstances().forEach(objectInfo -> {
                if (spaceIds.contains(objectInfo.getInstanceId())) {
                    resultIds.add(objectInfo.getInstanceId());
                }
            });
        });
        return resultIds;
    }

    /**
     * 过滤风控目标
     *
     * @param firstRiskControlObjectives  一级风控目标
     * @param secondRiskControlObjectives 二级风控目标
     * @param abnormalEvent               异常事件
     * @return 异常事件的状态
     */
    private boolean filterAbnormalEvent(@Nullable List<String> firstRiskControlObjectives,
                                        @Nullable List<String> secondRiskControlObjectives,
                                        ExceptionEventResponse abnormalEvent) {
        if (CollectionUtils.isNotEmpty(firstRiskControlObjectives)) {
            // 一级风控目标为空则跳过
            if (CollectionUtils.isEmpty(abnormalEvent.getRiskMeasureAimLevel1Collect())) {
                return true;
            }
            // 一级风控目标不包含的跳过
            return abnormalEvent.getRiskMeasureAimLevel1Collect().stream().noneMatch(firstRiskControlObjectives::contains);
        }
        if (CollectionUtils.isNotEmpty(secondRiskControlObjectives)) {
            // 二级风控目标为空则跳过
            if (CollectionUtils.isEmpty(abnormalEvent.getRiskMeasureAimLevel2Collect())) {
                return true;
            }
            // 二级风控目标不包含的跳过
            return abnormalEvent.getRiskMeasureAimLevel2Collect().stream().noneMatch(secondRiskControlObjectives::contains);
        }
        return false;
    }

    private static final int BATCH_SIZE = 1000;
    private static final int TIMEOUT_SECONDS = 30;

    /**
     * 查询异常事件二次转换数据结构
     *
     * @param objectIds             对象id
     * @param riskMeasureAimLevel2s
     * @return 异常事件map集合
     * @auther 薛涛
     */
    public Map<String, List<ExceptionEventBaseInfoVO>> exceptionMapToObjectKey(@NonNull Collection<String> objectIds, @Nullable Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s) {
        Map<String, List<ExceptionEventBaseInfoVO>> exceptionEventObjMap = new HashMap<>(16);
        if (!nacosSwitchConfig.getYihengServerSwitch()) {
            return exceptionEventObjMap;
        }
        List<ExceptionEventOriginalProblemResponse> problemResponses = null;
        try {
            ExceptionEventProblemParam param = new ExceptionEventProblemParam(Sets.newHashSet(objectIds), riskMeasureAimLevel2s);
            problemResponses = objectIds.size() > BATCH_SIZE ?
                    fetchDataConcurrently(param) :
                    fetchDataDirectly(param);
        } catch (Exception e) {
            logProjectInstance().error("查询异常事件", e, Pair.of("objectIds", objectIds));
        }
        if (CollectionUtils.isEmpty(problemResponses)) {
            return exceptionEventObjMap;
        }
        exceptionEventObjMap = problemResponses.stream().map(abnormalEventConverter::toVO).collect(Collectors.groupingBy(ExceptionEventBaseInfoVO::getId));
        ExceptionEventBaseInfoVO.fillInstanceInfo(exceptionEventObjMap);
        return exceptionEventObjMap;
    }

    /**
     * 同时获取数据
     *
     * @param param 参数
     * @return {@code Map<String, List<ExceptionEventBaseInfoVO>> }
     * @since 7.250602.0
     */
    private List<ExceptionEventOriginalProblemResponse> fetchDataConcurrently(@NonNull ExceptionEventProblemParam param)
            throws InterruptedException, TimeoutException, ExecutionException {

        // 分页处理（优化内存使用）
        Iterable<List<String>> partitions = Iterables.partition(param.getRwdObjectIds(), BATCH_SIZE);
        Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2 = param.getRiskMeasureAimLevel2();

        // 并行任务处理
        List<CompletableFuture<List<ExceptionEventOriginalProblemResponse>>> futures =
                StreamSupport.stream(partitions.spliterator(), true)
                        .map(e -> createFetchTask(new ExceptionEventProblemParam(Sets.newHashSet(e), riskMeasureAimLevel2)))
                        .collect(Collectors.toList());

        // 组合结果（优化合并性能）
        CompletableFuture<List<ExceptionEventOriginalProblemResponse>> combinedFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.parallelStream()
                                .map(CompletableFuture::join)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()));

        return combinedFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 并行任务处理
     *
     * @param param 查询异常事件参数
     * @return 线程
     */
    private CompletableFuture<List<ExceptionEventOriginalProblemResponse>> createFetchTask(ExceptionEventProblemParam param) {
        return CompletableFuture.supplyAsync(() ->
                        CustomerResponse.handleResult(exceptionEventClient.listExceptionEventProblem(param)),
                BusinessThreadPoolExecutor.ttlExecutor()
        ).exceptionally(ex -> {
            log.error("批次查询异常: {}", ex.getMessage());
            return Collections.emptyList();
        });
    }

    /**
     * 直接获取数据
     *
     * @param param 参数
     * @return {@code Map<String, List<ExceptionEventBaseInfoVO>> }
     * @since 7.250701.0
     */
    private List<ExceptionEventOriginalProblemResponse> fetchDataDirectly(ExceptionEventProblemParam param) {
        CustomerResponse<List<ExceptionEventOriginalProblemResponse>> listCustomerResponse = exceptionEventClient.listExceptionEventProblem(param);
        return CustomerResponse.handleResult(listCustomerResponse);
    }

    /**
     * 处理异常事件信息
     *
     * @param exceptionEventBaseInfoVo 单个异常事件信息
     */
    public static void processAbnormalEventInfo(ExceptionEventBaseInfoVO exceptionEventBaseInfoVo) {
        List<ExceptionEventBaseInfoVO.ExceptionInstance> instances = exceptionEventBaseInfoVo.getInstances();
        if (CollectionUtils.isNotEmpty(instances)) {
            String instanceName = instances.stream()
                    .map(ExceptionEventBaseInfoVO.ExceptionInstance::getInstanceName)
                    .collect(Collectors.joining("、")).replaceAll("null", "");
            exceptionEventBaseInfoVo.setInstancesName(instanceName);
        }
        List<ExceptionEventBaseInfoVO.ExceptionProblem> problems = exceptionEventBaseInfoVo.getProblems();
        if (CollectionUtils.isNotEmpty(problems)) {
            String problemsName = problems.stream()
                    .map(ExceptionEventBaseInfoVO.ExceptionProblem::getNodeName)
                    .collect(Collectors.joining("、")).replaceAll("null", "");
            exceptionEventBaseInfoVo.setProblemsName(problemsName);
        }
        int size = problems.size();
        if (size > 0) {
            exceptionEventBaseInfoVo.setProblemsNumber(size);
        }
    }


    /**
     * 查询异常事件和iot数据
     *
     * @param requestVos       请求数据vo
     * @param result           响应结果集
     * @param exceptionMap     异常事件map集合
     * @param equipmentDataMap 设备数据集合
     * @param taskMap          巡检信息map
     */
    private void getExceptionEventEquipAndIot(List<ObjectClassCodeDTO> requestVos,
                                              LinkedList<ExceptionEventEquipDTO> result,
                                              Map<String, List<ExceptionEventBaseInfoVO>> exceptionMap,
                                              Map<String, List<IotInfoCodeFieldVo>> equipmentDataMap,
                                              Map<String, TaskInfoDTO> taskMap) {
        List<String> objectIdList = requestVos.stream().map(ObjectClassCodeDTO::getObjectId).collect(Collectors.toList());
        DtpRwdObjectQueryParam spaceCriteria = new DtpRwdObjectQueryParam();
        spaceCriteria.setId(objectIdList);
        List<DtpRwdObjectDTO> objectInfoList = dtpRwdClientService.listObject(spaceCriteria);
        if (CollectionUtils.isNotEmpty(objectInfoList)) {
            objectInfoList.forEach(rwdEquipment ->
                    result.add(ExceptionEventEquipDTO.of(
                                    rwdEquipment,
                                    exceptionMap.get(rwdEquipment.getId()),
                                    equipmentDataMap.get(rwdEquipment.getId()),
                                    taskMap.get(rwdEquipment.getId())
                            )
                    )
            );
        }
    }

    /**
     * 获取设备iot数据
     *
     * @param iotDataRequestVos iot数据
     * @param hover             是否为hover
     * @param graphTypeCode     图名
     * @return 设备iot数据信息
     */
    public List<ObjectIotVo> getEquipmentInstanceIotInfo(List<ObjectClassCodeDTO> iotDataRequestVos,
                                                         String graphTypeCode,
                                                         String hover,
                                                         ObjectTypeEnum objectType) {
        List<String> objectIdList = iotDataRequestVos.stream().distinct().map(ObjectClassCodeDTO::getObjectId).collect(Collectors.toList());
        List<String> classCodeList = iotDataRequestVos.stream().distinct().map(ObjectClassCodeDTO::getClassCode)
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());

        Map<String, List<ObjectClassCodeDTO>> objectInfoMap = iotDataRequestVos.stream().collect(Collectors.groupingBy(ObjectClassCodeDTO::getObjectId));

        LinkedList<ObjectIotVo> result = new LinkedList<>();

        if (ObjectTypeEnum.SPACE == objectType) {
            setSpaceInfoList(objectIdList, result);
        } else {
            setEquipmentInfoList(hover, graphTypeCode, classCodeList, objectIdList, objectInfoMap, result);
        }
        return result;
    }

    /**
     * 空间环境参数
     */
    private static final ArrayList<String> INFO_CODE_LIST = Lists.newArrayList(
            InfoCodeEnum.SPACE_TDB.getCode(),
            InfoCodeEnum.SPACE_CO2.getCode(),
            InfoCodeEnum.RH.getCode(),
            InfoCodeEnum.PM2D5.getCode(),
            InfoCodeEnum.PM10.getCode(),
            InfoCodeEnum.CO.getCode(),
            InfoCodeEnum.VOC.getCode(),
            InfoCodeEnum.TVOC.getCode(),
            InfoCodeEnum.HCHO.getCode()
    );

    /**
     * 一位空间信息点
     */
    private static final ArrayList<String> ONE_DECIMAL_PLACE_INFO_CODE = Lists.newArrayList(
            InfoCodeEnum.SPACE_TDB.getCode(),
            InfoCodeEnum.SPACE_CO2.getCode(),
            InfoCodeEnum.RH.getCode()
    );

    /**
     * 设置空间信息点
     *
     * @param objectIdList 对象id集合
     * @param result       响应信息
     */
    public void setSpaceInfoList(List<String> objectIdList,
                                 LinkedList<ObjectIotVo> result) {
        Map<String, List<InfoCodeValueVO>> responseDtoMap = queryRwdInfoMap(objectIdList, INFO_CODE_LIST);

        objectIdList.forEach(objectId -> {
            ArrayList<IotInfoCodeFieldVo> resultList = new ArrayList<>();
            List<InfoCodeValueVO> dataList = responseDtoMap.getOrDefault(objectId, new ArrayList<>());
            Map<String, List<InfoCodeValueVO>> infoDataMap = dataList.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getInfoCode));
            INFO_CODE_LIST.forEach(infoCode -> {
                IotInfoCodeFieldVo iotFieldVo = new IotInfoCodeFieldVo();
                InfoCodeEnum info = InfoCodeEnum.getInfo(infoCode);
                iotFieldVo.setCode(info.getCode());
                iotFieldVo.setName(info.getDesc());
                iotFieldVo.setUnit(info.getUnit());
                iotFieldVo.setInfoType(LocaleMessageUtil.getMessage("key_54fedcd29d9e414391c96fa0b36a6b4f"));
                if (infoDataMap.containsKey(infoCode)) {
                    iotFieldVo.setDataValue(
                            NumericalUtils.formatNumberWithDecimal(
                                    String.valueOf(infoDataMap.get(infoCode).get(0).getInfoCodeValue()),
                                    ONE_DECIMAL_PLACE_INFO_CODE.contains(infoCode) ? 1 : 2
                            )
                    );
                }
                resultList.add(iotFieldVo);
            });
            result.add(ObjectIotVo.builder().objectId(objectId).iotInfoCodeFieldVos(resultList).build());
        });
    }

    /**
     * 设置设备信息
     *
     * @param hover         hover选中状态
     * @param graphTypeCode 图名
     * @param classCodeList 对象编码集合
     * @param objectIdList  对象id集合
     * @param objectInfoMap 对象信息点map集合
     * @param result        响应结果集
     */
    private void setEquipmentInfoList(String hover,
                                      String graphTypeCode,
                                      List<String> classCodeList,
                                      List<String> objectIdList,
                                      Map<String, List<ObjectClassCodeDTO>> objectInfoMap,
                                      LinkedList<ObjectIotVo> result) {
        Map<String, DtpKnowledgeObjectInfoCodeResponse> konwledgeInfoCodeMap = new LinkedHashMap<>();
        List<GraphInformationVo> graphInformationGroupByObjectClass = commonConfigService.getGraphInformation();
        List<GraphInformationVo> graphInformationVos = graphInformationGroupByObjectClass.stream()
                .filter(info -> StringUtils.isBlank(hover) || hover.equals(info.getHover()))
                .filter(info -> StringUtils.isEmpty(graphTypeCode) || info.getApplicationDrawing().contains(graphTypeCode))
                .filter(info -> classCodeList.contains(info.getClassCode())).collect(Collectors.toList());
        Map<String, List<GraphInformationVo>> classCodeInfoMap = graphInformationVos.stream().collect(Collectors.groupingBy(GraphInformationVo::getClassCode));

        Map<String, List<GraphInformationVo>> collect = graphInformationVos.stream().collect(Collectors.groupingBy(GraphInformationVo::getInfoCode));

        List<String> infoCodeList = graphInformationVos.stream().map(GraphInformationVo::getInfoCode).collect(Collectors.toList());
        int startIndex = 0;
        // 查询信息点
        Map<String, List<DtpKnowledgeObjectInfoCodeResponse>> dmpKnowledgeMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(classCodeList)) {
            while (startIndex < classCodeList.size()) {
                int endIndex = Math.min(startIndex + OBJECT_LIMIT, classCodeList.size());
                List<String> subset = classCodeList.subList(startIndex, endIndex);
                dmpKnowledgeMap.putAll(dmpKnowledgeClient.listInfoByClassCodes(subset));
                startIndex += OBJECT_LIMIT;
            }
        }

        classCodeList.forEach(code -> {
            if (dmpKnowledgeMap.containsKey(code)) {
                konwledgeInfoCodeMap.putAll(dmpKnowledgeMap.get(code).stream()
                        .filter(it -> infoCodeList.contains(it.getCode()))
                        .collect(Collectors.toMap(DtpKnowledgeObjectInfoCodeResponse::getCode, it -> it)));
            }
        });

        Map<String, List<InfoCodeValueVO>> responseDtoMap = queryRwdInfoMap(objectIdList, infoCodeList);

        if (MapUtils.isNotEmpty(responseDtoMap)) {
            responseDtoMap.forEach((objectId, dataList) -> {
                String classCode = objectInfoMap.get(objectId).get(0).getClassCode();
                ArrayList<IotInfoCodeFieldVo> resultList = new ArrayList<>();
                List<GraphInformationVo> informationVoList = classCodeInfoMap.getOrDefault(classCode, new ArrayList<>());
                Map<String, List<InfoCodeValueVO>> infoDataMap = dataList.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getInfoCode));
                informationVoList.forEach(x -> {
                    if (infoDataMap.containsKey(x.getInfoCode())) {
                        resultList.add(IotInfoCodeFieldVo.of(infoDataMap.get(x.getInfoCode()).get(0), konwledgeInfoCodeMap.get(x.getInfoCode()), collect));
                    } else {
                        setNullInfoCode(resultList, x, objectId, konwledgeInfoCodeMap.get(x.getInfoCode()), collect);
                    }
                });
                result.add(ObjectIotVo.builder().objectId(objectId).iotInfoCodeFieldVos(resultList).build());
            });
        } else {
            objectIdList.forEach(objectId -> {
                ArrayList<IotInfoCodeFieldVo> resultList = new ArrayList<>();
                classCodeInfoMap.getOrDefault(objectInfoMap.get(objectId).get(0).getClassCode(), new ArrayList<>())
                        .forEach(x -> {
                            setNullInfoCode(resultList, x, objectId, konwledgeInfoCodeMap.get(x.getInfoCode()), collect);
                        });
                result.add(ObjectIotVo.builder().objectId(objectId).iotInfoCodeFieldVos(resultList).build());
            });
        }
    }


    /**
     * 查询rwd信息返回map集合
     *
     * @param objectIdList 对象id集合
     * @param infoCodeList 信息点集合
     * @return 对象id -> rwd持久化数据
     */
    private Map<String, List<InfoCodeValueVO>> queryRwdInfoMap(List<String> objectIdList, List<String> infoCodeList) {
        List<InfoCodeValueVO> energyDataResponses = queryRwdInfoData(objectIdList, infoCodeList);

        Map<String, Map<String, InfoCodeValueVO>> latestDataByObjectAndPoint = energyDataResponses.stream()
                .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId, // 首先按照对象ID分组
                        Collectors.toMap(InfoCodeValueVO::getInfoCode, // 再按照信息点进行分组
                                d -> d, // 以对象本身为值
                                (existing, replacement) -> Objects.requireNonNull(existing.getUpdateTime())
                                        .isAfter(Objects.requireNonNull(replacement.getUpdateTime())) ? existing : replacement))); // 选择时间更晚的数据
        // 将Map中的DataPoint对象提取出来放入List中
        List<InfoCodeValueVO> latestDataList = latestDataByObjectAndPoint.values().stream()
                .flatMap(m -> m.values().stream())
                .collect(Collectors.toList());

        return latestDataList.stream()
                .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));
    }

    /**
     * 查询实时rwd信息点数据
     *
     * @param equipIdList 设备id集合
     * @param infoCodes   信息点
     * @return rwd实时信息数据
     */
    public List<InfoCodeValueVO> queryRwdInfoData(List<String> equipIdList, List<String> infoCodes) {
        try {
            List<InfoCodeValueVO> data = this.abstractIotDataCacheServer.listInfoCodeValue(ContextUtil.getProjectId(), null, equipIdList, infoCodes);
            data.sort(Comparator.comparing(InfoCodeValueVO::getDataTime).reversed());

            Map<String, Map<String, InfoCodeValueVO>> latestDataByObjectAndPoint = data.stream()
                    .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId, // 首先按照对象ID分组
                            Collectors.toMap(InfoCodeValueVO::getInfoCode, // 再按照信息点进行分组
                                    d -> d, // 以对象本身为值
                                    (existing, replacement) ->
                                            Objects.requireNonNull(existing.getDataTime())
                                                    .isAfter(Objects.requireNonNull(replacement.getDataTime()))
                                                    ? existing : replacement))); // 选择时间更晚的数据

            // 将Map中的DataPoint对象提取出来放入List中
            return latestDataByObjectAndPoint.values().stream()
                    .flatMap(m -> m.values().stream())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("DMP抛出异常", e);
        }
        return new ArrayList<>();
    }


    /**
     * 设置空信息点
     *
     * @param resultList 结果集
     * @param info       信息点数据
     * @param objectId   对象id
     * @param rwdInfo    rwd信息
     * @param collect    数据信息
     */
    private void setNullInfoCode(ArrayList<IotInfoCodeFieldVo> resultList,
                                 GraphInformationVo info,
                                 String objectId,
                                 DtpKnowledgeObjectInfoCodeResponse rwdInfo,
                                 Map<String, List<GraphInformationVo>> collect) {
        InfoCodeValueVO ipDataResponseDto = new InfoCodeValueVO();
        ipDataResponseDto.setInfoCode(info.getInfoCode());
        ipDataResponseDto.setObjectId(objectId);
        ipDataResponseDto.setInfoCodeValue(null);
        resultList.add(IotInfoCodeFieldVo.of(ipDataResponseDto, rwdInfo, collect));
    }

    /**
     * 异常事件信息
     *
     * @param checkDeviceInfoVo 异常事件信息
     * @return 异常事件集合
     */
    public List<ExceptionEventEquipDTO> queryRiskEventList(CheckDeviceInfoDTO checkDeviceInfoVo) {
        return getEquipmentAnomalousEvent(
                checkDeviceInfoVo.getIotDataRequestVos(),
                checkDeviceInfoVo.getResultIot(),
                checkDeviceInfoVo.getGraphTypeCode(),
                checkDeviceInfoVo.getHover(),
                checkDeviceInfoVo.getObjectType()
        );
    }

    /**
     * 设备实例异常事件信息和iot数据
     *
     * @param iotDataRequestVos iot请求入参
     * @param mapTitle          图名
     * @param resultIot         是否返回iot信息
     * @return 设备实例异常事件信息和iot数据
     */
    public List<ExceptionEventEquipDTO> getEquipmentAnomalousEvent(List<ObjectClassCodeDTO> iotDataRequestVos,
                                                                   Boolean resultIot,
                                                                   String mapTitle,
                                                                   String hover,
                                                                   ObjectTypeEnum objectType) {

        List<String> objectIds = iotDataRequestVos.stream().map(ObjectClassCodeDTO::getObjectId).collect(Collectors.toList());
        LinkedList<ExceptionEventEquipDTO> exceptionEventEquip = new LinkedList<>();
        Map<String, TaskInfoDTO> taskMap = new HashMap<>(0);
        try {
            taskMap = this.abstractCommonServer.mapTaskInfo(objectIds);
        } catch (Exception e) {
            log.error("查询巡检信息出错入参{},抛出异常", objectIds, e);
        }

        // 查询所有设备的异常状态信息
        Map<String, List<ExceptionEventBaseInfoVO>> exceptionMap = exceptionMapToObjectKey(objectIds, null);
        Map<String, List<IotInfoCodeFieldVo>> equipmentDataMap = new HashMap<>(1);
        if (resultIot) {
            // 查询iot信息
            equipmentDataMap = getEquipmentInstanceIotInfo(iotDataRequestVos, mapTitle, hover, objectType)
                    .stream()
                    .collect(Collectors.toMap(ObjectIotVo::getObjectId, ObjectIotVo::getIotInfoCodeFieldVos, (v1, v2) -> v2));
        }

        try {
            // 设备信息
            getExceptionEventEquipAndIot(iotDataRequestVos, exceptionEventEquip, exceptionMap, equipmentDataMap, taskMap);

        } catch (Exception e) {
            logProjectInstance().error("解析文件标签失败", e, Pair.of("iotDataRequestVos", iotDataRequestVos));
        }

        return exceptionEventEquip;
    }


    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240702.0
     */
    @Override
    public String logTag() {
        return "异常事件";
    }
}

