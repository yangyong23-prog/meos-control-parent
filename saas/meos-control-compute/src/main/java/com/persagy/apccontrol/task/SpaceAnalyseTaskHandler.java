package com.persagy.apccontrol.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFloorDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.AnalysisAirConditionerSpace;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalCarbonDioxideIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalTemperatureIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.CalendarDayPlanByObjIdParam;
import com.persagy.apccontrol.basic.model.po.common.TimeTagPO;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel1;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.service.airterminal.AnalysisAirConditionerSpaceService;
import com.persagy.apccontrol.basic.service.systemgraph.impl.RiskEventService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.service.demand.DemandOpeningCalendarService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @description:空间分析
 * @author: wxf
 * @create: 2024-11-29 18:01
 **/
@Component("spaceAnalyseTaskHandler")
@Slf4j
public class SpaceAnalyseTaskHandler implements TecXxJobHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceAnalyseTaskHandler.class);
    public static final String LOG_TAG = LocaleMessageUtil.getMessage("key_01386d8a67fc41ccb9e639cf73d363c1");

    private final DtpRwdClientService dtpRwdClientService;
    private final DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;
    private final MidCalendarClientService midCalendarClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final DemandOpeningCalendarService demandOpeningCalendarService;
    private final RiskEventService riskEventService;
    private final AnalysisAirConditionerSpaceService analysisAirConditionerSpaceService;

    //分段连续点位数
    private static final int CONTINUE_NUM = 3;

    public SpaceAnalyseTaskHandler(DtpRwdClientService dtpRwdClientService,
                                   DemandConfigGateway demandConfigGateway,
                                   MidCalendarClientService midCalendarClientService,
                                   AbstractIotDataCacheServer abstractIotDataCacheServer, DemandOpeningCalendarService demandOpeningCalendarService, RiskEventService riskEventService, AnalysisAirConditionerSpaceService analysisAirConditionerSpaceService) {
        this.dtpRwdClientService = dtpRwdClientService;
        this.demandConfigGateway = demandConfigGateway;
        this.midCalendarClientService = midCalendarClientService;
        this.abstractIotDataCacheServer = abstractIotDataCacheServer;
        this.demandOpeningCalendarService = demandOpeningCalendarService;
        this.riskEventService = riskEventService;
        this.analysisAirConditionerSpaceService = analysisAirConditionerSpaceService;
    }

    /**
     * 空间温度分析
     */
    private BiFunction<ValueObject, BigDecimal, BaseEnum<String>> tdbAnalysisFunction = (valueObject, ipValue) -> {
        DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality = (DemandConfigAirconditioningTdbSeasonValueObject) valueObject;
        //舒适区下限值
        BigDecimal lowerComfortSeasonLimitValue = tdbSeasonQuality.getLowerComfortSeasonLimitValue();
        //过热门限
        BigDecimal overHeatingSeasonLimitValue = tdbSeasonQuality.getOverHeatingSeasonLimitValue();
        //过冷门限
        BigDecimal underCoolingSeasonLimitValue = tdbSeasonQuality.getUnderCoolingSeasonLimitValue();
        //舒适区上限
        BigDecimal upperComfortSeasonLimitValue = tdbSeasonQuality.getUpperComfortSeasonLimitValue();

        AirTerminalTemperatureIntervalEnum stat;
        if (ipValue.compareTo(overHeatingSeasonLimitValue) >= 0) {
            return AirTerminalTemperatureIntervalEnum.TOO_HOT;
        } else if (ipValue.compareTo(underCoolingSeasonLimitValue) < 0) {
            return AirTerminalTemperatureIntervalEnum.TOO_COLD;
        } else if (ipValue.compareTo(underCoolingSeasonLimitValue) >= 0 && ipValue.compareTo(lowerComfortSeasonLimitValue) < 0) {
            return AirTerminalTemperatureIntervalEnum.COLD;
        } else if (ipValue.compareTo(upperComfortSeasonLimitValue) >= 0 && ipValue.compareTo(overHeatingSeasonLimitValue) < 0) {
            return AirTerminalTemperatureIntervalEnum.HOT;
        } else {
            return AirTerminalTemperatureIntervalEnum.APPROPRIATE;
        }
    };

    /**
     * 空间co2分析
     */
    private BiFunction<ValueObject, BigDecimal, BaseEnum<String>> co2AnalysisFunction = (valueObject, ipValue) -> {
        DemandConfigAirconditioningCO2QualityValueObject co2Quality = (DemandConfigAirconditioningCO2QualityValueObject) valueObject;
        //舒适区上限
        BigDecimal carbonDioxideLimitValue = co2Quality.getCarbonDioxideLimitValue();
        //过高上限
        BigDecimal carbonDioxideUpperLimitValue = co2Quality.getCarbonDioxideUpperLimitValue();

        if (ipValue.compareTo(carbonDioxideUpperLimitValue) > 0) {
            return AirTerminalCarbonDioxideIntervalEnum.TOO_HIGH;
        } else if (ipValue.compareTo(carbonDioxideLimitValue) > 0) {
            return AirTerminalCarbonDioxideIntervalEnum.HIGH;
        } else {
            return AirTerminalCarbonDioxideIntervalEnum.APPROPRIATE;
        }
    };

    /**
     * 温度配置检查
     */
    private boolean hasTdbConfigured(@NonNull DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality) {
        return Objects.nonNull(tdbSeasonQuality.getLowerComfortSeasonLimitValue())
                && Objects.nonNull(tdbSeasonQuality.getOverHeatingSeasonLimitValue())
                && Objects.nonNull(tdbSeasonQuality.getUnderCoolingSeasonLimitValue())
                && Objects.nonNull(tdbSeasonQuality.getUpperComfortSeasonLimitValue());
    }

    /**
     * co2配置检查
     */
    private boolean hasCo2Configured(@NonNull DemandConfigAirconditioningCO2QualityValueObject co2Quality) {
        return Objects.nonNull(co2Quality.getCarbonDioxideLimitValue())
                && Objects.nonNull(co2Quality.getCarbonDioxideUpperLimitValue());
    }

    @Override
    public String getHandlerName() {
        return LOG_TAG;
    }

    @Override
    public Future<String> processAsync(String groupCode, ProjectDTO projectDTO) {
        process(groupCode,projectDTO);
        return new AsyncResult<>(this.getHandlerName() + LocaleMessageUtil.getMessage("key_2fa808b2f7ea4112a5ed1750c743f957") + groupCode + ";projectId" + projectDTO.getProjectId());
    }

    /**
     * 检查特定日期是否存在空调需求模板的实例
     */
    private boolean checkProjectIsExistsAirConditioner(LocalDate date) {
        BaseDemandTemplateParam baseDemandTemplateParam = new BaseDemandTemplateParam();
        baseDemandTemplateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        baseDemandTemplateParam.setDate(date);
        return demandConfigGateway.isExistsCustomizedInstance(baseDemandTemplateParam);
    }

    @Override
    public void process(String groupCode, ProjectDTO projectDTO) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectDTO.getProjectId());

        //获取项目是否有空调需求配置
        LocalDateTime now = DateUtils.now();
        LocalDateTime yesterday = now.minus(1, ChronoUnit.DAYS);
        if (!checkProjectIsExistsAirConditioner(yesterday.toLocalDate())) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未配置空调需求模版");
            return;
        }

        LogExecutor.project().info(LOGGER, LOG_TAG, "任务开始");

        //先查楼层
        String projectId = projectDTO.getProjectId();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setWithColumns(Collections.singleton(DtpRwdObjectDTO.Fields.id));
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(queryParam);

        if (CollectionUtils.isEmpty(floors)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未查询到楼层数据");
            return;
        }

        DtpRwdObjectQueryParam spaceQueryParam = new DtpRwdObjectQueryParam().setWithColumns(ImmutableSet.of(DtpRwdSpaceDTO.Fields.buildingId, DtpRwdObjectDTO.Fields.localName, DtpRwdSpaceDTO.Fields.floorId));
        floors.forEach(floor -> {
            spaceQueryParam.addInfoCode(DtpRwdSpaceDTO.Fields.floorId, floor.getId());
            List<DtpRwdSpaceDTO> spaces = dtpRwdClientService.listSpace(spaceQueryParam);

            if (CollectionUtils.isEmpty(spaces)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "未查询到空间列表", Collections.singletonList(Pair.of("floor", floor.getId())));
                return;
            }

            LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(yesterday);
            LocalDateTime endTime = LocalDateTimeUtil.endOfDay(yesterday);

            //获取营业时间
            CalendarDayPlanByObjIdParam calendarDayPlanByObjIdParam = new CalendarDayPlanByObjIdParam();
            calendarDayPlanByObjIdParam.setDate(LocalDate.from(yesterday));

            List<AnalysisAirConditionerSpace> analysisTdbSpaces = new ArrayList<>();
            List<AnalysisAirConditionerSpace> analysisCo2Spaces = new ArrayList<>();
            spaces.forEach(space -> {
                //String spaceId = "Sp6101122010dc2c58eab29349f2a72174e3562e2ca7";
                String spaceId = space.getId();
                calendarDayPlanByObjIdParam.setObjId(spaceId);
                LinkedHashSet<Range<LocalTime>> businessTime = demandOpeningCalendarService.getBusinessTimeByObjId(calendarDayPlanByObjIdParam);
                if (CollectionUtil.isEmpty(businessTime)) {
                    LogExecutor.project().info(LOGGER, LOG_TAG, "空间未查到营业时间", Collections.singletonList(Pair.of("space", spaceId)));
                    return;
                }

                //先查是否有需求配置
                DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality = new DemandConfigAirconditioningTdbSeasonValueObject();
                DemandConfigAirconditioningCO2QualityValueObject co2Quality = new DemandConfigAirconditioningCO2QualityValueObject();
                fillQuality(tdbSeasonQuality, co2Quality, spaceId);
                if (!hasTdbConfigured(tdbSeasonQuality) && !hasCo2Configured(co2Quality)) {
                    LogExecutor.project().info(LOGGER, LOG_TAG, "空间未配置需求", Collections.singletonList(Pair.of("space", spaceId)));
                    return;
                }

                //查询持久化数据
                List<EnergyDataResponse> energyDataResponses = dtpRwdClientService.queryIPDataInPeriod(groupCode, projectId, Sets.newHashSet(spaceId), Sets.newHashSet(InfoCodeEnum.SPACE_CO2.getCode(), InfoCodeEnum.SPACE_TDB.getCode()), beginTime, endTime, "5min");
                if (CollectionUtils.isEmpty(energyDataResponses)) {
                    LogExecutor.project().info(LOGGER, LOG_TAG, "空间未查到持久化数据", Collections.singletonList(Pair.of("space", spaceId)));
                    return;
                }

                List<EnergyDataResponse> sortedList = energyDataResponses.stream().sorted(Comparator.comparing(EnergyDataResponse::getIpTime)).collect(Collectors.toList());
                Map<Boolean, List<EnergyDataResponse>> partitionedData = sortedList.stream().collect(Collectors.partitioningBy(data -> data.getInfoCode().equals(InfoCodeEnum.SPACE_TDB.getCode())));
                List<EnergyDataResponse> tdbList = partitionedData.get(true);
                List<EnergyDataResponse> co2List = partitionedData.get(false);

                AnalysisAirConditionerSpace tdbAnalysisSpace = AnalysisAirConditionerSpace.builder()
                        .buildingId(space.getBuildingId())
                        .spaceId(space.getId())
                        .floorId(space.getFloorId())
                        .projectId(ContextUtil.getProjectId())
                        .spaceName(space.getLocalName())
                        .day(LocalDate.from(yesterday))
                        .businessTime(businessTime)
                        .build();
                AnalysisAirConditionerSpace co2AnalysisSpace = BeanUtil.toBean(tdbAnalysisSpace, AnalysisAirConditionerSpace.class);
                tdbAnalysisSpace.setTagType(RunningMonitorSecondTypeEnum.RUN_ANALYSIS_TDB);
                co2AnalysisSpace.setTagType(RunningMonitorSecondTypeEnum.RUN_ANALYSIS_CO2);

                //分段分析
                if (hasTdbConfigured(tdbSeasonQuality)) {
                    dataAnalysis(tdbList, tdbAnalysisSpace, tdbSeasonQuality, tdbAnalysisFunction);
                    if (tdbAnalysisSpace.getTimeNumber() > 0) {
                        analysisTdbSpaces.add(tdbAnalysisSpace);
                    }
                }

                if (hasCo2Configured(co2Quality)) {
                    dataAnalysis(co2List, co2AnalysisSpace, co2Quality, co2AnalysisFunction);
                    if (co2AnalysisSpace.getTimeNumber() > 0) {
                        analysisCo2Spaces.add(co2AnalysisSpace);
                    }
                }
            });
            fillInfoCodeRuleRiskEvent(analysisTdbSpaces, beginTime, endTime,null,Arrays.asList(RiskMeasureAimLevel2.E34.getCode(),RiskMeasureAimLevel2.E35.getCode()));
            fillInfoCodeRuleRiskEvent(analysisCo2Spaces, beginTime, endTime,Arrays.asList(RiskMeasureAimLevel1.E1.getCode()),Arrays.asList(RiskMeasureAimLevel2.E5.getCode()));

            analysisTdbSpaces.addAll(analysisCo2Spaces);
            analysisAirConditionerSpaceService.saveBatchAndDel(analysisTdbSpaces);
        });
    }

    /**
     * 填充信息点规则和异常状态
     */
    private void fillInfoCodeRuleRiskEvent(@NonNull List<AnalysisAirConditionerSpace> spaces, LocalDateTime beginTime, LocalDateTime endTime,@Nullable List<String> firstRiskControlObjectives,
                                           @Nullable List<String> secondRiskControlObjectives) {
        if (CollectionUtil.isEmpty(spaces)) {
            return;
        }
        //空间的筛选：默认选中“有IOT监测”【空气（干球）温度（实际值）：tdb有实例化规则】的空间
        //空间的筛选：默认选中“有IOT监测”【空气CO2浓度（实际值）：cO2有实例化规则】的空间
        //有异常事件（风控目标 = 热舒适性差）的，且在“运行管理标准-空调需求“配置（当前选择的日期）的空间
        Set<String> spaceIds = spaces.stream().map(AnalysisAirConditionerSpace::getSpaceId).collect(Collectors.toSet());
        Map<String, Set<String>> ruleSetMap = mapObjectIdAndExistsRuleInfoCodes(spaceIds);

        List<String> abnormalSpaces = riskEventService.listAbnormalStateBySpaceIdsAppointTime(spaceIds, beginTime, endTime, firstRiskControlObjectives, secondRiskControlObjectives);
        Map<String, Boolean> abnormalSpacesMap = abnormalSpaces.stream().collect(Collectors.toMap(
                spaceId -> spaceId,
                spaceId -> true,
                (existing, replacement) -> existing
        ));

        spaces.forEach(space -> {
            if (ruleSetMap.containsKey(space.getSpaceId())) {
                space.setInfoCodeRule(ruleSetMap.get(space.getSpaceId()));
            }
            space.setAbnormalEvent(abnormalSpacesMap.getOrDefault(space.getSpaceId(), false));
        });
    }

    /**
     * iot数据分析
     **/
    private void dataAnalysis(@NonNull List<EnergyDataResponse> dataList, @NonNull AnalysisAirConditionerSpace space, @NonNull ValueObject qualityValue, @NonNull BiFunction<ValueObject, BigDecimal, BaseEnum<String>> analysisFunction) {
        //当前点位状态
        AtomicReference<BaseEnum<String>> currentStat = new AtomicReference<>();
        //分段开始点位状态
        AtomicReference<BaseEnum<String>> startStat = new AtomicReference<>();
        //分段开始点位数据
        AtomicReference<EnergyDataResponse> startData = new AtomicReference<>();
        //分段当前点位数据
        AtomicReference<EnergyDataResponse> currentData = new AtomicReference<>();
        //分段连续点位数
        AtomicInteger continueCnt = new AtomicInteger();

        //达标次数
        AtomicInteger standardCnt = new AtomicInteger();
        //总次数
        AtomicInteger totalCnt = new AtomicInteger();
        //分段
        List<TimeTagPO> timeTagPOS = new ArrayList<>();
        //获取营业时间
        dataList.forEach(data -> {

            BigDecimal ipValue = data.getIpValue();
            String ipTimeStr = data.getIpTime();
            LocalTime ipTime = LocalTime.parse(ipTimeStr, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            //只分析在营业时段内
            if (space.getBusinessTime().stream().noneMatch(range -> range.contains(ipTime))) {
                return;
            }

            if (ObjectUtil.isNull(ipValue)) {
                return;
            }

            //分析
            currentData.set(data);
            BaseEnum<String> stat = analysisFunction.apply(qualityValue, ipValue);
            currentStat.set(stat);

            if (startStat.get() == null) {
                startStat.set(currentStat.get());
                startData.set(data);
                continueCnt.set(1);
            } else if (startStat.get().equals(currentStat.get())) {
                continueCnt.incrementAndGet();
            } else {
                int cnt = continueCnt.get();
                if (cnt >= CONTINUE_NUM) {
                    //生成一个节点 舒适不存
                    addTimeTag(startData.get().getIpTime(),ipTimeStr, startStat, timeTagPOS, continueCnt, space.getSpaceId());
                    /*
                    String startTime = LocalDateTime.parse(startData.get().getIpTime(), DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String endTime = ipTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));



                    if (!startStat.get().value().equals(AirTerminalTemperatureIntervalEnum.APPROPRIATE.value())) {
                        TimeTagPO timeTagPO = TimeTagPO.builder()
                                .time(Range.between(LocalTime.parse(startTime), LocalTime.parse(endTime)))
                                .tag(startStat.get().toString())
                                .build();
                        timeTagPOS.add(timeTagPO);
                    }
                    LogExecutor.project().info(LOGGER, LOG_TAG, "数据区间", Arrays.asList(Pair.of("space", space.getSpaceId()), Pair.of("状态", startStat.get().toString()), Pair.of("开始时间", startTime), Pair.of("结束时间", endTime), Pair.of("连续点数", continueCnt.get())));

                     */
                }
                startStat.set(currentStat.get());
                startData.set(data);
                continueCnt.set(1);
            }

            totalCnt.incrementAndGet();
            if (currentStat.get().value().equals(AirTerminalTemperatureIntervalEnum.APPROPRIATE.value())) {
                //达标
                standardCnt.incrementAndGet();
            }
        });

        if (continueCnt.get() >= CONTINUE_NUM) {
            addTimeTag(startData.get().getIpTime(),currentData.get().getIpTime(), startStat, timeTagPOS, continueCnt, space.getSpaceId());
        }

        space.setTimeNumber(totalCnt.get());
        space.setSatisfyTimeNumber(standardCnt.get());
        space.setTimeTag(timeTagPOS);
    }

    private void addTimeTag(String startTime, String endTime, AtomicReference<BaseEnum<String>> startStat, List<TimeTagPO> timeTagPOS, AtomicInteger continueCnt,String spaceId){
        startTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        endTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        if (!startStat.get().value().equals(AirTerminalTemperatureIntervalEnum.APPROPRIATE.value())) {
            TimeTagPO timeTagPO = TimeTagPO.builder()
                    .time(Range.between(LocalTime.parse(startTime), LocalTime.parse(endTime)))
                    .tag(startStat.get().toString())
                    .build();
            timeTagPOS.add(timeTagPO);
        }
        LogExecutor.project().info(LOGGER, LOG_TAG, "数据区间", Arrays.asList(Pair.of("space", spaceId), Pair.of("状态", startStat.get().toString()), Pair.of("开始时间", startTime), Pair.of("结束时间", endTime), Pair.of("连续点数", continueCnt.get())));
    }

    /**
     * 获取空气（干球）温度（实际值）：有实例化规则
     */
    private Map<String, Set<String>> mapObjectIdAndExistsRuleInfoCodes(@NonNull Set<String> spaceIds) {
        Set<String> infoCodeList = Sets.newHashSet(InfoCodeEnum.SPACE_CO2.getCode(), InfoCodeEnum.SPACE_TDB.getCode());
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        classCodeAndObjectIds.put(CommonConst.DtpRwdConst.GeneralZone, spaceIds);
        return abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, infoCodeList);
    }

    /**
     * 填充温度和CO2浓度质量配置
     *
     * @param tdbSeasonQuality 温度季节品质配置
     * @param co2Quality       二氧化碳品质配置
     * @param spaceId          空间ID
     */
    private void fillQuality(DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality, DemandConfigAirconditioningCO2QualityValueObject co2Quality, @NonNull String spaceId) {
        try {
            LocalDateTime now = DateUtils.now();
            LocalDate yesterday = LocalDate.from(now.minus(1, ChronoUnit.DAYS));

            DemandConfigQueryParam param = new DemandConfigQueryParam();
            param.setInstanceIds(Sets.newHashSet(spaceId));
            param.setInstanceType(DemandInstanceTypeEnum.SPACE);
            param.setDate(yesterday);
            param.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
            DemandTemplate demandTemplate = param.queryAndGetTemplate();

            DemandConfigByInstanceQueryParam instanceQueryParamparam = new DemandConfigByInstanceQueryParam();
            instanceQueryParamparam.setDate(yesterday);
            instanceQueryParamparam.setTemplateId(demandTemplate.getId());

            DemandInstanceValueObject instance = new DemandInstanceValueObject();
            instance.setInstanceType(DemandInstanceTypeEnum.SPACE);
            instance.setInstanceId(spaceId);
            instanceQueryParamparam.setInstance(instance);

            Optional<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> configByInstance = demandConfigGateway.getConfigByInstance(instanceQueryParamparam);
            if (configByInstance.isPresent()) {
                DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig =
                        configByInstance.get().getFinalConfig();
                if (Objects.isNull(finalConfig) || ObjectUtil.isNull(finalConfig.getGlobalConfig())) {
                    return;
                }

                DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = finalConfig.getGlobalConfig().getEnvironmentQuality();
                if (ObjectUtil.isNull(environmentQuality)) {
                    return;
                }

                DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
                MidCalendarAirSeasonTypeEnum airSeasonType = midCalendarClientService.getAirSeasonType(ContextUtil.getProjectId(), yesterday);
                if (Objects.nonNull(airSeasonType) && Objects.nonNull(tdbQuality)) {
                    DemandConfigAirconditioningTdbSeasonValueObject currentSeasonQuality = tdbQuality.getCurrentSeasonQuality(airSeasonType);
                    if (ObjectUtil.isNotNull(currentSeasonQuality)) {
                        BeanUtil.copyProperties(currentSeasonQuality, tdbSeasonQuality);
                        LogExecutor.project().info(LOGGER, LOG_TAG, "tdb标准", Arrays.asList(Pair.of("spaceId", spaceId), Pair.of("tdb", tdbSeasonQuality)));
                    }
                }
                BeanUtil.copyProperties(environmentQuality.getCO2Quality(), co2Quality);
                LogExecutor.project().info(LOGGER, LOG_TAG, "co2标准", Arrays.asList(Pair.of("spaceId", spaceId), Pair.of("co2", co2Quality)));
            }
        }catch (Exception e){
            LogExecutor.project().error(LOGGER, LOG_TAG, "获取需求配置失败", e, Arrays.asList(Pair.of("spaceId", spaceId)));
        }
    }

    //@Scheduled(cron = "* * * * * ?")
    public void process() {
        String groupCode = "TYXK";
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectId("Pj9522457558");
        projectDTO.setProjectLocalId("Pj9522457558");
        process(groupCode, projectDTO);
    }
}