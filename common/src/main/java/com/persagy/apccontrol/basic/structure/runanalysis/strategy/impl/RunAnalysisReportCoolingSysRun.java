package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.controller.shellconfig.param.QueryShellConfigParam;
import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigSceneEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosAllControllerParameterQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosScheduleInfoCodeDefineQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpMeosServerCloudClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.shellconfig.ShellConfigService;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerSize;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerTypeSub;
import com.persagy.apccontrol.basic.structure.control.enumeration.ConsoleTypeEnum;
import com.persagy.apccontrol.basic.structure.control.model.SimpleInfoCodeDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.ExceptionRecordDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.WeatherDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingsysrun.*;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.DateTimeTypeEnum;
import com.persagy.validate.utility.enumeration.DateTypeEnum;
import com.persagy.validate.utility.executor.DateTimeRangeExecutor;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 冷站系统运行策略
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportCoolingSysRun extends AbstractRunAnalysisReportStrategy<CoolingSysRunDTO> {

    private final DtpRwdClientService dtpRwdClientService;
    private final ShellConfigService shellConfigService;
    private final DtpMeosServerCloudClientService dtpMeosServerCloudClientService;
    private final AirConditioningControlZones airConditioningControlZones;

    /**
     * 效率编码和壳配置编码对应
     */
    private final List<Pair<String, ShellConfigItemEnum>> efficiencyCodes = Lists.newArrayList(
            Pair.of("sysCOP", ShellConfigItemEnum.ALG_AIR_COOLING_EFF_SYSC),
            Pair.of("chillersCOP", ShellConfigItemEnum.ALG_AIR_COOLING_EFF_CHIC),
            Pair.of("wTFch", ShellConfigItemEnum.ALG_AIR_COOLING_EFF_WTFCH),
            Pair.of("wTFc", ShellConfigItemEnum.ALG_AIR_COOLING_EFF_WTFC),
            Pair.of("thermalUnbalanceRate", ShellConfigItemEnum.ALG_AIR_COOLING_EFF_CTHF)
    );

    /**
     * 系统信息点
     */
    private final List<String> systemInfoCodes = Arrays.asList(
            InfoCodeEnum.COOLING_SUPPLY_SWITCH_SET.getCode(),
            InfoCodeEnum.CHILL_WATER_OUT_TEMP_SET.getCode(),
            InfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.getCode(),
            InfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.getCode(),
            InfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode(),
            InfoCodeEnum.DISCHARGE_COOLING_SWITCH_SET.getCode(),
            InfoCodeEnum.FREE_COOLING_SWITCH_SET.getCode(),
            InfoCodeEnum.DISTRICT_COOLING_PLATE_SWITCH_SET.getCode(),
            InfoCodeEnum.COOLING_STORAGE_SWITCH_SET.getCode()
    );

    /**
     * 系统下冷机信息点
     */
    private final List<String> systemChillerInfoCodes = Arrays.asList(
            ChillerTypeSub.BASE.getCode(),
            ChillerTypeSub.ACCCCC.getCode(),
            ChillerTypeSub.ACCCSC.getCode(),
            ChillerTypeSub.BASE_DUAL.getCode(),
            ChillerTypeSub.ACAHC.getCode(),
            ChillerTypeSub.ACCCGH.getCode(),
            ChillerTypeSub.ACCCWH.getCode(),
            ChillerTypeSub.ACCCAC.getCode()
    );

    /**
     * 系统下串联冷机组信息点
     */
    private final String systemChillerInfoCode_ACSCG = ChillerTypeSub.ACSCG.getCode();

    /**
     * 系统实际运行信息点
     */
    private final List<String> systemRealRunningInfoCodes = Arrays.asList(
            InfoCodeEnum.PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP.getCode(),
            InfoCodeEnum.CHILL_WATER_DETA_TEMP.getCode(),
            InfoCodeEnum.CHILL_WATER_DETA_PRESS.getCode(),
            InfoCodeEnum.WORST_TERMINAL_DETA_PRESS.getCode()
    );

    /**
     * 系统实际运行冷机负载率关联设备类
     */
    private final List<String> systemRealRunningLoadRateRelClassCodes = Arrays.asList(
            DtpRwdClassCodeEnum.ACCCCC.getValue(),
            DtpRwdClassCodeEnum.ACCCSC.getValue(),
            DtpRwdClassCodeEnum.ACCCGH.getValue(),
            DtpRwdClassCodeEnum.ACCCWH.getValue(),
            DtpRwdClassCodeEnum.ACCCAC.getValue()
    );

    /**
     * 系统实际运行冷机负载率关联组设备类
     */
    private final List<String> systemRealRunningLoadRateRelGroupClassCodes = Collections.singletonList(
            DtpRwdClassCodeEnum.ACAHC_GROUP.getValue()
    );

    /**
     * 系统实际运行水泵关联设备类
     */
    private final List<String> systemRealRunningPumpRelClassCodes = Collections.singletonList(
            DtpRwdClassCodeEnum.ACCCCP.getValue()
    );

    @Override
    public Boolean isSupport(@NonNull RunAnalysisReportTypeEnum type) {
        return type == RunAnalysisReportTypeEnum.DAILY;
    }

    @Override
    public CoolingSysRunDTO buildWithConfig(
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

        // 查询系统对象
        List<DtpRwdObjectDTO> systems = this.querySystems(allConfigData);
        if (CollectionUtils.isEmpty(systems)) {
            return null;
        }

        // 查询天气
        CompletableFuture<WeatherDTO> weatherFuture = CompletableFuture.supplyAsync(
                () -> this.queryWeather(date), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询效率配置
        CompletableFuture<List<ShellConfigVO>> efficiencyConfigsFuture = CompletableFuture.supplyAsync(
                this::queryEfficiencyConfigs, BusinessThreadPoolExecutor.ttlExecutor());

        // 查询系统数据
        CompletableFuture<List<SystemEnergyDTO>> systemEnergyFuture = CompletableFuture.supplyAsync(
                () -> this.querySystems(date, systems), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询系统下设备
        CompletableFuture<List<DtpRwdObjectDTO>> equipmentsInSystemFuture = CompletableFuture.supplyAsync(
                () -> this.queryEquipmentsInSystem(systems.stream()
                        .map(DtpRwdObjectDTO::getId)
                        .collect(Collectors.toSet())),
                BusinessThreadPoolExecutor.ttlExecutor());

        // 查询设备异常记录有效信息点定义
        CompletableFuture<Map<String, List<InfoCodeDefineModel>>> exceptionRecordValidInfoCodeDefinesFuture =
                equipmentsInSystemFuture.thenApplyAsync(this::queryExceptionRecordValidInfoCodeDefines, BusinessThreadPoolExecutor.ttlExecutor());

        // 查询系统ID -> 系统下异常记录
        CompletableFuture<Map<String, List<ExceptionRecordDTO>>> systemId2ExceptionRecordsFuture =
                equipmentsInSystemFuture.thenCombineAsync(exceptionRecordValidInfoCodeDefinesFuture,
                        (equipInSystem, validInfoCodeDefines) ->
                                this.querySystemId2ExceptionRecords(date, equipInSystem, validInfoCodeDefines),
                        BusinessThreadPoolExecutor.ttlExecutor()
                );
        // 填充系统异常记录
        CompletableFuture<Void> fillSystemExceptionRecordFuture = systemEnergyFuture.thenAcceptBothAsync(
                systemId2ExceptionRecordsFuture, this::fillSystemExceptionRecords);

        // 查询系统相关信息点定义
        CompletableFuture<Map<String, List<InfoCodeDefineModel>>> classCode2InfoCodeDefineFuture =
                systemEnergyFuture.thenCombineAsync(exceptionRecordValidInfoCodeDefinesFuture,
                        (systemEnergies, exceptionRecordValidInfoCodeDefines) -> {
                            Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines = this.queryInfoCodeDefines(systemEnergies);
                            return this.mergeClassCode2InfoCodeDefines(Lists.newArrayList(
                                    classCode2InfoCodeDefines, exceptionRecordValidInfoCodeDefines));
                        });

        // 查询控制台信息点定义
        CompletableFuture<Map<String, List<SimpleInfoCodeDTO>>> scheduleInfoCodeDefinesFuture = CompletableFuture.supplyAsync(
                this::queryScheduleInfoCodeDefines, BusinessThreadPoolExecutor.ttlExecutor());

        CompletableFuture.allOf(weatherFuture, efficiencyConfigsFuture, systemEnergyFuture,
                equipmentsInSystemFuture, exceptionRecordValidInfoCodeDefinesFuture, systemId2ExceptionRecordsFuture,
                fillSystemExceptionRecordFuture, classCode2InfoCodeDefineFuture, scheduleInfoCodeDefinesFuture).get();

        CoolingSysRunDTO result = new CoolingSysRunDTO();
        result.setWeather(weatherFuture.get());
        result.setEfficiencyConfigs(efficiencyConfigsFuture.get());
        result.setSystems(systemEnergyFuture.get());
        result.setClassCode2InfoCodeDefines(classCode2InfoCodeDefineFuture.get());
        result.setScheduleInfoCodeDefines(scheduleInfoCodeDefinesFuture.get());
        logInstance().info("构建-报告完成", Arrays.asList(
                Pair.of("date", date),
                Pair.of("costMs", System.currentTimeMillis() - startTime),
                Pair.of("result", result)
        ));
        return result;
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN;
    }

    /**
     * 填充系统异常记录
     *
     * @param systemEnergies            系统数据
     * @param systemId2ExceptionRecords 系统ID -> 系统下异常记录
     * @since 7.260201.0
     */
    private void fillSystemExceptionRecords(
            @NonNull List<SystemEnergyDTO> systemEnergies,
            @NonNull Map<String, List<ExceptionRecordDTO>> systemId2ExceptionRecords) {
        if (CollectionUtils.isEmpty(systemEnergies) || MapUtils.isEmpty(systemId2ExceptionRecords)) {
            return;
        }
        for (SystemEnergyDTO systemEnergy : systemEnergies) {
            String systemId = systemEnergy.getSystem().getId();
            List<ExceptionRecordDTO> exceptionRecords = systemId2ExceptionRecords.get(systemId);
            if (CollectionUtils.isEmpty(exceptionRecords)) {
                return;
            }
            systemEnergy.setExceptionRecords(exceptionRecords);
        }
    }

    /**
     * 查询系统ID -> 系统下异常记录
     *
     * @param date                                日期
     * @param equipmentIdsInSystem                设备ID -> 所在系统ID
     * @param exceptionRecordValidInfoCodeDefines 设备异常记录有效信息点定义
     * @return 系统ID -> 系统下异常记录
     * @since 7.260201.0
     */
    private @NonNull Map<String, List<ExceptionRecordDTO>> querySystemId2ExceptionRecords(
            @NonNull LocalDate date,
            @NonNull List<DtpRwdObjectDTO> equipmentIdsInSystem,
            @NonNull Map<String, List<InfoCodeDefineModel>> exceptionRecordValidInfoCodeDefines) {
        if (CollectionUtils.isEmpty(equipmentIdsInSystem) || MapUtils.isEmpty(exceptionRecordValidInfoCodeDefines)) {
            return Collections.emptyMap();
        }
        // 查询设备异常记录
        List<ExceptionRecordDTO> exceptionRecords = this.queryExceptionRecords(date,
                equipmentIdsInSystem, exceptionRecordValidInfoCodeDefines);
        // 设备ID -> 所在系统ID
        Map<String, Set<String>> equipmentId2SystemIds = DtpRwdObjectDTO.mapIdAndRelObjIds(equipmentIdsInSystem);
        // 系统ID -> 系统下异常记录
        Map<String, List<ExceptionRecordDTO>> systemId2ExceptionRecords = new HashMap<>();
        for (ExceptionRecordDTO exceptionRecord : exceptionRecords) {
            String equipmentId = exceptionRecord.getObject().getId();
            Set<String> inSystemIds = equipmentId2SystemIds.get(equipmentId);
            if (CollectionUtils.isEmpty(inSystemIds)) {
                continue;
            }
            inSystemIds.forEach(inSystemId -> MapExecutor.dynamicPut(systemId2ExceptionRecords,
                    inSystemId, exceptionRecord, ArrayList::new));
        }
        return systemId2ExceptionRecords;
    }

    /**
     * 查询信息点定义
     *
     * @param systems 系统数据
     * @return 信息点定义
     * @since 7.260201.0
     */
    private @NonNull Map<String, List<InfoCodeDefineModel>> queryInfoCodeDefines(
            @NonNull List<SystemEnergyDTO> systems) {
        if (CollectionUtils.isEmpty(systems)) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> classCode2InfoCodes = new HashMap<>();
        for (SystemEnergyDTO system : systems) {
            String classCode = system.getSystem().getClassCode();
            Map<String, List<SystemRealRunningDataDTO>> infoCode2RealRunningValue = system.getInfoCode2RealRunningValue();

            // 添加系统信息点
            Set<String> systemInfoCodes = new HashSet<>();
            Optional.ofNullable(system.getInfoCode2DateTime2AdjustmentValue())
                    .map(Map::keySet)
                    .ifPresent(systemInfoCodes::addAll);
            Optional.ofNullable(infoCode2RealRunningValue)
                    .map(Map::keySet)
                    .ifPresent(systemInfoCodes::addAll);
            classCode2InfoCodes.put(classCode, systemInfoCodes);

            // 添加系统关联设备信息点
            if (MapUtils.isNotEmpty(infoCode2RealRunningValue)) {
                infoCode2RealRunningValue.forEach((infoCode, realRunningValues) ->
                        realRunningValues.stream()
                                .filter(realRunningValue -> realRunningValue.getRelObject() != null)
                                .forEach(realRunningValue -> MapExecutor.dynamicPut(
                                        classCode2InfoCodes, realRunningValue.getRelObject().getClassCode(), infoCode, HashSet::new)));
            }
        }
        // 添加冷机负载率中静态信息点字段定义
        Stream.concat(systemRealRunningLoadRateRelClassCodes.stream(), systemRealRunningLoadRateRelGroupClassCodes.stream())
                .forEach(classCode -> MapExecutor.dynamicPut(classCode2InfoCodes,
                        classCode, InfoCodeEnum.RATED_COOL.getCode(), HashSet::new));
        // 添加冷冻水泵运行频率中静态信息点字段定义
        systemRealRunningPumpRelClassCodes.forEach(classCode -> MapExecutor.dynamicPut(classCode2InfoCodes,
                classCode, InfoCodeEnum.RATED_COOL.getCode(), HashSet::new));
        return this.queryInfoCodeDefines(classCode2InfoCodes);
    }

    /**
     * 查询系统数据
     *
     * @param date    日期
     * @param systems 系统
     * @return 系统数据
     * @since 7.260201.0
     */
    private List<SystemEnergyDTO> querySystems(@NonNull LocalDate date, @NonNull List<DtpRwdObjectDTO> systems) {
        if (CollectionUtils.isEmpty(systems)) {
            logInstance().warn("无系统对象，无法查询冷站数据");
            return Collections.emptyList();
        }
        Set<String> systemIds = systems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());

        // 查询当日需求模版
        Optional<DemandTemplate> templateOpt = this.queryTemplate(date);

        // 查询相似日
        List<LocalDate> similarWeekDates = this.querySimilarWeekDates(templateOpt, date);

        // 查询相似日的累计电量
        List<LocalDate> queryDates = Stream.concat(similarWeekDates.stream(), Stream.of(date)).collect(Collectors.toList());
        Map<LocalDate, Map<String, @NonNull BigDecimal>> date2SystemId2AccElecConsum =
                this.queryAccElecConsumByDate(queryDates, systemIds);
        // 获取当日累计电量
        Map<String, @NonNull BigDecimal> systemId2AccElecConsum = Optional.ofNullable(
                date2SystemId2AccElecConsum.get(date)).orElse(Collections.emptyMap());
        // 计算系统耗电量环比
        Map<String, @NonNull BigDecimal> systemId2AccElecConsumMom = this.calculateMom(
                date, similarWeekDates, date2SystemId2AccElecConsum);

        // 查询系统ID -> 时间 -> 累计电量
        Map<String, Map<LocalDateTime, BigDecimal>> systemId2AccElecConsumByHour =
                this.querySystemId2AccElecConsumByHour(date, systemIds);

        // 查询系统ID -> 日期 -> 效率
        Map<String, Map<LocalDate, Map<String, BigDecimal>>> systemId2Date2Efficiency =
                this.querySystemId2Date2Efficiency(date, systemIds);

        // 查询系统ID -> 信息点编码 -> 时间 -> 调节记录值
        Map<String, Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>>> systemId2InfoCode2DateTime2AdjustmentValue =
                this.querySystemId2InfoCode2DateTime2ValueInAdjustmentRecord(date, systemIds);

        // 查询系统ID -> 信息点编码 -> 实际运行数据
        Map<String, Map<String, List<SystemRealRunningDataDTO>>> systemId2InfoCode2RealRunningValue =
                this.querySystemId2InfoCode2RealRunningValue(date, systemIds);

        // 查询系统ID -> 控制分区
        Map<String, List<AirConditioningControlZonesDto>> systemId2AirConditioningControlZones = airConditioningControlZones.buildAirConditioningControlZone(systemIds, date);

        List<SystemEnergyDTO> systemEnergies = new ArrayList<>();
        for (DtpRwdObjectDTO system : systems) {
            String systemId = system.getId();

            SystemEnergyDTO systemEnergy = new SystemEnergyDTO();
            systemEnergy.setSystem(system);
            systemEnergy.setDayAccElecConsum(systemId2AccElecConsum.get(systemId));
            systemEnergy.setAccElecConsumMom(systemId2AccElecConsumMom.get(systemId));
            systemEnergy.setDateTime2AccElecConsum(Optional.ofNullable(systemId2AccElecConsumByHour
                    .get(systemId)).orElseGet(Collections::emptyMap));
            systemEnergy.setDate2Efficiency(Optional.ofNullable(systemId2Date2Efficiency
                    .get(systemId)).orElseGet(Collections::emptyMap));
            systemEnergy.setInfoCode2DateTime2AdjustmentValue(Optional.ofNullable(systemId2InfoCode2DateTime2AdjustmentValue
                    .get(systemId)).orElseGet(Collections::emptyMap));
            systemEnergy.setInfoCode2RealRunningValue(Optional.ofNullable(systemId2InfoCode2RealRunningValue
                    .get(systemId)).orElseGet(Collections::emptyMap));
            systemEnergy.setAirConditioningControlZones(Optional.ofNullable(systemId2AirConditioningControlZones
                    .get(systemId)).orElseGet(Collections::emptyList));
            systemEnergies.add(systemEnergy);
        }
        return systemEnergies;
    }

    /**
     * 查询系统调度信息点定义
     *
     * @return 系统调度信息点定义
     * @since 7.250201.0
     */
    private Map<String, List<SimpleInfoCodeDTO>> queryScheduleInfoCodeDefines() {
        DtpMeosScheduleInfoCodeDefineQueryParam param = new DtpMeosScheduleInfoCodeDefineQueryParam();
        param.setClassCode(DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue());
        return this.dtpMeosServerCloudClientService.listScheduleInfoCodeDefine(param);
    }

    /**
     * 查询系统下设备
     *
     * @param systemIds 系统ID
     * @return 系统下的设备
     * @since 7.260201.0
     */
    private @NonNull List<DtpRwdObjectDTO> queryEquipmentsInSystem(@NonNull Set<String> systemIds) {
        if (CollectionUtils.isEmpty(systemIds)) {
            return Collections.emptyList();
        }
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(systemIds));
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName,
                DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.relObjIds);
        return this.dtpRwdClientService.listObject(queryParam);
    }

    /**
     * 查询系统实际运行数据
     *
     * @param date      日期
     * @param systemIds 系统ID
     * @return 系统ID -> 信息点编码 -> 实际运行值
     * @since 7.260201.0
     */
    private @NonNull Map<String, Map<String, List<SystemRealRunningDataDTO>>>
    querySystemId2InfoCode2RealRunningValue(
            @NonNull LocalDate date,
            @NonNull Set<String> systemIds) {
        Map<String, Map<String, List<SystemRealRunningDataDTO>>> systemId2InfoCodes = new HashMap<>();

        // 查询系统ID -> 冷机负载率
        Map<String, List<SystemRealRunningDataDTO>> systemId2LoadRates =
                this.querySystemId2LoadRates(date, systemIds);
        systemId2LoadRates.forEach((systemId, loadRates) ->
                MapExecutor.dynamicPutAll(systemId2InfoCodes, systemId, InfoCodeEnum.I_PLR.getCode(),
                        loadRates, LinkedHashMap::new, ArrayList::new));

        // 查询系统ID -> 运行频率
        Map<String, List<SystemRealRunningDataDTO>> systemId2RunningFrequencies =
                this.querySystemId2RunningFrequency(date, systemIds);
        systemId2RunningFrequencies.forEach((systemId, runningFrequencies) ->
                MapExecutor.dynamicPutAll(systemId2InfoCodes, systemId, InfoCodeEnum.WORK_FREQ.getCode(),
                        runningFrequencies, LinkedHashMap::new, ArrayList::new));

        // 查询系统IOT数据
        List<InfoCodeValueVO> infoCodeValues = this.queryInfoCodeValues(date, systemIds,
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES, this.systemRealRunningInfoCodes);
        if (CollectionUtils.isNotEmpty(infoCodeValues)) {
            infoCodeValues.stream()
                    .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId,
                            Collectors.groupingBy(InfoCodeValueVO::getInfoCode)))
                    .forEach((objectId, infoCode2Values) ->
                            infoCode2Values.forEach((infoCode, values) -> {
                                SystemRealRunningDataDTO dto = new SystemRealRunningDataDTO();
                                dto.setDateTime2Value(values.stream()
                                        .filter(item -> StringUtils.isNotBlank(item.toDataValueString()))
                                        .collect(Collectors.toMap(
                                                InfoCodeValueVO::getDataTime,
                                                InfoCodeValueVO::toDataValueString,
                                                (existing, replacement) -> replacement)));
                                MapExecutor.dynamicPut(systemId2InfoCodes, objectId, infoCode, dto, LinkedHashMap::new, ArrayList::new);
                            }));
        }
        return systemId2InfoCodes;
    }

    /**
     * 查询系统运行频率
     *
     * @param date      日期
     * @param systemIds 系统ID
     * @return 系统ID -> 运行频率
     * @since 7.260201.0
     */
    private @NonNull Map<String, List<SystemRealRunningDataDTO>> querySystemId2RunningFrequency(
            @NonNull LocalDate date,
            @NonNull Set<String> systemIds) {
        if (CollectionUtils.isEmpty(systemIds)) {
            return Collections.emptyMap();
        }

        // 查询系统ID -> 运行频率关联设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCode(this.systemRealRunningPumpRelClassCodes);
        queryParam.addInfoCode(InfoCodeEnum.NET_USE_TYPE.getCode(), "1");
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(systemIds));
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName,
                DtpRwdObjectDTO.Fields.classCode, InfoCodeEnum.RATED_FLOW.getCode(), DtpRwdObjectDTO.Fields.relObjIds);
        Map<String, List<DtpRwdObjectDTO>> systemId2RunningFrequencyRelObjects =
                DtpRwdObjectDTO.mapRelObjIdAndData(this.dtpRwdClientService.listObject(queryParam));
        if (MapUtils.isEmpty(systemId2RunningFrequencyRelObjects)) {
            logProjectInstance().warn("未查询到冷冻水泵运行频率关联设备", Pair.of("systemIds", systemIds));
            return Collections.emptyMap();
        }

        // 查询运行数据
        return this.querySystemRunningData(date, systemId2RunningFrequencyRelObjects,
                SystemRealRunningDataDTO.ObjectType.FROZEN_PUMP, InfoCodeEnum.WORK_FREQ.getCode());
    }

    /**
     * 查询系统冷机负载率
     *
     * @param date      日期
     * @param systemIds 系统ID
     * @return 系统ID -> 冷机负载率
     * @since 7.260201.0
     */
    private @NonNull Map<String, List<SystemRealRunningDataDTO>> querySystemId2LoadRates(
            @NonNull LocalDate date,
            @NonNull Set<String> systemIds) {
        if (CollectionUtils.isEmpty(systemIds)) {
            return Collections.emptyMap();
        }

        // 查询系统ID -> 冷机负载率关联设备
        Map<String, List<DtpRwdObjectDTO>> systemId2LoadRateRelObjects = this.querySystemId2LoadRateRelObjects(systemIds);
        if (MapUtils.isEmpty(systemId2LoadRateRelObjects)) {
            logProjectInstance().warn("未查询到冷机负载率关联设备", Pair.of("systemIds", systemIds));
            return Collections.emptyMap();
        }

        // 查询运行数据
        return this.querySystemRunningData(date, systemId2LoadRateRelObjects,
                SystemRealRunningDataDTO.ObjectType.CHILLER, InfoCodeEnum.I_PLR.getCode());
    }

    /**
     * 通用的系统运行数据查询方法
     *
     * @param date                日期
     * @param systemId2RelObjects 系统ID -> 关联设备
     * @param objectType          对象类型
     * @param infoCode            信息点编码
     * @return 系统ID -> 运行数据
     */
    private @NonNull Map<String, List<SystemRealRunningDataDTO>> querySystemRunningData(
            @NonNull LocalDate date,
            @NonNull Map<String, List<DtpRwdObjectDTO>> systemId2RelObjects,
            SystemRealRunningDataDTO.@NonNull ObjectType objectType,
            @NonNull String infoCode) {
        if (MapUtils.isEmpty(systemId2RelObjects)) {
            return Collections.emptyMap();
        }

        Set<String> relObjectIds = systemId2RelObjects.values().stream()
                .flatMap(List::stream)
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toSet());

        // 查询有运行记录的设备
        Set<String> hasOpenRecordObjectIds = this.queryHasOpenRecordObjectIds(date, relObjectIds);

        // 查询运行数据
        List<InfoCodeValueVO> infoCodeValues = this.queryInfoCodeValues(date, relObjectIds,
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES, Sets.newHashSet(infoCode));

        Map<String, Map<LocalDateTime, String>> objectId2DateTime2Value = infoCodeValues.stream().collect(
                Collectors.groupingBy(InfoCodeValueVO::getObjectId, Collectors.toMap(
                        InfoCodeValueVO::getDataTime, InfoCodeValueVO::toDataValueString)));

        Map<String, List<SystemRealRunningDataDTO>> result = new HashMap<>();
        systemId2RelObjects.forEach((systemId, relObjects) -> {
            List<SystemRealRunningDataDTO> objects = relObjects.stream().map(relObject -> {
                SystemRealRunningDataDTO runningData = new SystemRealRunningDataDTO();
                runningData.setRelObject(relObject);
                runningData.setObjectType(objectType);
                runningData.setDateTime2Value(Optional.ofNullable(objectId2DateTime2Value
                        .get(relObject.getId())).orElseGet(Collections::emptyMap));
                runningData.setHasOpenRecord(hasOpenRecordObjectIds.contains(relObject.getId()));
                return runningData;
            }).collect(Collectors.toList());
            MapExecutor.dynamicPutAll(result, systemId, objects, ArrayList::new);
        });
        return result;
    }

    /**
     * 查询有运行记录的设备
     *
     * @param date      日期
     * @param objectIds 设备ID
     * @return 有运行记录的设备ID
     * @since 7.260201.0
     */
    private @NonNull Set<String> queryHasOpenRecordObjectIds(@NonNull LocalDate date, @NonNull Set<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Collections.emptySet();
        }
        List<InfoCodeValueVO> infoCodeValues = this.queryInfoCodeValues(date, objectIds,
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES, Sets.newHashSet(InfoCodeEnum.RUN_STATUS.getCode()));
        if (CollectionUtils.isEmpty(infoCodeValues)) {
            logInstance().warn("未查询到运行状态，无法标记开启记录", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("objectIds", objectIds)
            ));
            return Collections.emptySet();
        }
        return infoCodeValues.stream()
                // 仅获取存在开启记录的设备
                .filter(infoCodeValue -> infoCodeValue.equalsInfoCodeValue("1"))
                .map(InfoCodeValueVO::getObjectId)
                .collect(Collectors.toSet());
    }

    /**
     * 查询系统冷机负载率关联设备
     *
     * @param systemIds 系统ID
     * @return 系统ID -> 关联设备
     * @since 7.260201.0
     */
    public @NonNull Map<String, List<DtpRwdObjectDTO>> querySystemId2LoadRateRelObjects(@NonNull Set<String> systemIds) {
        if (CollectionUtils.isEmpty(systemIds)) {
            return Collections.emptyMap();
        }
        // 查询系统下的冷机
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName,
                DtpRwdObjectDTO.Fields.classCode, InfoCodeEnum.RATED_COOL.getCode(),
                DtpRwdObjectDTO.Fields.relObjIds);
        queryParam.setClassCode(this.systemRealRunningLoadRateRelClassCodes);
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(systemIds));
        Map<String, List<DtpRwdObjectDTO>> systemId2Chillers = DtpRwdObjectDTO.mapRelObjIdAndData(
                this.dtpRwdClientService.listObject(queryParam));

        // 查询系统下的冷机组
        queryParam.setClassCode(this.systemRealRunningLoadRateRelGroupClassCodes);
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(systemIds));
        Map<String, List<DtpRwdObjectDTO>> systemId2ChillerGroups = DtpRwdObjectDTO.mapRelObjIdAndData(
                this.dtpRwdClientService.listObject(queryParam));

        Map<String, List<DtpRwdObjectDTO>> systemId2Objects = new HashMap<>();
        systemId2Chillers.forEach((systemId, chillers) ->
                MapExecutor.dynamicPutAll(systemId2Objects, systemId, chillers, ArrayList::new));
        systemId2ChillerGroups.forEach((systemId, chillerGroups) ->
                MapExecutor.dynamicPutAll(systemId2Objects, systemId, chillerGroups, ArrayList::new));
        return systemId2Objects;
    }

    public @NonNull Map<String, List<DtpRwdObjectDTO>> querySystemId2LoadRateRelObjects(@NonNull Set<String> systemIds,List<String> classCodes) {
        if (CollectionUtils.isEmpty(systemIds)) {
            return Collections.emptyMap();
        }
        // 查询系统下的冷却塔风机
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName,
                DtpRwdObjectDTO.Fields.classCode, InfoCodeEnum.RATED_COOL.getCode(),
                DtpRwdObjectDTO.Fields.relObjIds);
        queryParam.setClassCode(classCodes);
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(systemIds));
        Map<String, List<DtpRwdObjectDTO>> systemId2Chillers = DtpRwdObjectDTO.mapRelObjIdAndData(
                this.dtpRwdClientService.listObject(queryParam));

        Map<String, List<DtpRwdObjectDTO>> systemId2Objects = new HashMap<>();
        systemId2Chillers.forEach((systemId, chillers) ->
                MapExecutor.dynamicPutAll(systemId2Objects, systemId, chillers, ArrayList::new));
        return systemId2Objects;
    }

    /**
     * 查询系统调节数据
     *
     * @param date      日期
     * @param systemIds 系统ID
     * @return 系统ID -> 信息点编码 -> 时间 -> 值
     * @since 7.260201.0
     */
    private @NonNull Map<String, Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>>>
    querySystemId2InfoCode2DateTime2ValueInAdjustmentRecord(
            @NonNull LocalDate date,
            @NonNull Set<String> systemIds) {
        Map<String, Set<String>> systemId2ValidInfoCodes = this.querySystemId2ValidInfoCodes(date, systemIds);
        Map<String, Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>>> result = new HashMap<>();
        for (String systemId : systemIds) {
            Set<String> infoCodes = Optional.ofNullable(systemId2ValidInfoCodes
                    .get(systemId)).orElseGet(Collections::emptySet);

            // 查询系统信息点数据
            Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> systemInfoCode2DateTime2Value =
                    this.querySystemInfoCode2DateTime2Value(date, systemId, infoCodes.stream()
                            .filter(this.systemInfoCodes::contains)
                            .collect(Collectors.toSet()));

            // 查询系统下冷机台数
            Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> chillerType2DateTime2Value =
                    this.querySystemChillerType2DateTime2Value(date, systemId, infoCodes.stream()
                            .filter(this.systemChillerInfoCodes::contains)
                            .collect(Collectors.toSet()), InfoCodeEnum.EQUIP_SWITCH_SET);

            // 查询系统下串联冷机组台数
            Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> chillerType2DateTime2Value_ACSCG =
                    this.querySystemChillerType2DateTime2Value(date, systemId, infoCodes.stream()
                            .filter(this.systemChillerInfoCode_ACSCG::equals)
                            .collect(Collectors.toSet()), InfoCodeEnum.ENABLE_COMMAND);

            // 合并系统信息点数据和系统冷机台数
            Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> merged = this.mergeChillerType2DateTime2Value(
                    systemInfoCode2DateTime2Value, chillerType2DateTime2Value, chillerType2DateTime2Value_ACSCG);
            if (MapUtils.isNotEmpty(merged)) {
                result.put(systemId, merged);
            }
        }
        return result;
    }

    /**
     * 合并多个系统冷机台数
     *
     * @param maps 多个系统冷机台数
     * @return 合并后的系统冷机台数
     * @since 7.260201.0
     */
    @SafeVarargs
    private final @NonNull Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> mergeChillerType2DateTime2Value(
            @NonNull Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>>... maps) {
        if (ArrayUtils.isEmpty(maps)) {
            return Collections.emptyMap();
        }
        Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> merged = new HashMap<>();
        for (Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> map : maps) {
            if (MapUtils.isEmpty(map)) {
                continue;
            }
            merged.putAll(map);
        }
        return merged;
    }

    /**
     * 查询系统信息点数据
     *
     * @param date      日期
     * @param systemId  系统ID
     * @param infoCodes 信息点代码
     * @return 信息点编码 -> 时间 -> 值
     * @since 7.260201.0
     */
    private Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> querySystemInfoCode2DateTime2Value(
            @NonNull LocalDate date,
            @NonNull String systemId,
            @NonNull Set<String> infoCodes) {
        if (CollectionUtils.isEmpty(infoCodes)) {
            return Collections.emptyMap();
        }
        List<InfoCodeValueVO> infoCodeValues = this.queryInfoCodeValues(date, Sets.newHashSet(systemId),
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES, infoCodes);
        if (CollectionUtils.isEmpty(infoCodeValues)) {
            logProjectInstance().warn("系统无信息点数据", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("systemId", systemId),
                    Pair.of("infoCodes", infoCodes)
            ));
            return Collections.emptyMap();
        }
        return infoCodeValues.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getInfoCode,
                Collectors.toMap(InfoCodeValueVO::getDataTime, infoCodeValue -> {
                            SystemAdjustmentRecordDataDTO dto = new SystemAdjustmentRecordDataDTO();
                            dto.setDataType(SystemAdjustmentRecordDataDTO.DataType.IOT);
                            dto.setInfoCode(infoCodeValue.getInfoCode());
                            dto.setInfoCodeValue(Optional.ofNullable(infoCodeValue.toDataValueString())
                                    .map(BigDecimal::new).orElse(null));
                            return dto;
                        }, (v1, v2) -> v2
                )
        ));
    }

    /**
     * 查询系统冷机台数
     *
     * @param date      日期
     * @param systemId  系统ID
     * @param infoCodes 信息点编码
     * @return 冷机类型 -> 时间 -> 冷机台数
     * @since 7.260201.0
     */
    private Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> querySystemChillerType2DateTime2Value(
            @NonNull LocalDate date,
            @NonNull String systemId,
            @NonNull Set<String> infoCodes,
            @NonNull InfoCodeEnum queryInfoCode) {
        if (CollectionUtils.isEmpty(infoCodes)) {
            logProjectInstance().warn("无信息点编码，无法查询冷机台数");
            return Collections.emptyMap();
        }
        // 查询冷机台数分组
        Map<ChillerTypeSub, Map<ChillerSize, List<String>>> chillerType2Size2Ids =
                this.dtpRwdClientService.queryChillerType2Size2Ids(systemId);
        if (MapUtils.isEmpty(chillerType2Size2Ids)) {
            logProjectInstance().warn("无冷机分组，无法查询冷机台数");
            return Collections.emptyMap();
        }
        // 根据入参的infoCodes过滤冷机类型
        chillerType2Size2Ids.entrySet().removeIf(entry -> !infoCodes.contains(entry.getKey().getCode()));
        if (MapUtils.isEmpty(chillerType2Size2Ids)) {
            logProjectInstance().warn("过滤后无冷机分组，无法查询冷机台数");
            return Collections.emptyMap();
        }
        // 查询所有冷机的设备开关设定值
        Set<String> allChillerIds = chillerType2Size2Ids.values().stream()
                .flatMap(size2Ids -> size2Ids.values().stream().flatMap(Collection::stream))
                .collect(Collectors.toSet());
        List<InfoCodeValueVO> infoCodeValues = this.queryInfoCodeValues(date, allChillerIds,
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES, Sets.newHashSet(queryInfoCode.getCode()));
        if (CollectionUtils.isEmpty(infoCodeValues)) {
            logProjectInstance().warn("冷机无设备开关设定值，无法计算冷机台数");
        }
        Map<String, Map<LocalDateTime, List<InfoCodeValueVO>>> chillerId2DateTime2Values = infoCodeValues.stream()
                // 仅获取状态为开的IOT数据
                .filter(infoCodeValue -> StringUtils.equals("1", infoCodeValue.toDataValueString()))
                .collect(Collectors.groupingBy(
                        InfoCodeValueVO::getObjectId,
                        Collectors.groupingBy(InfoCodeValueVO::getDataTime)
                ));

        // 根据冷机台数分组进行返回，使用DateTimeRangeExecutor生成5分钟精度的所有时间点
        Map<String, Map<LocalDateTime, SystemAdjustmentRecordDataDTO>> result = new HashMap<>();
        LocalDateTime today = date.atStartOfDay();
        LocalDateTime tomorrow = date.plusDays(1).atStartOfDay();
        for (Map.Entry<ChillerTypeSub, Map<ChillerSize, List<String>>> typeEntry : chillerType2Size2Ids.entrySet()) {
            ChillerTypeSub chillerTypeSub = typeEntry.getKey();
            String typeCode = chillerTypeSub.getCode();
            Map<ChillerSize, List<String>> size2Ids = typeEntry.getValue();

            Map<LocalDateTime, SystemAdjustmentRecordDataDTO> dateTime2Value = new LinkedHashMap<>();
            DateTimeRangeExecutor.generateDateTime(DateTimeTypeEnum.MINUTE, 5, today, tomorrow, dateTime -> {
                SystemAdjustmentRecordDataDTO record = new SystemAdjustmentRecordDataDTO();
                record.setDataType(SystemAdjustmentRecordDataDTO.DataType.CHILLER);

                Integer chillerLargeNumber = null;
                Integer chillerMediumNumber = null;
                Integer chillerSmallNumber = null;

                for (Map.Entry<ChillerSize, List<String>> sizeEntry : size2Ids.entrySet()) {
                    ChillerSize size = sizeEntry.getKey();
                    List<String> chillerIds = sizeEntry.getValue();

                    for (String chillerId : chillerIds) {
                        if (size == ChillerSize.LARGE) {
                            chillerLargeNumber = Optional.ofNullable(chillerLargeNumber).orElse(0);
                        } else if (size == ChillerSize.M1 || size == ChillerSize.M2 || size == ChillerSize.M3) {
                            chillerMediumNumber = Optional.ofNullable(chillerMediumNumber).orElse(0);
                        } else if (size == ChillerSize.SMALL) {
                            chillerSmallNumber = Optional.ofNullable(chillerSmallNumber).orElse(0);
                        }
                        Map<LocalDateTime, List<InfoCodeValueVO>> dateTime2Values = chillerId2DateTime2Values.get(chillerId);
                        if (MapUtils.isNotEmpty(dateTime2Values) && dateTime2Values.containsKey(dateTime)) {
                            if (size == ChillerSize.LARGE) {
                                chillerLargeNumber++;
                            } else if (size == ChillerSize.M1 || size == ChillerSize.M2 || size == ChillerSize.M3) {
                                chillerMediumNumber++;
                            } else if (size == ChillerSize.SMALL) {
                                chillerSmallNumber++;
                            }
                        }
                    }
                }

                record.setChillerLargeNumber(chillerLargeNumber);
                record.setChillerMediumNumber(chillerMediumNumber);
                record.setChillerSmallNumber(chillerSmallNumber);
                dateTime2Value.put(dateTime, record);
            });
            result.put(typeCode, dateTime2Value);
        }
        return result;
    }

    /**
     * 查询空调全量信息点和名称映射
     *
     * @param date      日期
     * @param systemIds 系统 ID 集合
     * @return 系统ID -> 有效信息点编码
     * @since 7.260201.0
     */
    private @NonNull Map<String, Set<String>> querySystemId2ValidInfoCodes(@NonNull LocalDate date, @NonNull Set<String> systemIds) {
        Map<String, Set<String>> systemId2ValidInfoCodes = new ConcurrentHashMap<>();
        Function<String, CompletableFuture<Void>> function = systemId -> CompletableFuture.runAsync(() -> {
            List<ConsoleTypeEnum> types = Lists.newArrayList(
                    ConsoleTypeEnum.COOL_INPUT, ConsoleTypeEnum.COOL_SUPPLY);
            for (ConsoleTypeEnum type : types) {
                DtpMeosAllControllerParameterQueryParam queryParam = new DtpMeosAllControllerParameterQueryParam();
                queryParam.setSystemId(systemId);
                queryParam.setSystemType(DtpRwdClassCodeEnum.ACAC_SYSTEM);
                queryParam.setConsoleTypes(type);
                queryParam.setBeginDate(date);
                queryParam.setEndDate(date.plusDays(1));
                Set<String> infoCodes = this.dtpMeosServerCloudClientService.getAllControllerParameter(queryParam).stream()
                        .flatMap(allControllerParameter -> {
                            String infoCode = allControllerParameter.getInfoCode();
                            String actualInfoCode = allControllerParameter.getActualInfoCode();
                            String setInfoCode = allControllerParameter.getSetInfoCode();
                            return Stream.of(infoCode, actualInfoCode, setInfoCode)
                                    .filter(StringUtils::isNotBlank);
                        }).collect(Collectors.toSet());
                if (CollectionUtils.isEmpty(infoCodes)) {
                    logProjectInstance().warn("系统无有效信息点", Arrays.asList(
                            Pair.of("date", date),
                            Pair.of("systemId", systemId),
                            Pair.of("type", type)
                    ));
                    continue;
                }
                infoCodes.forEach(infoCode -> MapExecutor.dynamicPut(systemId2ValidInfoCodes,
                        systemId, infoCode, HashSet::new));
            }
        }, BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture.allOf(systemIds.stream().map(function).toArray(CompletableFuture[]::new)).join();
        return systemId2ValidInfoCodes;
    }

    /**
     * 查询效率配置
     *
     * @return 效率配置
     * @since 7.260201.0
     */
    private List<ShellConfigVO> queryEfficiencyConfigs() {
        QueryShellConfigParam queryShellConfigParam = new QueryShellConfigParam();
        queryShellConfigParam.setScene(ShellConfigSceneEnum.SCENE_EFF.getCode());
        queryShellConfigParam.setProjectId(ContextUtil.getProjectId());
        List<ShellConfigVO> configs = this.shellConfigService.query(ContextUtil.getGroupCode(), queryShellConfigParam);
        if (CollectionUtils.isEmpty(configs)) {
            logProjectInstance().warn("无效率配置");
            return Collections.emptyList();
        }
        return configs;
    }

    /**
     * 查询系统效率
     *
     * @param date      日期
     * @param systemIds 系统 ID 集合
     * @return 系统 ID -> 日期 -> 效率
     * @since 7.260201.0
     */
    private Map<String, Map<LocalDate, Map<String, BigDecimal>>> querySystemId2Date2Efficiency(
            @NonNull LocalDate date, @NonNull Set<String> systemIds) {
        if (CollectionUtils.isEmpty(systemIds)) {
            return Collections.emptyMap();
        }
        LocalDate beginDate = date.minusWeeks(1).plusDays(1);
        Map<String, Map<LocalDate, Map<String, BigDecimal>>> systemId2Date2Efficiency = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                        DtpRwdPersistenceTimeTypeEnum.DAY, systemIds,
                        this.efficiencyCodes.stream().map(Pair::getLeft).collect(Collectors.toList()),
                        beginDate.atStartOfDay(), date.plusDays(1).atStartOfDay()).stream()
                .filter(infoCodeValue -> StringUtils.isNotBlank(infoCodeValue.toDataValueString()))
                .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId, Collectors.groupingBy(infoCodeValue ->
                        infoCodeValue.getDataTime().toLocalDate(), Collectors.toMap(
                        InfoCodeValueVO::getInfoCode, infoCodeValue -> new BigDecimal(
                                infoCodeValue.toDataValueString()), (v1, v2) -> v2))));
        return systemIds.stream().collect(Collectors.toMap(systemId -> systemId, systemId ->
                DateTimeRangeExecutor.generateDate(DateTypeEnum.DAY, beginDate, date, true, eachDate -> {
                    return Optional.ofNullable(MapExecutor.dynamicGet(systemId2Date2Efficiency,
                            systemId, eachDate)).orElseGet(Collections::emptyMap);
                })));
    }

    /**
     * 查询系统累计用电量
     *
     * @param date      日期
     * @param systemIds 系统 ID 集合
     * @return 系统 ID -> 日用电量
     * @since 7.260201.0
     */
    private Map<String, Map<LocalDateTime, BigDecimal>> querySystemId2AccElecConsumByHour(
            @NonNull LocalDate date,
            @NonNull Set<String> systemIds) {
        if (CollectionUtils.isEmpty(systemIds)) {
            return Collections.emptyMap();
        }
        List<InfoCodeValueVO> infoCodeValues = this.queryInfoCodeValues(date, systemIds,
                DtpRwdPersistenceTimeTypeEnum.HOUR, Sets.newHashSet(InfoCodeEnum.ACC_ELEC_CONSUM.getCode()));
        return infoCodeValues.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId, Collectors.groupingBy(
                InfoCodeValueVO::getDataTime, Collectors.reducing(BigDecimal.ZERO, infoCodeValue ->
                        new BigDecimal(infoCodeValue.toDataValueString()), BigDecimal::add))));
    }

    /**
     * 查询系统
     *
     * @param allConfigData 所有配置数据
     * @return 系统
     * @since 7.260201.0
     */
    private List<DtpRwdObjectDTO> querySystems(
            @NonNull Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData) {
        Set<String> systemIds = Optional.ofNullable(allConfigData.get(
                        RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN_ITEM))
                .map(configs -> configs.stream()
                        .map(RunAnalysisReportConfig::getObjectId)
                        .filter(StringUtils::isNotBlank))
                .orElseGet(Stream::empty)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(systemIds)) {
            logProjectInstance().warn("无冷站系统运行配置，无法查询系统数据");
            return Collections.emptyList();
        }
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(systemIds);
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.ACAC_SYSTEM);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id,
                DtpRwdObjectDTO.Fields.localName, DtpRwdObjectDTO.Fields.classCode);
        return dtpRwdClientService.listObject(queryParam);
    }

    @Override
    public String logTag() {
        return "运行分析报告-冷站系统运行";
    }
}
