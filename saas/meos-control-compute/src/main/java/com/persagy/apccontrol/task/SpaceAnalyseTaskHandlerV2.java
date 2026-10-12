package com.persagy.apccontrol.task;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.persagy.apccontrol.basic.model.vo.demand.DemandConfigVO;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel1;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.service.airterminal.AnalysisAirConditionerSpaceService;
import com.persagy.apccontrol.basic.service.demand.DemandQueryService;
import com.persagy.apccontrol.basic.service.systemgraph.impl.RiskEventService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTimeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceBatchQueryParam;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
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
import java.time.Duration;
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
@Component("spaceAnalyseTaskHandlerV2")
@Slf4j
public class SpaceAnalyseTaskHandlerV2 implements TecXxJobHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceAnalyseTaskHandlerV2.class);
    public static final String LOG_TAG = "运行分析-空间分析V2";

    private final DtpRwdClientService dtpRwdClientService;
    private final DemandConfigGateway demandConfigGateway;
    private final MidCalendarClientService midCalendarClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final DemandOpeningCalendarService demandOpeningCalendarService;
    private final RiskEventService riskEventService;
    private final AnalysisAirConditionerSpaceService analysisAirConditionerSpaceService;
    private final DemandQueryService demandQueryService;

    private final WeatherClientService weatherClientService;
    private LocalDateTime processDay;

    //分段连续点位数
    private static final int CONTINUE_NUM = 3;

    public SpaceAnalyseTaskHandlerV2(DtpRwdClientService dtpRwdClientService,
                                     DemandConfigGateway demandConfigGateway,
                                     MidCalendarClientService midCalendarClientService,
                                     AbstractIotDataCacheServer abstractIotDataCacheServer, DemandOpeningCalendarService demandOpeningCalendarService, RiskEventService riskEventService, AnalysisAirConditionerSpaceService analysisAirConditionerSpaceService, DemandQueryService demandQueryService, WeatherClientService weatherClientService) {
        this.dtpRwdClientService = dtpRwdClientService;
        this.demandConfigGateway = demandConfigGateway;
        this.midCalendarClientService = midCalendarClientService;
        this.abstractIotDataCacheServer = abstractIotDataCacheServer;
        this.demandOpeningCalendarService = demandOpeningCalendarService;
        this.riskEventService = riskEventService;
        this.analysisAirConditionerSpaceService = analysisAirConditionerSpaceService;
        this.demandQueryService = demandQueryService;
        this.weatherClientService = weatherClientService;
    }

    /**
     * 空间温度分析
     */
    private BiFunction<ValueObject, BigDecimal, Pair<BaseEnum<String>,BigDecimal>> tdbAnalysisFunction = (valueObject, ipValue) -> {
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
        if (ipValue.compareTo(overHeatingSeasonLimitValue) > 0) {
            return Pair.of(AirTerminalTemperatureIntervalEnum.TOO_HOT, ipValue.subtract(overHeatingSeasonLimitValue));
        } else if (ipValue.compareTo(underCoolingSeasonLimitValue) < 0) {
            return Pair.of(AirTerminalTemperatureIntervalEnum.TOO_COLD, underCoolingSeasonLimitValue.subtract(ipValue));
        } else if (ipValue.compareTo(underCoolingSeasonLimitValue) >= 0 && ipValue.compareTo(lowerComfortSeasonLimitValue) < 0) {
            return Pair.of(AirTerminalTemperatureIntervalEnum.COLD, lowerComfortSeasonLimitValue.subtract(ipValue));
        } else if (ipValue.compareTo(upperComfortSeasonLimitValue) > 0 && ipValue.compareTo(overHeatingSeasonLimitValue) <= 0) {
            return Pair.of(AirTerminalTemperatureIntervalEnum.HOT, ipValue.subtract(upperComfortSeasonLimitValue));
        } else {
            return Pair.of(AirTerminalTemperatureIntervalEnum.APPROPRIATE,ipValue);
        }
    };

    /**
     * 空间co2分析
     */
    private BiFunction<ValueObject, BigDecimal, Pair<BaseEnum<String>,BigDecimal>> co2AnalysisFunction = (valueObject, ipValue) -> {
        DemandConfigAirconditioningCO2QualityValueObject co2Quality = (DemandConfigAirconditioningCO2QualityValueObject) valueObject;
        //舒适区上限
        BigDecimal carbonDioxideLimitValue = co2Quality.getCarbonDioxideLimitValue();
        //过高上限
        BigDecimal carbonDioxideUpperLimitValue = co2Quality.getCarbonDioxideUpperLimitValue();

        if (ipValue.compareTo(carbonDioxideUpperLimitValue) > 0) {
            return Pair.of(AirTerminalCarbonDioxideIntervalEnum.TOO_HIGH, ipValue.subtract(carbonDioxideUpperLimitValue));
        } else if (ipValue.compareTo(carbonDioxideLimitValue) > 0) {
            return Pair.of(AirTerminalCarbonDioxideIntervalEnum.HIGH, ipValue.subtract(carbonDioxideLimitValue));
        } else {
            return Pair.of(AirTerminalCarbonDioxideIntervalEnum.APPROPRIATE, ipValue);
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
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectDTO.getProjectId());
        LocalDateTime now = DateUtils.now();
        this.processDay = now.minus(1, ChronoUnit.DAYS);
        process(groupCode, projectDTO);
        return new AsyncResult<>(this.getHandlerName() + LocaleMessageUtil.getMessage("key_2fa808b2f7ea4112a5ed1750c743f957") + groupCode + ";projectId" + projectDTO.getProjectId());
    }

    public boolean processWithDay(String groupCode, ProjectDTO projectDTO, LocalDate day) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectDTO.getProjectId());
        this.processDay = LocalDateTime.of(day, LocalTime.MIN);
        process(groupCode, projectDTO);
        return true;
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

        //获取项目是否有空调需求配置
        LocalDateTime yesterday = this.processDay;
        if (!checkProjectIsExistsAirConditioner(yesterday.toLocalDate())) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未配置空调需求模版");
            return;
        }

        LogExecutor.project().info(LOGGER, LOG_TAG, "任务开始:" + yesterday);

        String projectId = projectDTO.getProjectId();
        List<DemandConfigAggregate<BaseDemandConfig>> demandConfigVOS = listDemandConfigAirconditioning();

        if (CollectionUtils.isEmpty(demandConfigVOS)) {
            LogExecutor.project().info(LOGGER, LOG_TAG, "未查询到需求配置", Collections.singletonList(Pair.of("date", yesterday)));
            return;
        }

        LocalDateTime beginTime = LocalDateTimeUtil.beginOfDay(yesterday);
        LocalDateTime endTime = LocalDateTimeUtil.endOfDay(yesterday);

        //获取营业时间
        CalendarDayPlanByObjIdParam calendarDayPlanByObjIdParam = new CalendarDayPlanByObjIdParam();
        calendarDayPlanByObjIdParam.setDate(LocalDate.from(yesterday));

        List<AnalysisAirConditionerSpace> analysisTdbSpaces = new ArrayList<>();
        List<AnalysisAirConditionerSpace> analysisCo2Spaces = new ArrayList<>();
        demandConfigVOS.forEach(spaceConfig -> {
            DemandInstanceValueObject instance = spaceConfig.getIdentifier().getInstance();
            if (instance == null) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "实例为空", Collections.singletonList(Pair.of("spaceConfig", spaceConfig.getIdentifier())));
                return;
            }
            String spaceId = instance.getInstanceId();
            if (spaceId == null) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "空间ID为空", Collections.singletonList(Pair.of("instance", instance)));
                return;
            }
            DemandConfigValueObject<BaseDemandConfig> finalConfig = spaceConfig.getFinalConfig();
            if (Objects.isNull(finalConfig) || ObjectUtil.isNull(finalConfig.getGlobalConfig())) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "空间未查询到需求配置", Collections.singletonList(Pair.of("spaceId", spaceId)));
                return;
            }

            BaseDemandConfig globalConfig = finalConfig.getGlobalConfig();
            if (!(globalConfig instanceof DemandConfigAirconditioningValueObject)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "需求配置错误", Collections.singletonList(Pair.of("spaceId", spaceId)));
                return;
            }

            calendarDayPlanByObjIdParam.setObjId(spaceId);
            LinkedHashSet<Range<LocalTime>> businessTime = demandOpeningCalendarService.getBusinessTimeByObjId(calendarDayPlanByObjIdParam);
            if (CollectionUtil.isEmpty(businessTime)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "空间未查到营业时间", Collections.singletonList(Pair.of("space", spaceId)));
                return;
            }

            //先查是否有需求配置
            DemandConfigAirconditioningValueObject environmentQualityConfig = (DemandConfigAirconditioningValueObject) globalConfig;
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality = environmentQualityConfig.getEnvironmentQuality();
            DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality = new DemandConfigAirconditioningTdbSeasonValueObject();
            DemandConfigAirconditioningCO2QualityValueObject co2Quality = new DemandConfigAirconditioningCO2QualityValueObject();
            fillQuality(tdbSeasonQuality, co2Quality, environmentQuality, spaceId);
            if (!hasTdbConfigured(tdbSeasonQuality) && !hasCo2Configured(co2Quality)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "空间未配置需求", Collections.singletonList(Pair.of("space", spaceId)));
                return;
            }

            //查询持久化数据
            List<EnergyDataResponse> energyDataResponses = dtpRwdClientService.queryIPDataInPeriod(groupCode, projectId, Sets.newHashSet(spaceId), Sets.newHashSet(InfoCodeEnum.SPACE_CO2.getCode(), InfoCodeEnum.SPACE_TDB.getCode()), beginTime, endTime, "15min");
            if (CollectionUtils.isEmpty(energyDataResponses)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "空间未查到持久化数据", Collections.singletonList(Pair.of("space", spaceId)));
                return;
            }

            List<EnergyDataResponse> sortedList = energyDataResponses.stream().sorted(Comparator.comparing(EnergyDataResponse::getIpTime)).collect(Collectors.toList());
            Map<Boolean, List<EnergyDataResponse>> partitionedData = sortedList.stream().collect(Collectors.partitioningBy(data -> data.getInfoCode().equals(InfoCodeEnum.SPACE_TDB.getCode())));
            List<EnergyDataResponse> tdbList = partitionedData.get(true);
            List<EnergyDataResponse> co2List = partitionedData.get(false);

            DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
            queryParam.setId(spaceId);
            DtpRwdSpaceDTO space = dtpRwdClientService.getSpace(queryParam);
            //List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(queryParam);
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

            //达标率
            AnalysisAirConditionerSpace tdbQualifySpace = BeanUtil.toBean(tdbAnalysisSpace, AnalysisAirConditionerSpace.class);
            AnalysisAirConditionerSpace co2QualifySpace = BeanUtil.toBean(tdbAnalysisSpace, AnalysisAirConditionerSpace.class);

            tdbAnalysisSpace.setTagType(RunningMonitorSecondTypeEnum.RUN_ANALYSIS_TDB);
            co2AnalysisSpace.setTagType(RunningMonitorSecondTypeEnum.RUN_ANALYSIS_CO2);

            tdbQualifySpace.setTagType(RunningMonitorSecondTypeEnum.RUN_QUALIFY_TDB);
            co2QualifySpace.setTagType(RunningMonitorSecondTypeEnum.RUN_QUALIFY_CO2);

            //分段分析
            if (hasTdbConfigured(tdbSeasonQuality)) {
                dataAnalysis(tdbList, tdbAnalysisSpace, tdbSeasonQuality, tdbAnalysisFunction, environmentQuality, tdbQualifySpace);
                if (tdbAnalysisSpace.getTimeNumber() > 0) {
                    analysisTdbSpaces.add(tdbAnalysisSpace);
                    analysisTdbSpaces.add(tdbQualifySpace);
                }
            }

            if (hasCo2Configured(co2Quality)) {
                dataAnalysis(co2List, co2AnalysisSpace, co2Quality, co2AnalysisFunction, environmentQuality, co2QualifySpace);
                if (co2AnalysisSpace.getTimeNumber() > 0) {
                    analysisCo2Spaces.add(co2AnalysisSpace);
                    analysisCo2Spaces.add(co2QualifySpace);
                }
            }
        });
        fillInfoCodeRuleRiskEvent(analysisTdbSpaces, beginTime, endTime, null, Arrays.asList(RiskMeasureAimLevel2.E34.getCode(), RiskMeasureAimLevel2.E35.getCode()));
        fillInfoCodeRuleRiskEvent(analysisCo2Spaces, beginTime, endTime, Arrays.asList(RiskMeasureAimLevel1.E1.getCode()), Arrays.asList(RiskMeasureAimLevel2.E5.getCode()));

        analysisTdbSpaces.addAll(analysisCo2Spaces);
        analysisAirConditionerSpaceService.deleteByDay( yesterday.toLocalDate());
        analysisAirConditionerSpaceService.saveBatchAndDel(analysisTdbSpaces);
    }

    /**
     * 填充信息点规则和异常状态
     */
    private void fillInfoCodeRuleRiskEvent(@NonNull List<AnalysisAirConditionerSpace> spaces, LocalDateTime beginTime, LocalDateTime endTime, @Nullable List<String> firstRiskControlObjectives,
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
    private void dataAnalysis(@NonNull List<EnergyDataResponse> dataList, @NonNull AnalysisAirConditionerSpace space, @NonNull ValueObject qualityValue, @NonNull BiFunction<ValueObject, BigDecimal, Pair<BaseEnum<String>,BigDecimal>> analysisFunction, DemandConfigAirconditioningEnvironmentQualityObject environmentQuality, @NonNull AnalysisAirConditionerSpace qualifySpace) {
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
            Pair<BaseEnum<String>,BigDecimal> stat = analysisFunction.apply(qualityValue, ipValue);

            totalCnt.incrementAndGet();
            //生成一个节点 舒适不存
            addTimeTag(data.getIpTime(), ipTimeStr, stat, timeTagPOS, continueCnt, space.getSpaceId());
        });

        space.setTimeNumber(totalCnt.get());
        space.setSatisfyTimeNumber(totalCnt.get());
        space.setTimeTag(timeTagPOS);

        computeQualifyRate(space, qualifySpace, environmentQuality);
    }

    private void computeQualifyRate(AnalysisAirConditionerSpace space, AnalysisAirConditionerSpace qualifySpace, DemandConfigAirconditioningEnvironmentQualityObject environmentQuality) {
        List<TimeTagPO> timeTagPOS = space.getTimeTag();
        if (CollUtil.isNotEmpty(timeTagPOS)) {
            //是否参与调度
            Boolean isParticipateInScheduling = environmentQuality.getIsParticipateInScheduling();
            BigDecimal continuousScheduledHour = null;
            List<DemandTimeIntervalValueObject> unscheduledTimeRange = null;
            if (Boolean.TRUE.equals(isParticipateInScheduling)) {
                if (space.getTagType().equals(RunningMonitorSecondTypeEnum.RUN_ANALYSIS_TDB)) {
                    continuousScheduledHour = environmentQuality.getTdbQuality().getTemperatureContinuousScheduledHour();
                    unscheduledTimeRange = environmentQuality.getTdbQuality().getTemperatureUnscheduledTimeRange();
                } else if (space.getTagType().equals(RunningMonitorSecondTypeEnum.RUN_ANALYSIS_CO2)) {
                    continuousScheduledHour = environmentQuality.getCO2Quality().getCarbonDioxideContinuousScheduledHour();
                    unscheduledTimeRange = environmentQuality.getCO2Quality().getCarbonDioxideUnscheduledTimeRange();
                }
            } else {
                DemandTimeIntervalValueObject demandTimeIntervalValueObject = new DemandTimeIntervalValueObject();
                demandTimeIntervalValueObject.setBeginTime(DemandTimeValueObject.ofAbsoluteTimeByMin());
                demandTimeIntervalValueObject.setEndTime(new DemandTimeValueObject().setTimeType(DemandTimeTypeEnum.ABSOLUTE).setAbsoluteTime(LocalTime.MAX));
                unscheduledTimeRange = Collections.singletonList(demandTimeIntervalValueObject);
            }

            Map<LocalTime, TimeTagPO> computeTimeTagMap = new HashMap<>();
            Map<LocalTime, TimeTagPO> timeTagPOMap = new HashMap<>(timeTagPOS.size());
            timeTagPOS.forEach(timeTagPO -> {
                timeTagPOMap.put(timeTagPO.getTime().getMinimum(), timeTagPO);
            });

            BigDecimal finalContinuousScheduledHour = continuousScheduledHour;
            List<DemandTimeIntervalValueObject> finalUnscheduledTimeRange = unscheduledTimeRange;
            // 只关心一天内的时间顺序，用固定日期把 LocalTime 临时转换成不会回绕的时间轴。
            LocalDate baseDate = LocalDate.of(2000, 1, 1);
            //只计算营业时段内

            //不达标次数
            Map<LocalTime, Boolean> noQualifyMap = new HashMap<>();
            space.getBusinessTime().stream().forEach(range -> {
                LocalTime minimum = range.getMinimum();
                LocalTime maximum = range.getMaximum();

                //最后时间点
                LocalTime end = LocalTime.of(23, 0);
                for (LocalTime time = minimum; time.isBefore(maximum); time = time.plusHours(1)) {
                    //获取小时整点
                    LocalTime hour = time.withMinute(0).withSecond(0);

                    //15，30，45，00四个点位
                    for (int i = 1; i <= 4; i++) {
                        LocalTime minute = hour.plusMinutes(i * 15);
                        TimeTagPO timeTagPO = timeTagPOMap.get(minute);
                        if (timeTagPO != null) {
                            computeTimeTagMap.putIfAbsent(hour, TimeTagPO.builder()
                                    .time(Range.between(hour, hour.withMinute(59)))
                                    .tag(AirTerminalTemperatureIntervalEnum.APPROPRIATE.value())
                                    .build());

                            //System.out.println(minute);
                            //System.out.println(timeTagPO.getTag());
                            String tag = timeTagPO.getTagKey();
                            //出现一次"过冷" / "过热" 不达标
                            if (AirTerminalTemperatureIntervalEnum.TOO_COLD.value().equals(tag)
                                    || AirTerminalTemperatureIntervalEnum.TOO_HOT.value().equals(tag)
                                    || AirTerminalCarbonDioxideIntervalEnum.TOO_HIGH.value().equals(tag)) {
                                computeTimeTagMap.get(hour).setTag(timeTagPO.getTag());
                                noQualifyMap.put(hour, true);
                                break;
                            } else if ((AirTerminalTemperatureIntervalEnum.HOT.value().equals(tag)
                                    || AirTerminalTemperatureIntervalEnum.COLD.value().equals(tag)
                                    || AirTerminalCarbonDioxideIntervalEnum.HIGH.value().equals(tag))
                                    && finalContinuousScheduledHour != null) {

                                LocalDateTime minimumDateTime = LocalDateTime.of(baseDate, minimum);
                                LocalDateTime minuteDateTime = LocalDateTime.of(baseDate, minute);
                                // 当前小时段按 15、30、45、00 四个点计算，23 点小时段里的 00:00 表示次日 00:00。
                                if (minute.isBefore(hour)) {
                                    minuteDateTime = minuteDateTime.plusDays(1);
                                }

                                // 从当前点向前每 15 分钟回溯，直到营业开始时间；用 LocalDateTime 避免 00:00 往前减后回绕到 23:45。
                                LocalDateTime beforeDateTime = minuteDateTime.minusMinutes(15);
                                while (!beforeDateTime.isBefore(minimumDateTime)) {
                                    LocalTime beforeTime = beforeDateTime.toLocalTime();
                                    TimeTagPO beforeTimeTagPO = timeTagPOMap.get(beforeTime);
                                    if (beforeTimeTagPO != null) {
                                        if (checkContinuity(tag, beforeTimeTagPO)) {
                                            Duration between = Duration.between(beforeDateTime, minuteDateTime);
                                            // 已连续偏离达到配置时长时，当前小时判定为不达标。
                                            if (between.getSeconds() >= finalContinuousScheduledHour.doubleValue() * 3600) {
                                                computeTimeTagMap.get(hour).setTag(timeTagPO.getTag());
                                                noQualifyMap.put(hour, true);
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                    beforeDateTime = beforeDateTime.minusMinutes(15);
                                }
                            }

                            //不可调度时间段，出现非"舒适" / "无数据"
                            if (AirTerminalTemperatureIntervalEnum.APPROPRIATE.value().equals(computeTimeTagMap.get(hour).getTag())) {
                                if (!AirTerminalTemperatureIntervalEnum.APPROPRIATE.value().equals(tag) && !AirTerminalCarbonDioxideIntervalEnum.APPROPRIATE.value().equals(tag) && finalUnscheduledTimeRange != null) {
                                    for (DemandTimeIntervalValueObject demandTimeIntervalValueObject : finalUnscheduledTimeRange) {
                                        LocalTime beginTime = demandTimeIntervalValueObject.getBeginTime().getAbsoluteTime();
                                        LocalTime endTime = demandTimeIntervalValueObject.getEndTime().getAbsoluteTime();

                                        if (!minute.isBefore(beginTime) && minute.isBefore(endTime)) {
                                            computeTimeTagMap.get(hour).setTag(timeTagPO.getTag());
                                            noQualifyMap.put(hour, true);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!time.isBefore(end)) {
                        break;
                    }
                }
            });
           if(CollectionUtil.isNotEmpty(computeTimeTagMap.values())){
               qualifySpace.setTimeTag(new ArrayList<>(computeTimeTagMap.values()));
               qualifySpace.setTimeNumber(computeTimeTagMap.size());
               qualifySpace.setSatisfyTimeNumber(computeTimeTagMap.size() - noQualifyMap.size());
           }
        }
    }

    //检查是否连续状态
    private boolean checkContinuity(String tag, TimeTagPO preTagData) {
        String preTag = preTagData == null ? null : preTagData.getTagKey();
        if (AirTerminalTemperatureIntervalEnum.HOT.value().equals(tag)) {
            return AirTerminalTemperatureIntervalEnum.HOT.value().equals(preTag) || AirTerminalTemperatureIntervalEnum.TOO_HOT.value().equals(preTag);
        } else if (AirTerminalTemperatureIntervalEnum.COLD.value().equals(tag)) {
            return AirTerminalTemperatureIntervalEnum.COLD.value().equals(preTag) || AirTerminalTemperatureIntervalEnum.TOO_COLD.value().equals(preTag);
        } else if (AirTerminalCarbonDioxideIntervalEnum.HIGH.value().equals(tag)) {
            return AirTerminalCarbonDioxideIntervalEnum.HIGH.value().equals(preTag) || AirTerminalCarbonDioxideIntervalEnum.TOO_HIGH.value().equals(preTag);
        }
        return false;
    }

    private void addTimeTag(String startTime, String endTime, Pair<BaseEnum<String>,BigDecimal> startStat, List<TimeTagPO> timeTagPOS, AtomicInteger continueCnt, String spaceId) {
        startTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        endTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")).format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        TimeTagPO timeTagPO = TimeTagPO.builder()
                .time(Range.between(LocalTime.parse(startTime), LocalTime.parse(endTime)))
                .tag(startStat.toString("%s:%s"))
                .build();
        timeTagPOS.add(timeTagPO);

        LogExecutor.project().info(LOGGER, LOG_TAG, "数据区间", Arrays.asList(Pair.of("space", spaceId), Pair.of("状态", startStat.toString()), Pair.of("开始时间", startTime), Pair.of("结束时间", endTime), Pair.of("连续点数", continueCnt.get())));
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

    private List<DemandConfigAggregate<BaseDemandConfig>> listDemandConfigAirconditioning() {
        LocalDate yesterday = this.processDay.toLocalDate();
        DemandConfigQueryParam param = new DemandConfigQueryParam();

        param.setInstanceType(DemandInstanceTypeEnum.SPACE);
        param.setDate(yesterday);
        param.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        DemandTemplate demandTemplate = param.queryAndGetTemplate();

        DemandConfigQueryParam demandQueryParam = new DemandConfigQueryParam();
        demandQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        demandQueryParam.setTemplateId(demandTemplate.getId());
        demandQueryParam.setDate(yesterday);
        demandQueryParam.setQueryInstanceTag(false);

        DemandConfigByInstanceBatchQueryParam instanceBatchQueryParam = new DemandConfigByInstanceBatchQueryParam();
        instanceBatchQueryParam.setDate(yesterday);
        instanceBatchQueryParam.setFilterShieldedSpaces(true);
        instanceBatchQueryParam.setInstanceTypeAndInstanceIds(Collections.singletonMap(DemandInstanceTypeEnum.SPACE, null));
        instanceBatchQueryParam.setTemplateId(demandTemplate.getId());
        instanceBatchQueryParam.setAutoFillEmptyInstanceIds(true);

        return demandConfigGateway.listConfigsByInstances(instanceBatchQueryParam);

        //return demandQueryService.queryDemandConfig(param);
    }

    private void fillQuality(DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality, DemandConfigAirconditioningCO2QualityValueObject co2Quality, @NonNull DemandConfigAirconditioningEnvironmentQualityObject environmentQuality, @NonNull String spaceId) {
        LocalDate yesterday = this.processDay.toLocalDate();
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

    public static Duration calculateTimeDifference(LocalTime start, LocalTime end) {
        if (end.isBefore(start)) {
            // 跨天情况：从开始时间到午夜，再从午夜到结束时间
            return Duration.between(start, LocalTime.MAX)
                    .plus(Duration.between(LocalTime.MIN, end))
                    .plus(Duration.ofNanos(1)); // 加上午夜的一个纳秒
        }
        return Duration.between(start, end);
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
