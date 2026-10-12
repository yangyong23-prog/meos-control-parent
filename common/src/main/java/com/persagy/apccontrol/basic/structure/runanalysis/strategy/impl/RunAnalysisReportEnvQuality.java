package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.ClassCodeDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.runanalysis.BasicsAnalysisParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.AirConditionerSpaceAnalysisVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.SpacesAnalysis;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.runanalysis.RunAnalysisService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.domain.identifier.DemandConfigIdentifier;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningEnvironmentQualityObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbQualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.ExceptionRecordDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.envquality.EnvQualityDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.envquality.EnvQualitySpaceDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.envquality.EnvQualitySpaceDetailDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.envquality.EnvQualitySpaceRelObjectDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.groovy.util.Maps;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 环境品质策略
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportEnvQuality extends AbstractRunAnalysisReportStrategy<EnvQualityDTO> {

    private final DtpRwdClientService dtpRwdClientService;
    private final RunAnalysisService runAnalysisService;

    private final Map<String, List<String>> classCode2InfoCodes = Maps.of(
            DtpRwdClassCodeEnum.ACATAH.getValue(), Lists.newArrayList(
                    InfoCodeEnum.EQUIP_SWITCH_SET.getCode(),
                    InfoCodeEnum.RUN_STATUS.getCode(),
                    InfoCodeEnum.T_TARGET_SET.getCode(),
                    InfoCodeEnum.T_TARGET.getCode(),
                    InfoCodeEnum.SUPPLY_AIR_TEMP_SET.getCode(),
                    InfoCodeEnum.SUPPLY_AIR_TEMP.getCode()
            ),
            DtpRwdClassCodeEnum.ACATFU.getValue(), Lists.newArrayList(
                    InfoCodeEnum.EQUIP_SWITCH_SET.getCode(),
                    InfoCodeEnum.RUN_STATUS.getCode(),
                    InfoCodeEnum.T_TARGET_SET.getCode(),
                    InfoCodeEnum.T_TARGET.getCode()
            ),
            DtpRwdClassCodeEnum.ACATFC.getValue(), Lists.newArrayList(
                    InfoCodeEnum.EQUIP_SWITCH_SET.getCode(),
                    InfoCodeEnum.RUN_STATUS.getCode(),
                    InfoCodeEnum.T_TARGET_SET.getCode(),
                    InfoCodeEnum.T_TARGET.getCode()
            )
    );

    @Override
    public Boolean isSupport(@NonNull RunAnalysisReportTypeEnum type) {
        return type == RunAnalysisReportTypeEnum.DAILY;
    }

    @Override
    public EnvQualityDTO buildWithConfig(
            @NonNull RunAnalysisReportBuildParam param,
            RunAnalysisReportConfig config,
            Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData)
            throws ExecutionException, InterruptedException {
        LocalDate date = param.getReportDate();
        logInstance().info("开始构建报告", Arrays.asList(
                Pair.of("date", date),
                Pair.of("allConfigData", allConfigData)
        ));
        long startTime = System.currentTimeMillis();
        // 查询空间ID
        CompletableFuture<Set<String>> spaceIdsFuture = CompletableFuture.supplyAsync(
                () -> this.querySpaceIds(date, allConfigData), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询空间数据
        CompletableFuture<EnvQualitySpaceDTO> space = spaceIdsFuture.thenApplyAsync(spaceIds ->
                this.querySpaceComplianceRate(date, spaceIds), BusinessThreadPoolExecutor.ttlExecutor());

        CompletableFuture.allOf(spaceIdsFuture, space).get();

        if (CollectionUtils.isEmpty(spaceIdsFuture.get())) {
            logProjectInstance().warn("无有效空间，无法构建报告", Pair.of("date", date));
            return null;
        }

        EnvQualityDTO result = new EnvQualityDTO();
        result.setSpace(space.get());
        logProjectInstance().info("构建-报告完成", Arrays.asList(
                Pair.of("date", date),
                Pair.of("costMs", System.currentTimeMillis() - startTime),
                Pair.of("spaceDetailsCount", Optional.ofNullable(space.get().getSpaceDetails()).map(List::size).orElse(0)),
                Pair.of("tdbComplianceRate", space.get().getTdbComplianceRate())
        ));
        return result;
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENV_QUALITY;
    }

    /**
     * 查询空间数据
     *
     * @param date     日期
     * @param spaceIds 空间ID
     * @return 空间温度
     * @since 7.260201.0
     */
    private EnvQualitySpaceDTO querySpaceComplianceRate(
            @NonNull LocalDate date,
            @NonNull Set<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            return EnvQualitySpaceDTO.EMPTY;
        }
        // 查询空间对象
        List<DtpRwdSpaceDTO> spaces = this.dtpRwdClientService.listSpace(new DtpRwdObjectQueryParam()
                .setId(spaceIds).addWithColumn(DtpRwdObjectDTO.Fields.id,
                        DtpRwdObjectDTO.Fields.localName,
                        DtpRwdSpaceDTO.Fields.floorId));

        // 查询空间分析
        Optional<AirConditionerSpaceAnalysisVO> spaceAnalysisOpt = this.querySpaceAnalysis(date, spaceIds);

        // 查询空间营业时间
        Map<String, List<Range<LocalTime>>> spaceId2WorkTimes = this.queryObjectId2WorkTimes(date, spaceIds);

        // 查询空间服务系统
        Map<String, List<DtpRwdObjectDTO>> spaceId2Systems = this.querySpaceId2Systems(spaceIds);

        // 查询空调季
        Optional<MidCalendarAirSeasonTypeEnum> airSeasonType = this.queryAirSeasonType(date);

        // 查询温度标准配置
        Map<String, DemandConfigAirconditioningTdbQualityValueObject> spaceId2TdbQuality =
                this.queryTdbQualityConfig(date, spaceIds);

        // 查询空间温度孪生控制目标
        Map<String, Map<LocalDateTime, String>> spaceId2DateTime2TempTwinControlAim =
                this.querySpaceTwinControlAim(date, spaceIds);

        // 查询空间TDB
        Map<String, Map<LocalDateTime, String>> spaceId2DateTime2Tdb = this.querySpaceTdb(date, spaceIds);

        // 查询空间服务设备
        List<DtpRwdObjectDTO> serviceEquipments = this.queryServiceEquipments(spaceIds);

        // 查询关联设备数据
        Map<String, Map<String, List<EnvQualitySpaceRelObjectDTO>>> spaceId2ClassCode2Details =
                this.queryEquipmentInfo(date, serviceEquipments);

        // 查询服务设备的信息点定义
        Map<String, List<InfoCodeDefineModel>> serviceEquipmentInfoCodeDefines =
                this.queryServiceEquipmentInfoCodeDefines(spaceId2ClassCode2Details);

        // 查询设备类异常记录信息点定义
        Map<String, List<InfoCodeDefineModel>> exceptionRecordValidInfoCodeDefines =
                this.queryExceptionRecordValidInfoCodeDefines(serviceEquipments);

        // 合并信息点定义
        Map<String, List<InfoCodeDefineModel>> finalClassCode2InfoCodeDefines = this.mergeClassCode2InfoCodeDefines(
                Lists.newArrayList(serviceEquipmentInfoCodeDefines, exceptionRecordValidInfoCodeDefines));

        // 查询设备类定义
        Map<String, ClassCodeDefineModel> classCode2Define = this.queryClassCode2Define(
                finalClassCode2InfoCodeDefines.keySet());

        // 查询设备异常记录
        Map<String, List<DtpRwdObjectDTO>> spaceId2Equipments = DtpRwdObjectDTO.mapRelObjIdAndData(serviceEquipments);
        Map<String, List<ExceptionRecordDTO>> spaceId2ExceptionRecords = new HashMap<>();
        spaceId2Equipments.forEach((spaceId, equipments) -> {
            List<ExceptionRecordDTO> exceptionRecords = this.queryExceptionRecords(
                    date, equipments, exceptionRecordValidInfoCodeDefines);
            if (CollectionUtils.isNotEmpty(exceptionRecords)) {
                spaceId2ExceptionRecords.put(spaceId, exceptionRecords);
            }
        });

        // 构建返回结果
        Map<String, SpacesAnalysis> spaceId2Analysis = spaceAnalysisOpt.map(AirConditionerSpaceAnalysisVO::getSpacesAnalysisList)
                .map(spacesAnalysisList -> spacesAnalysisList.stream()
                        .collect(Collectors.toMap(SpacesAnalysis::getSpaceId, Function.identity()
                        ))).orElseGet(Collections::emptyMap);
        List<EnvQualitySpaceDetailDTO> spaceDetails = spaces.stream()
                .map(space -> {
                    String spaceId = space.getId();
                    String localName = space.getLocalName();
                    String floorId = space.getFloorId();

                    Optional<SpacesAnalysis> analysisOpt = Optional.ofNullable(spaceId2Analysis.get(spaceId));

                    EnvQualitySpaceDetailDTO detail = new EnvQualitySpaceDetailDTO();
                    detail.setId(spaceId);
                    detail.setLocalName(localName);
                    detail.setFloorId(floorId);
                    detail.setTimeTag(analysisOpt.map(SpacesAnalysis::getTimeTag).orElseGet(Collections::emptyList));
                    detail.setBusinessTime(Optional.ofNullable(spaceId2WorkTimes
                            .get(spaceId)).orElseGet(Collections::emptyList));
                    detail.setServiceSystems(Optional.ofNullable(spaceId2Systems
                            .get(spaceId)).orElseGet(Collections::emptyList));
                    detail.setTdbQuality(spaceId2TdbQuality.get(spaceId));
                    detail.setDateTime2TempTwinControlAim(Optional.ofNullable(spaceId2DateTime2TempTwinControlAim
                            .get(spaceId)).orElseGet(Collections::emptyMap));
                    detail.setDateTime2Tdb(Optional.ofNullable(spaceId2DateTime2Tdb
                            .get(spaceId)).orElseGet(Collections::emptyMap));
                    detail.setRelClassCode2Detail(Optional.ofNullable(spaceId2ClassCode2Details
                            .get(spaceId)).orElseGet(Collections::emptyMap));
                    detail.setExceptionRecords(Optional.ofNullable(spaceId2ExceptionRecords
                            .get(spaceId)).orElseGet(Collections::emptyList));
                    return detail;
                }).collect(Collectors.toList());
        EnvQualitySpaceDTO result = new EnvQualitySpaceDTO();
        result.setTdbComplianceRate(spaceAnalysisOpt.map(AirConditionerSpaceAnalysisVO::getSatisfyPercentage).orElse(null));
        result.setAirSeasonType(airSeasonType.orElse(null));
        result.setSpaceDetails(spaceDetails);
        result.setClassCode2Define(classCode2Define);
        result.setClassCode2InfoCodeDefines(finalClassCode2InfoCodeDefines);
        return result;
    }

    /**
     * 查询服务设备的信息点定义
     *
     * @param spaceId2ClassCode2Details 空间ID -> 关联设备类编码 -> 设备类下设备数据
     * @return 设备类编码 -> 信息点定义列表
     * @since 7.260201.0
     */
    private Map<String, List<InfoCodeDefineModel>> queryServiceEquipmentInfoCodeDefines(
            @NonNull Map<String, Map<String, List<EnvQualitySpaceRelObjectDTO>>> spaceId2ClassCode2Details) {
        if (MapUtils.isEmpty(spaceId2ClassCode2Details)) {
            return Collections.emptyMap();
        }
        // 解析出所有设备类编码和信息点
        Map<String, Set<String>> classCode2InfoCodes = spaceId2ClassCode2Details.values().stream()
                .flatMap(classCode2Details -> classCode2Details.values().stream().flatMap(Collection::stream))
                .filter(detail -> MapUtils.isNotEmpty(detail.getInfoCode2DateTime2Value()))
                .collect(Collectors.groupingBy(object -> object.getObject().getClassCode(),
                        Collectors.collectingAndThen(Collectors.mapping(object ->
                                object.getInfoCode2DateTime2Value().keySet(), Collectors.toSet()), list ->
                                list.stream().flatMap(Set::stream).collect(Collectors.toSet())
                        )
                ));
        if (MapUtils.isEmpty(classCode2InfoCodes)) {
            return Collections.emptyMap();
        }
        return this.queryInfoCodeDefines(classCode2InfoCodes);
    }

    /**
     * 查询空间服务系统
     *
     * @param spaceIds 空间ID集合
     * @return 空间ID与服务系统列表的映射
     * @since 7.260201.0
     */
    private Map<String, List<DtpRwdObjectDTO>> querySpaceId2Systems(@NonNull Set<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            logProjectInstance().warn("无空间ID，无法查询空间服务系统");
            return Collections.emptyMap();
        }
        DtpRwdObjectQueryParam systemQueryParam = new DtpRwdObjectQueryParam();
        systemQueryParam.setClassCodeEnum(DtpRwdClassCodeEnum.ACAC_SYSTEM);
        systemQueryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE).setObjTo(spaceIds));
        systemQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                DtpRwdObjectDTO.Fields.localName,
                DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdObjectDTO> systems = dtpRwdClientService.listObject(systemQueryParam);
        logProjectInstance().info("查询-空间服务系统", Arrays.asList(
                Pair.of("spaceIds", spaceIds),
                Pair.of("systemsCount", systems.size()),
                Pair.of("mappedSpaces", DtpRwdObjectDTO.mapRelObjIdAndData(systems).size())
        ));
        return DtpRwdObjectDTO.mapRelObjIdAndData(systems);
    }

    /**
     * 查询空调季类型
     *
     * @param date 日期
     * @return 空调季类型
     * @since 7.260201.0
     */
    private Optional<MidCalendarAirSeasonTypeEnum> queryAirSeasonType(@NonNull LocalDate date) {
        MidCalendarAirSeasonTypeEnum airSeasonType = this.midCalendarClientService.getAirSeasonType(ContextUtil.getProjectId(), date);
        logProjectInstance().info("查询-空调季类型", Arrays.asList(
                Pair.of("date", date),
                Pair.of("airSeasonType", airSeasonType)
        ));
        return Optional.ofNullable(airSeasonType);
    }

    /**
     * 查询温度标准配置
     *
     * @param date     日期
     * @param spaceIds 空间ID集合
     * @return 空间ID -> 温度标准配置
     * @since 7.260201.0
     */
    public Map<String, DemandConfigAirconditioningTdbQualityValueObject> queryTdbQualityConfig(
            @NonNull LocalDate date,
            @NonNull Set<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            logProjectInstance().warn("无空间ID，无法查询温度标准配置", Pair.of("date", date));
            return Collections.emptyMap();
        }
        Optional<DemandTemplate> template = this.queryTemplate(date);
        if (!template.isPresent()) {
            logProjectInstance().warn("无需求模版，无法查询温度标准配置", Pair.of("date", date));
            return Collections.emptyMap();
        }
        DemandConfigByInstanceBatchQueryParam batchQueryParam = new DemandConfigByInstanceBatchQueryParam();
        batchQueryParam.setDate(date);
        batchQueryParam.setTemplateId(template.get().getId());
        batchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(
                DemandInstanceTypeEnum.SPACE, spaceIds));
        List<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> configs =
                this.demandConfigGateway.listConfigsByInstances(batchQueryParam);
        if (CollectionUtils.isEmpty(configs)) {
            logProjectInstance().info("查询-温度标准配置为空", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("spaceIds", spaceIds)
            ));
            return Collections.emptyMap();
        }
        Map<String, DemandConfigAirconditioningTdbQualityValueObject> result = new HashMap<>();
        for (DemandConfigAggregate<DemandConfigAirconditioningValueObject> config : configs) {
            DemandConfigIdentifier identifier = config.getIdentifier();
            String instanceId = identifier.getInstance().getInstanceId();
            Optional.ofNullable(config.getFinalConfig())
                    .map(DemandConfigValueObject::getGlobalConfig)
                    .map(DemandConfigAirconditioningValueObject::getEnvironmentQuality)
                    .map(DemandConfigAirconditioningEnvironmentQualityObject::getTdbQuality)
                    .ifPresent(tdbQuality -> result.put(instanceId, tdbQuality));
        }
        logProjectInstance().info("查询-温度标准配置", Arrays.asList(
                Pair.of("date", date),
                Pair.of("spaceIds", spaceIds),
                Pair.of("resultCount", result.size())
        ));
        return result;
    }

    /**
     * 查询空间孪生控制目标
     *
     * @param date     日期
     * @param spaceIds 空间ID集合
     * @return 孪生控制目标映射
     * @since 7.260201.0
     */
    private Map<String, Map<LocalDateTime, String>> querySpaceTwinControlAim(
            @NonNull LocalDate date,
            @NonNull Set<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            logProjectInstance().warn("无空间ID，无法查询空间孪生控制目标");
            return Collections.emptyMap();
        }
        List<InfoCodeValueVO> twinControlAims = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES, spaceIds,
                Sets.newHashSet(InfoCodeEnum.TEMP_TWIN_CONTROL_AIM.getCode()),
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        Map<String, Map<LocalDateTime, String>> result = twinControlAims.stream()
                .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId, Collectors.toMap(
                        InfoCodeValueVO::getDataTime, InfoCodeValueVO::toDataValueString, (o1, o2) -> o2)));
        logProjectInstance().info("查询-空间孪生控制目标", Arrays.asList(
                Pair.of("date", date),
                Pair.of("spaceIds", spaceIds),
                Pair.of("twinControlAims", twinControlAims)
        ));
        return result;
    }

    /**
     * 查询空间TDB
     *
     * @param date     日期
     * @param spaceIds 空间ID集合
     * @return TDB映射
     * @since 7.260201.0
     */
    public Map<String, Map<LocalDateTime, String>> querySpaceTdb(
            @NonNull LocalDate date,
            @NonNull Set<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            logProjectInstance().warn("无空间ID，无法查询空间TDB");
            return Collections.emptyMap();
        }
        List<InfoCodeValueVO> spaceTdbValues = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES, spaceIds,
                Sets.newHashSet(InfoCodeEnum.SPACE_TDB.getCode()),
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        Map<String, Map<LocalDateTime, String>> result = spaceTdbValues.stream()
                .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId, Collectors.toMap(
                        InfoCodeValueVO::getDataTime, InfoCodeValueVO::toDataValueString, (o1, o2) -> o2)));
        logProjectInstance().info("查询-空间TDB", Arrays.asList(
                Pair.of("date", date),
                Pair.of("spaceIds", spaceIds),
                Pair.of("spaceTdbValues", spaceTdbValues)
        ));
        return result;
    }

    private List<DtpRwdObjectDTO> queryServiceEquipments(@NonNull Set<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            logProjectInstance().warn("无空间ID，无法查询空间服务设备");
            return Collections.emptyList();
        }
        // 查询空间服务设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCode(classCode2InfoCodes.keySet());
        queryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjTo(spaceIds));
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                DtpRwdObjectDTO.Fields.localName,
                DtpRwdObjectDTO.Fields.classCode,
                DtpRwdObjectDTO.Fields.relObjIds
        );
        return dtpRwdClientService.listObject(queryParam);
    }

    /**
     * 处理服务设备相关逻辑
     *
     * @param date              日期
     * @param serviceEquipments 空间服务设备
     * @return 空间ID -> 设备类编码 -> 设备详情
     * @since 7.260201.0
     */
    private Map<String, Map<String, List<EnvQualitySpaceRelObjectDTO>>> queryEquipmentInfo(
            @NonNull LocalDate date,
            @NonNull List<DtpRwdObjectDTO> serviceEquipments) {
        if (CollectionUtils.isEmpty(serviceEquipments)) {
            logProjectInstance().warn("无空间服务设备，无法查询服务设备信息");
            return Collections.emptyMap();
        }
        Map<String, Set<String>> spaceId2EquipmentIds = DtpRwdObjectDTO.mapRelObjIdAndIds(serviceEquipments);
        Map<String, DtpRwdObjectDTO> equipmentId2Data = DtpRwdObjectDTO.mapIdAndData(serviceEquipments);
        Map<String, List<String>> classCode2EquipmentIds = serviceEquipments.stream().collect(Collectors.groupingBy(
                DtpRwdObjectDTO::getClassCode, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toList())));

        // 查询服务设备IOT值
        LocalDateTime today = date.atStartOfDay();
        LocalDateTime tomorrow = date.plusDays(1).atStartOfDay();
        Map<String, List<InfoCodeValueVO>> equipmentId2IotValues = new HashMap<>();
        classCode2EquipmentIds.forEach((classCode, equipmentIds) -> {
            // 查询每个设备类的IOT值
            List<String> infoCodes = classCode2InfoCodes.get(classCode);
            List<InfoCodeValueVO> infoCodeValues = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                    DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES, equipmentIds, infoCodes, today, tomorrow);
            if (CollectionUtils.isEmpty(infoCodeValues)) {
                logProjectInstance().warn("设备类无IOT数据", Arrays.asList(
                        Pair.of("equipmentIds", equipmentIds),
                        Pair.of("classCode", classCode),
                        Pair.of("infoCodes", infoCodes)
                ));
                return;
            }
            equipmentId2IotValues.putAll(infoCodeValues.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId)));
            logProjectInstance().info("查询-设备类IOT值", Arrays.asList(
                    Pair.of("equipmentIds", equipmentIds),
                    Pair.of("classCode", classCode),
                    Pair.of("infoCodes", infoCodes),
                    Pair.of("infoCodeValues", infoCodeValues)
            ));
        });

        // 构建结果
        Map<String, Map<String, List<EnvQualitySpaceRelObjectDTO>>> spaceId2RelClassCode2Detail = new HashMap<>();
        spaceId2EquipmentIds.forEach((spaceId, equipmentIds) -> {
            Map<String, List<EnvQualitySpaceRelObjectDTO>> classCode2Detail = new HashMap<>();
            for (String equipmentId : equipmentIds) {
                DtpRwdObjectDTO equipment = equipmentId2Data.get(equipmentId);
                Map<String, Map<LocalDateTime, String>> infoCode2DateTime2Value = Optional.ofNullable(
                                equipmentId2IotValues.get(equipmentId))
                        .map(values -> values.stream().collect(Collectors.groupingBy(
                                InfoCodeValueVO::getInfoCode, Collectors.toMap(InfoCodeValueVO::getDataTime,
                                        InfoCodeValueVO::toDataValueString, (v1, v2) -> v2
                                ))))
                        .orElse(Collections.emptyMap());

                EnvQualitySpaceRelObjectDTO dto = new EnvQualitySpaceRelObjectDTO();
                dto.setObject(equipment);
                dto.setInfoCode2DateTime2Value(infoCode2DateTime2Value);
                MapExecutor.dynamicPut(classCode2Detail, equipment.getClassCode(), dto, ArrayList::new);
            }
            spaceId2RelClassCode2Detail.put(spaceId, classCode2Detail);
        });
        return spaceId2RelClassCode2Detail;
    }

    /**
     * 查询空间分析
     *
     * @param date     日期
     * @param spaceIds 空间ID
     * @return 空间分析
     * @since 7.260201.0
     */
    private Optional<AirConditionerSpaceAnalysisVO> querySpaceAnalysis(
            @NonNull LocalDate date,
            @NonNull Set<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            logProjectInstance().warn("无空间ID，无法查询空间分析");
            return Optional.empty();
        }
        BasicsAnalysisParam analysisParam = new BasicsAnalysisParam();
        analysisParam.setType(RunningMonitorSecondTypeEnum.RUN_QUALIFY_TDB);
        analysisParam.setInstanceIds(new ArrayList<>(spaceIds));
        analysisParam.setInstanceType(InstanceTypeEnum.SPACE);
        analysisParam.setDate(date);
        AirConditionerSpaceAnalysisVO spaceAnalysis = runAnalysisService.getSpaceAnalysisList(analysisParam);
        logProjectInstance().info("查询-空间分析", Arrays.asList(
                Pair.of("date", date),
                Pair.of("analysisParam", analysisParam),
                Pair.of("spaceAnalysis", spaceAnalysis)
        ));
        return Optional.ofNullable(spaceAnalysis);
    }

    @Override
    public String logTag() {
        return "运行分析报告-环境品质";
    }
}
