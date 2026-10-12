package com.persagy.apccontrol.basic.service.airterminal.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.abstracts.AbstractRunningMonitorServer;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.convert.TagConverter;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.calculate.CalculateResult;
import com.persagy.apccontrol.basic.model.calculate.CalculateResultConditionDetail;
import com.persagy.apccontrol.basic.model.calculate.CalculateResultDetail;
import com.persagy.apccontrol.basic.model.context.airterminal.AirTerminalEquipmentStatusStatisticsContext;
import com.persagy.apccontrol.basic.model.context.airterminal.AirTerminalQualityStatisticsContext;
import com.persagy.apccontrol.basic.model.dto.ObjectClassCodeDTO;
import com.persagy.apccontrol.basic.model.dto.SingleObjectDTO;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalEquipmentTagDTO;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectionTrendTableDto;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.*;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHour24ForecastDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherHourlyDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherNowWeatherDTO;
import com.persagy.apccontrol.basic.model.entity.EquipRecord;
import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.model.enumtype.EnumCalculateValueType;
import com.persagy.apccontrol.basic.model.enumtype.OnlineStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.*;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeRunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.*;
import com.persagy.apccontrol.basic.model.enumtype.terminal.ObjectTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.weather.WeatherDataTypeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.*;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandSpaceByGroupFloorQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.SpaceInfoQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.TagInfoQueryParam;
import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.TagInfoVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.*;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.DashboardEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.RcCalculateResultService;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import com.persagy.apccontrol.basic.service.*;
import com.persagy.apccontrol.basic.service.airterminal.AirTerminalService;
import com.persagy.apccontrol.basic.service.dashboard.space.AirTerminalQuerySpace;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.airterminal.converter.AirTerminalConverter;
import com.persagy.apccontrol.basic.structure.cacherwd.key.CacheObjectModelKeyConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.projection.CacheObjectExtensionFieldConfig;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheIotRuleRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheObjectRelRepository;
import com.persagy.apccontrol.basic.structure.cacherwd.repository.CacheRuleRepository;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirTerminalStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.param.DemandConfigByInstanceQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.*;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.CalculateComputeUtil;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import com.persagy.validate.utility.util.BigDecimalUtils;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 空调末端服务实现
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
public class AirTerminalServiceImpl implements AirTerminalService, BizComponent<AirTerminalService> {

    /**
     * 空间设备接口从缓存对象主表读取的空间业务字段。
     *
     * <p>模型视图字段由独立模型图接口读取，本接口不查询
     * {@code bimId}、{@code bimLocation} 和 {@code outline}。</p>
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> SPACE_EQUIPMENT_SPACE_FIELDS = Sets.newHashSet(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.name,
            DtpRwdObjectDTO.Fields.localId,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.objType,
            DtpRwdObjectDTO.Fields.valid,
            DtpRwdSpaceDTO.Fields.buildingId,
            DtpRwdSpaceDTO.Fields.floorId,
            DtpRwdSpaceDTO.Fields.roomFuncType);

    /**
     * 空间设备接口从缓存对象主表读取的设备业务字段。
     *
     * <p>模型视图字段由独立模型图接口读取，本接口不查询
     * {@code bimId} 和 {@code bimLocation}。</p>
     *
     * @since v7.260701.0
     */
    private static final @NonNull Set<String> SPACE_EQUIPMENT_EQUIPMENT_FIELDS = Sets.newHashSet(
            DtpRwdObjectDTO.Fields.id,
            DtpRwdObjectDTO.Fields.name,
            DtpRwdObjectDTO.Fields.localId,
            DtpRwdObjectDTO.Fields.localName,
            DtpRwdObjectDTO.Fields.classCode,
            DtpRwdObjectDTO.Fields.objType,
            DtpRwdObjectDTO.Fields.valid,
            DtpRwdEquipmentDTO.Fields.buildingId,
            DtpRwdEquipmentDTO.Fields.floorId,
            InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value());

    private final AirTerminalConverter airTerminalConverter;
    private final MidCalendarClientService midCalendarClientService;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final WeatherClientService weatherClientService;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final AbstractCommonServer abstractCommonServer;
    private final InstanceService instanceService;
    private final BizComputeService bizComputeService;
    private final RcCalculateResultService rcCalculateResultService;
    private final AbstractRunningMonitorServer abstractRunningMonitorServer;
    private final TagConverter tagConverter;
    private final EquipRecordService equipRecordService;
    private final BizDtpRwdService bizDtpRwdService;
    private final InfoCodeGateway infoCodeGateway;
    private final Executor executor;
    private final DemandCalendarQueryService demandCalendarQueryService;
    private final DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;
    private final RunningMonitorAirTerminalStrategy runningMonitorAirTerminalStrategy;
    private final DemandInstanceGateway demandInstanceGateway;
    private final NacosSwitchConfig nacosSwitchConfig;

    /**
     * 末端空间查询组件，用于统一读取持久化的空间范围配置。
     *
     * @since v7.260701.0
     */
    private final @NonNull AirTerminalQuerySpace airTerminalQuerySpace;

    /**
     * 缓存对象模型共享 READY 状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService
            cacheObjectModelStatusService;

    /**
     * 新缓存键与对象 ID 关联仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRepository cacheObjectRepository;

    /**
     * 新缓存对象关系仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectRelRepository
            cacheObjectRelRepository;

    /**
     * 新缓存规则领域仓储，按对象和部署端规则类型读取规则。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRuleRepository cacheRuleRepository;

    /**
     * 当前部署端规则类型提供者。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheIotRuleRepository cacheIotRuleRepository;

    private static final ExpressRunner EXPRESS_RUNNER = new ExpressRunner(true, false);

    @Override
    public AirTerminalQualityIntervalVO getQualityInterval(@NonNull AirTerminalQualityIntervalQueryParam param) {
        String projectId = param.getProjectId();

        MidCalendarAirSeasonTypeEnum airSeasonType = midCalendarClientService
                .getAirSeasonType(projectId, DateUtils.now().toLocalDate());
        DemandConfigAirconditioningValueObject projectGlobalConfig = queryDemandProjectGlobalConfig();
        DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality = null;
        DemandConfigAirconditioningCO2QualityValueObject co2Quality = null;
        if (Objects.nonNull(projectGlobalConfig)) {
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                    projectGlobalConfig.getEnvironmentQuality();
            if (Objects.nonNull(environmentQuality)) {
                DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
                if (Objects.nonNull(tdbQuality)) {
                    tdbSeasonQuality = tdbQuality.getCurrentSeasonQuality(airSeasonType);
                }
                co2Quality = environmentQuality.getCO2Quality();
            }
        }
        List<BaseEnumVO> temperatureIntervals = new ArrayList<>();
        for (AirTerminalTemperatureIntervalEnum temperatureInterval : AirTerminalTemperatureIntervalEnum.values()) {
            BaseEnumVO temperatureIntervalVo = airTerminalConverter.toBaseEnumVo(temperatureInterval);

            AirTerminalTemperatureIntervalEnum.Detail detail = temperatureInterval.ofDetail("", tdbSeasonQuality);
            String limitTips = detail.getLimitTips();
            temperatureIntervalVo.setUserTips(temperatureInterval.getReasonPhrase() + limitTips);
            temperatureIntervalVo.addAdditionalMapValue("limitTips", limitTips);
            temperatureIntervals.add(temperatureIntervalVo);
        }
        List<BaseEnumVO> carbonDioxideIntervals = new ArrayList<>();
        List<AirTerminalCarbonDioxideIntervalEnum> carbonDioxideIntervalEnums = Lists.newArrayList(
                AirTerminalCarbonDioxideIntervalEnum.APPROPRIATE,
                AirTerminalCarbonDioxideIntervalEnum.TOO_HIGH
        );
        for (AirTerminalCarbonDioxideIntervalEnum carbonDioxideInterval : carbonDioxideIntervalEnums) {
            BaseEnumVO carbonDioxideIntervalVo = airTerminalConverter.toBaseEnumVo(carbonDioxideInterval);

            AirTerminalCarbonDioxideIntervalEnum.Detail detail = carbonDioxideInterval.ofDetail("", co2Quality);
            String limitTips = detail.getLimitTips();
            carbonDioxideIntervalVo.setUserTips(carbonDioxideInterval.getReasonPhrase() + limitTips);
            carbonDioxideIntervalVo.addAdditionalMapValue("limitTips", limitTips);
            carbonDioxideIntervals.add(carbonDioxideIntervalVo);
        }
        AirTerminalQualityIntervalVO result = new AirTerminalQualityIntervalVO();
        result.setTemperatureIntervals(temperatureIntervals);
        result.setCarbonDioxideIntervals(carbonDioxideIntervals);
        return result;
    }

    @Override
    public AirTerminalDemandStatusVO getDemandStatus(@NonNull AirTerminalDemandStatusQueryParam param) {
        String projectId = param.getProjectId();

        MidCalendarAcSeasonTypeEnum acSeasonType = midCalendarClientService
                .getAcSeasonType(projectId, projectId, DateUtils.now().toLocalDate());

        AirTerminalDemandStatusVO result = new AirTerminalDemandStatusVO();
        result.setCarbonDioxideDemandStatuses(airTerminalConverter.toBaseEnumVo(DIOXIDE_DEMAND_STATUS_ENUMS));
        List<BaseEnumVO> temperatureDemandStatuses = new ArrayList<>();
        if (Objects.nonNull(acSeasonType)) {
            switch (acSeasonType) {
                case SEASON_COOLING:
                    temperatureDemandStatuses = airTerminalConverter.toBaseEnumVo(
                            COOLING_TEMPERATURE_DEMAND_STATUS_ENUMS);
                    break;
                case SEASON_HEATING:
                    temperatureDemandStatuses = airTerminalConverter.toBaseEnumVo(
                            HEATING_CARBON_DIOXIDE_DEMAND_STATUS_ENUMS);
                    break;
                default:
            }
        }
        result.setTemperatureDemandStatuses(temperatureDemandStatuses);
        return result;
    }

    @Override
    public AirTerminalOutdoorWeatherVO getOutdoorWeather(@NonNull AirTerminalOutdoorWeatherQueryParam param) {
        String projectId = param.getProjectId();

        LocalDateTime startOfDay = DateUtils.now().toLocalDate().atStartOfDay();

        AirTerminalOutdoorWeatherVO result = new AirTerminalOutdoorWeatherVO();
        WeatherNowWeatherDTO nowWeather = weatherClientService.getNowWeather(projectId, WeatherDataTypeEnum.WEATHER);
        if (Objects.isNull(nowWeather)) {
            return result;
        }
        String text = nowWeather.getText();
        BigDecimal temperature = nowWeather.getTemperature();
        BigDecimal humidity = nowWeather.getHumidity();

        List<BigDecimal> todayAllTemperatureList = new ArrayList<>();
        if (temperature != null) {
            todayAllTemperatureList.add(temperature);
        }
        WeatherHour24ForecastDTO hour24Forecast = weatherClientService.getHour24Forecast(projectId);
        if (Objects.isNull(hour24Forecast)) {
            return result;
        }
        for (WeatherHourlyDTO hourly : hour24Forecast.getHourlies()) {
            LocalDateTime time = hourly.getTime();
            BigDecimal hourTemperature = hourly.getTemperature();

            if (!time.isBefore(startOfDay) && Objects.nonNull(hourTemperature)) {
                todayAllTemperatureList.add(hourTemperature);
            }
        }
        BigDecimal minimumTemperature = bizComputeService.calculateMinimum(todayAllTemperatureList);
        BigDecimal maximumTemperature = bizComputeService.calculateMaximum(todayAllTemperatureList);

        result.setText(text);
        if (Objects.nonNull(temperature)) {
            result.setTemperature(temperature.toPlainString());
        }
        if (Objects.nonNull(humidity)) {
            result.setHumidity(humidity.toPlainString());
        }
        if (Objects.nonNull(minimumTemperature)) {
            result.setMinTemperature(minimumTemperature.toPlainString());
        }
        if (Objects.nonNull(maximumTemperature)) {
            result.setMaxTemperature(maximumTemperature.toPlainString());
        }
        return result;
    }

    @Override
    public AirTerminalSpaceQualityStatisticsVO getSpaceQualityStatistics(
            @NonNull AirTerminalSpaceQualityStatisticsQueryParam param) {
        AirTerminalSpaceQualityStatisticsVO result = new AirTerminalSpaceQualityStatisticsVO();

        AirTerminalQualityStatisticsContext context = initQualityStatisticsContext(param);
        if (context.isFailed()) {
            return result;
        }
        DtpKnowledgeInfoCodeDTO tdbDefine = context.getTdbDefine();
        DtpKnowledgeInfoCodeDTO co2Define = context.getCo2Define();
        List<DtpRwdSpaceDTO> spaces = context.getSpaces();
        List<BaseEnumVO> temperatureDemandStatuses = context.getTemperatureDemandStatuses();
        List<BaseEnumVO> carbonDioxideDemandStatuses = context.getCarbonDioxideDemandStatuses();
        List<BigDecimal> temperatures = context.getTemperatures();
        List<BigDecimal> carbonDioxides = context.getCarbonDioxides();
        Bag<AirTerminalDemandStatusEnum> temperatureDemandStatusBag = context.getTemperatureDemandStatusBag();
        Bag<AirTerminalDemandStatusEnum> carbonDioxideDemandStatusBag = context.getCarbonDioxideDemandStatusBag();
        int totalSpaceCount = spaces.size();

        // 品质统计-温度
        AirTerminalQualityStatisticsVO qualityTemperatureStatistics = new AirTerminalQualityStatisticsVO();
        qualityTemperatureStatistics.setTypes(context.getTemperatureIntervals());
        qualityTemperatureStatistics.setTotalSpaceCount(totalSpaceCount);
        qualityTemperatureStatistics.setTotalCount(temperatures.size());
        calculateAndSetQualityStatistics(qualityTemperatureStatistics, tdbDefine.getScale(), temperatures);

        // 品质统计-二氧化碳
        AirTerminalQualityStatisticsVO qualityCarbonDioxideStatistics = new AirTerminalQualityStatisticsVO();
        qualityCarbonDioxideStatistics.setTypes(context.getCarbonDioxideIntervals());
        qualityCarbonDioxideStatistics.setTotalSpaceCount(totalSpaceCount);
        qualityCarbonDioxideStatistics.setTotalCount(carbonDioxides.size());
        calculateAndSetQualityStatistics(qualityCarbonDioxideStatistics, co2Define.getScale(), carbonDioxides);

        // 调度统计-温度
        AirTerminalSchedulingStatisticsVO schedulingTemperatureStatistics = new AirTerminalSchedulingStatisticsVO();
        schedulingTemperatureStatistics.setTypes(temperatureDemandStatuses);
        calculateAndSetSchedulingStatistics(schedulingTemperatureStatistics,
                temperatureDemandStatuses, temperatureDemandStatusBag);

        // 调度统计-二氧化碳
        AirTerminalSchedulingStatisticsVO schedulingCarbonDioxideStatistics = new AirTerminalSchedulingStatisticsVO();
        schedulingCarbonDioxideStatistics.setTypes(carbonDioxideDemandStatuses);
        calculateAndSetSchedulingStatistics(schedulingCarbonDioxideStatistics,
                carbonDioxideDemandStatuses, carbonDioxideDemandStatusBag);

        result.setQualityTemperatureStatistics(qualityTemperatureStatistics);
        result.setQualityCarbonDioxideStatistics(qualityCarbonDioxideStatistics);
        result.setSchedulingTemperatureStatistics(schedulingTemperatureStatistics);
        result.setSchedulingCarbonDioxideStatistics(schedulingCarbonDioxideStatistics);
        return result;
    }

    @Override
    public AirTerminalQualityStatisticsContext getSpaceQualityStatisticsContext(
            @NonNull AirTerminalSpaceQualityStatisticsQueryParam param) {
        return initQualityStatisticsContext(param);
    }

    @Override
    public Set<AirTerminalEquipmentTypeVO> listEquipmentTypes(@NonNull AirTerminalEquipmentTypeQueryParam param) {
        AirTerminalInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();

        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.fillByInstance(new DemandInstanceValueObject(instanceType.toDemandInstanceTypeEnum(), instanceId));
        // equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        List<DtpRwdEquipmentDTO> equipments = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        if (CollectionUtils.isEmpty(equipments)) {
            return Sets.newHashSet();
        }
        Set<String> equipmentIds = equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<DtpRwdSpaceDTO> spaces = queryServiceSpaces(null, equipmentIds, ID_ADN_RELOBJIDS);
        if (CollectionUtils.isEmpty(spaces)) {
            return Sets.newHashSet();
        }
        Map<String, Set<String>> equipmentIdAndSpaceIds = DtpRwdSpaceDTO.mapRelObjIdAndIds(spaces);
        Map<DtpRwdClassCodeEnum, Set<String>> classCodeAndRelSpaceIds = new HashMap<>(16);
        for (DtpRwdObjectDTO equipment : equipments) {
            String equipmentId = equipment.getId();
            String classCode = equipment.getClassCode();

            DtpRwdClassCodeEnum classCodeEnum = BaseEnum.analyze(classCode, DtpRwdClassCodeEnum.class, false);
            if (Objects.isNull(classCodeEnum)) {
                continue;
            }
            Set<String> relSpaceIds = equipmentIdAndSpaceIds.getOrDefault(equipmentId, Collections.emptySet());
            MapExecutor.dynamicPutAll(classCodeAndRelSpaceIds, classCodeEnum, relSpaceIds, HashSet::new);
        }
        Set<String> classCodes = classCodeAndRelSpaceIds.keySet().stream()
                .map(DtpRwdClassCodeEnum::value)
                .collect(Collectors.toSet());
        Map<String, String> classCodeAndName = dtpKnowledgeClientService.mapClassCodeAndName(
                false, false, classCodes);
        Set<AirTerminalEquipmentTypeVO> result = new HashSet<>();
        classCodeAndRelSpaceIds.forEach((classCode, relSpaceIds) -> {
            AirTerminalEquipmentTypeVO resultItem = new AirTerminalEquipmentTypeVO();
            resultItem.setClassCode(classCode.value());
            resultItem.setClassCodeName(classCodeAndName.get(classCode.value()));
            resultItem.setRelSpaceIds(relSpaceIds);
            result.add(resultItem);
        });
        return result;
    }

    @Override
    public AirTerminalSpaceWithStatusVO listSpaceWithStatus(@NonNull AirTerminalSpaceWithStatusQueryParam param) {
        String projectId = param.getProjectId();
        AirTerminalInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();

        AirTerminalSpaceWithStatusVO result = new AirTerminalSpaceWithStatusVO();
        StopWatch stopWatch = StopWatch.create("查询空间带状态");
        stopWatch.start("查询设备");
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        List<DtpRwdEquipmentDTO> equipments = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        stopWatch.stop();
        // 根据策略查询对应的空间 （空间有楼层信息）
        stopWatch.start("查询空间");
        RunningMonitorSpaceQueryParam spaceQueryParam = new RunningMonitorSpaceQueryParam();
        spaceQueryParam.fillByInstance(new DemandInstanceValueObject(instanceType.toDemandInstanceTypeEnum(), instanceId));
        List<DtpRwdSpaceDTO> spaces = runningMonitorAirTerminalStrategy.listSpaces(spaceQueryParam);
        stopWatch.stop();
        if (CollectionUtils.isEmpty(equipments) && CollectionUtils.isEmpty(spaces)) {
            return null;
        }
        List<AirTerminalSpaceGraphVO> spaceGraphs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(spaces)) {
            Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            Map<String, Set<String>> spaceIdAndRelEquipmentIds = new HashMap<>();
            stopWatch.start("查询空间和设备的关系");
            if (CollectionUtils.isNotEmpty(equipments)) {
                Set<String> equipmentIds = equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setId(spaceIds);
                queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
                queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjFrom(equipmentIds));
                dtpRwdClientService.listSpace(queryParam).stream()
                        .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds()))
                        .forEach(item -> MapExecutor.dynamicPutAll(spaceIdAndRelEquipmentIds,
                                item.getId(), item.getRelObjIds(), HashSet::new));
                queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjFrom(equipmentIds));
                dtpRwdClientService.listSpace(queryParam).stream()
                        .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds()))
                        .forEach(item -> MapExecutor.dynamicPutAll(spaceIdAndRelEquipmentIds,
                                item.getId(), item.getRelObjIds(), HashSet::new));
            }
            stopWatch.stop();
            stopWatch.start("查询空间标签");
            Map<String, AirTerminalSpaceTagDTO> spaceIdAndTag = mapSpaceIdAndTag(projectId, spaceIds);
            stopWatch.stop();
            stopWatch.start("查询空间实例化规则");
            Set<String> existsInstantiateObjectIds = mapSpaceIdAndExistsRuleInfoCodes(projectId, spaceIds).keySet();
            stopWatch.stop();
            for (DtpRwdSpaceDTO space : spaces) {
                String spaceId = space.getId();

                AirTerminalSpaceGraphVO spaceGraph = airTerminalConverter.toSpaceGraphVo(space);
                spaceGraph.setUnableMonitor(!existsInstantiateObjectIds.contains(spaceId));
                spaceGraph.setControlled(spaceIdAndRelEquipmentIds.containsKey(spaceId));
                spaceGraph.setControlledRelEquipmentIds(spaceIdAndRelEquipmentIds.get(spaceId));
                spaceGraph.setTag(spaceIdAndTag.get(spaceId));
                spaceGraphs.add(spaceGraph);
            }
        }
        result.setSpaces(spaceGraphs);
        List<AirTerminalEquipmentGraphVO> equipmentGraphs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(equipments)) {
            Set<String> equipmentIds = equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            stopWatch.start("查询末端设备标签");
            Map<String, AirTerminalEquipmentTagDTO> equipmentIdAndTag = mapEquipmentIdAndTag(projectId, equipmentIds);
            stopWatch.stop();
            for (DtpRwdEquipmentDTO equipment : equipments) {
                String equipmentId = equipment.getId();

                AirTerminalEquipmentGraphVO equipmentGraph = airTerminalConverter.toEquipmentGraphVo(equipment);
                equipmentGraph.setControlled(true);
                equipmentGraph.setTag(equipmentIdAndTag.get(equipmentId));
                equipmentGraphs.add(equipmentGraph);
            }
        }
        result.setEquipments(equipmentGraphs);
        logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return result;
    }

    @Override
    public AirTerminalSpaceTemperatureStatisticsVO listSpaceTemperatureStatistics(@NonNull AirTerminalSpaceWithStatusQueryParam param) {
        String projectId = param.getProjectId();
        AirTerminalInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();

        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        equipmentQueryParam.setFilterExcludeClassCodes(Sets.newHashSet(DtpRwdClassCodeEnum.ACVVEJ.value()));
        Set<String> controlEquipmentIds = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<DtpRwdSpaceDTO> spaces;
        if (CollectionUtils.isEmpty(controlEquipmentIds)) {
            spaces = listColdSpaces(instanceType, instanceId);
        } else {
            spaces = querySpaces(projectId, instanceType, instanceId);
        }
        AirTerminalSpaceTemperatureStatisticsVO result = new AirTerminalSpaceTemperatureStatisticsVO();
        if (CollectionUtils.isNotEmpty(spaces)) {
            Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            Map<String, AirTerminalSpaceTagDTO> spaceIdAndTag = mapSpaceIdAndTag(projectId, spaceIds);
            result.setAllSpaceCount(spaces.size());
            for (DtpRwdSpaceDTO space : spaces) {
                String spaceId = space.getId();
                AirTerminalSpaceTagDTO spaceTag = spaceIdAndTag.get(spaceId);
                AirTerminalTemperatureIntervalEnum temperatureInterval = spaceTag.getTemperatureInterval();
                if (Objects.isNull(temperatureInterval)) {
                    result.setNoDataCount(result.getNoDataCount() + 1);
                    continue;
                }
                String temperatureValue = temperatureInterval.getValue();
                if (temperatureValue.equals(AirTerminalTemperatureIntervalEnum.TOO_COLD.value())) {
                    result.setTooColdCount(result.getTooColdCount() + 1);
                } else if (temperatureValue.equals(AirTerminalTemperatureIntervalEnum.COLD.value())) {
                    result.setColdCount(result.getColdCount() + 1);
                } else if (temperatureValue.equals(AirTerminalTemperatureIntervalEnum.APPROPRIATE.value())) {
                    result.setAppropriateCount(result.getAppropriateCount() + 1);
                } else if (temperatureValue.equals(AirTerminalTemperatureIntervalEnum.HOT.value())) {
                    result.setHotCount(result.getHotCount() + 1);
                } else if (temperatureValue.equals(AirTerminalTemperatureIntervalEnum.TOO_HOT.value())) {
                    result.setTooHotCount(result.getTooHotCount() + 1);
                }
            }
        }
        return result;
    }

    @Override
    public @Nullable AirTerminalSpaceWithStatusVO listSpaceEquipment(
            @NonNull AirTerminalSpaceWithStatusQueryParam param) {
        boolean useWhitelistSpaceRange =
                this.airTerminalQuerySpace.isWhitelistSpaceRangeConfigured();
        this.logProjectInstance().info("listSpaceEquipment",
                Collections.singletonList(Pair.of("param", param)));
        if (this.cacheObjectModelStatusService.isReady(
                RunningMonitorTypeEnum.AIR_TERMINAL)) {
            return this.getSpaceEquipmentFromNewCache(
                    param, useWhitelistSpaceRange);
        }

        // READY 路由已经在本方法确定；后续固定走实时分支，避免状态在二次检查时变化后混读新旧数据。
        RunningMonitorEquipmentQueryParam equipmentQueryParam =
                new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.setWithColumns(
                new HashSet<>(AirTerminalServiceImpl.SPACE_EQUIPMENT_EQUIPMENT_FIELDS));
        RunningMonitorSpaceQueryParam spaceQueryParam =
                new RunningMonitorSpaceQueryParam();
        spaceQueryParam.fillByInstance(new DemandInstanceValueObject(
                param.getInstanceType().toDemandInstanceTypeEnum(),
                param.getInstanceId()));
        spaceQueryParam.setWithColumns(
                new HashSet<>(AirTerminalServiceImpl.SPACE_EQUIPMENT_SPACE_FIELDS));
        spaceQueryParam.setMeosCache(false);

        StopWatch stopWatch = StopWatch.create("查询空间带状态");
        AirTerminalSpaceWithStatusVO result =
                this.queryRawSpaceEquipmentFromRwd(
                        equipmentQueryParam, spaceQueryParam,
                        useWhitelistSpaceRange, stopWatch);
        if (result == null) {
            return null;
        }

        String projectId = param.getProjectId();
        List<DtpRwdSpaceDTO> spaces = result.getRwdSpaces();
        List<DtpRwdEquipmentDTO> equipments = result.getRwdEquipments();
        List<AirTerminalSpaceGraphVO> spaceGraphs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(spaces)) {
            Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            Map<String, Set<String>> spaceIdAndRelEquipmentIds = new HashMap<>();
            stopWatch.start("查询空间和设备的关系");
            if (CollectionUtils.isNotEmpty(equipments)) {
                Set<String> equipmentIds = equipments.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
                DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam().setId(spaceIds);
                queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
                queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE).setObjFrom(equipmentIds));
                this.dtpRwdClientService.listSpace(queryParam).stream()
                        .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds()))
                        .forEach(item -> MapExecutor.dynamicPutAll(spaceIdAndRelEquipmentIds,
                                item.getId(), item.getRelObjIds(), HashSet::new));
                queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE).setObjFrom(equipmentIds));
                this.dtpRwdClientService.listSpace(queryParam).stream()
                        .filter(item -> CollectionUtils.isNotEmpty(item.getRelObjIds()))
                        .forEach(item -> MapExecutor.dynamicPutAll(spaceIdAndRelEquipmentIds,
                                item.getId(), item.getRelObjIds(), HashSet::new));
            }
            stopWatch.stop();
            stopWatch.start("查询空间实例化规则");
            Map<String, Set<String>> spaceIdAndExistsRuleInfoCodes =
                    this.mapSpaceIdAndExistsRuleInfoCodes(projectId, spaceIds);
            stopWatch.stop();
            for (DtpRwdSpaceDTO space : spaces) {
                String spaceId = space.getId();

                AirTerminalSpaceGraphVO spaceGraph =
                        this.airTerminalConverter.toSpaceGraphVo(space);
                Set<String> existsRuleInfoCodes = spaceIdAndExistsRuleInfoCodes.getOrDefault(spaceId, Collections.emptySet());
                spaceGraph.setUnableMonitor(existsRuleInfoCodes.isEmpty());
                spaceGraph.setControlled(spaceIdAndRelEquipmentIds.containsKey(spaceId));
                spaceGraph.setControlledRelEquipmentIds(spaceIdAndRelEquipmentIds.get(spaceId));
                spaceGraphs.add(spaceGraph);
            }
        }
        result.setSpaces(spaceGraphs);
        List<AirTerminalEquipmentGraphVO> equipmentGraphs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(equipments)) {
            for (DtpRwdEquipmentDTO equipment : equipments) {
                AirTerminalEquipmentGraphVO equipmentGraph =
                        this.airTerminalConverter.toEquipmentGraphVo(equipment);
                equipmentGraph.setControlled(true);
                equipmentGraphs.add(equipmentGraph);
            }
        }
        result.setEquipments(equipmentGraphs);
        this.logProjectInstance(projectId).debug("响应时间记录",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return result;
    }

    @Override
    public @Nullable AirTerminalSpaceWithStatusVO
    listFullSpaceEquipmentForCacheBuild(
            @NonNull AirTerminalSpaceWithStatusQueryParam param) {
        this.logProjectInstance().debug("listFullSpaceEquipmentForCacheBuild",
                Collections.singletonList(Pair.of("param", param)));

        // 末端设备查询复用同一参数，先合并目标设备类字段，避免每个 Key 再按 ID 补查。
        Set<String> equipmentFields = new LinkedHashSet<>();
        for (DtpRwdClassCodeEnum classCode : AirTerminalService.ALL_EQUIPMENT_CLASS_CODES) {
            equipmentFields.addAll(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                    classCode.value()));
        }
        RunningMonitorEquipmentQueryParam equipmentQueryParam =
                new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(equipmentFields.toArray(new String[0]));
        RunningMonitorSpaceQueryParam spaceQueryParam =
                new RunningMonitorSpaceQueryParam();
        spaceQueryParam.addWithColumn(CacheObjectExtensionFieldConfig.listRwdQueryFields(
                DtpRwdClassCodeEnum.SPACE.value()).toArray(new String[0]));
        spaceQueryParam.fillByInstance(new DemandInstanceValueObject(
                param.getInstanceType().toDemandInstanceTypeEnum(),
                param.getInstanceId()));
        // 全量重建必须直查 RWD，不能反读上一版新版缓存。
        spaceQueryParam.setMeosCache(false);

        return this.queryRawSpaceEquipmentFromRwd(
                equipmentQueryParam, spaceQueryParam, false,
                StopWatch.create("查询空间带状态"));
    }

    /**
     * 按调用方准备好的字段和缓存策略查询原始末端空间、设备数据。
     *
     * <p>该方法只负责查询并写入 {@code rwdSpaces}/{@code rwdEquipments}，不组装业务 Graph。
     * 空间范围由明确的领域参数决定：业务查询可使用新版白名单 Key，缓存构建固定传
     * {@code false} 查询项目全量空间。</p>
     *
     * @param equipmentQueryParam    已由调用入口配置完整的设备查询参数
     * @param spaceQueryParam        已由调用入口配置完整的空间查询参数
     * @param useWhitelistSpaceRange 是否使用新版白名单 Key 限定空间范围
     * @param stopWatch               本次查询的耗时记录器
     * @return 空间设备查询结果；空间和设备均为空时返回 {@code null}
     * @since v7.260701.0
     */
    private @Nullable AirTerminalSpaceWithStatusVO queryRawSpaceEquipmentFromRwd(
            @NonNull RunningMonitorEquipmentQueryParam equipmentQueryParam,
            @NonNull RunningMonitorSpaceQueryParam spaceQueryParam,
            boolean useWhitelistSpaceRange,
            @NonNull StopWatch stopWatch) {
        stopWatch.start("查询设备");
        List<DtpRwdEquipmentDTO> equipments =
                this.runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        stopWatch.stop();
        // 根据策略查询对应的空间 （空间有楼层信息）
        stopWatch.start("查询空间");
        List<DtpRwdSpaceDTO> spaces;
        if (useWhitelistSpaceRange) {
            spaces = this.runningMonitorAirTerminalStrategy.listWhitelistSpaces(spaceQueryParam);
        } else {
            // 复用本请求已查询的完整设备范围，避免设备服务空间分支再次拉取全部末端设备。
            Set<String> equipmentIds = CollectionUtils.isEmpty(equipments)
                    ? Collections.emptySet()
                    : equipments.stream()
                            .map(DtpRwdObjectDTO::getId)
                            .collect(Collectors.toSet());
            spaces = this.runningMonitorAirTerminalStrategy.listSpaces(
                    spaceQueryParam, equipmentIds);
        }
        stopWatch.stop();
        if (CollectionUtils.isEmpty(equipments) && CollectionUtils.isEmpty(spaces)) {
            return null;
        }
        AirTerminalSpaceWithStatusVO result = new AirTerminalSpaceWithStatusVO();
        result.setRwdSpaces(spaces);
        result.setRwdEquipments(equipments);
        return result;
    }

    /**
     * 从新对象主表和关系表恢复空间设备图结果。
     *
     * <p>READY 后不再请求旧对象表或 RWD。对象和关系均使用新缓存实际查询到的结果，
     * 不在消费端重复校验写入阶段已经保证的数据完整性。</p>
     *
     * @param param 查询参数
     * @param useWhitelistSpaceRange 系统壳配置是否选择白名单空间范围
     * @return 新缓存模型中的空间设备结果
     * @since v7.260701.0
     */
    private @Nullable AirTerminalSpaceWithStatusVO
    getSpaceEquipmentFromNewCache(
            @NonNull AirTerminalSpaceWithStatusQueryParam param,
            boolean useWhitelistSpaceRange) {
        String whitelistKey = CacheObjectModelKeyConfig.AIR_TERMINAL_WHITELIST_SPACE;
        String projectSpaceKey = CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_SPACE;
        String equipmentIndexKey = CacheObjectModelKeyConfig.AIR_TERMINAL_LIST_EQUIPMENT;
        // 壳配置决定空间范围；白名单 Key 空关联是合法空结果，不能回退项目全量 Key。
        String spaceIndexKey = useWhitelistSpaceRange
                ? whitelistKey : projectSpaceKey;
        Map<String, List<String>> objectIdsByCacheKey = this.cacheObjectRepository
                .mapObjectIdsByCacheKeys(Arrays.asList(
                        spaceIndexKey, equipmentIndexKey));
        List<DtpRwdSpaceDTO> spaces = this.cacheObjectRepository.listByObjectIds(
                objectIdsByCacheKey.getOrDefault(spaceIndexKey, Collections.emptyList()),
                DtpRwdSpaceDTO.class,
                AirTerminalServiceImpl.SPACE_EQUIPMENT_SPACE_FIELDS);
        spaces = this.filterNewCacheSpacesByInstance(spaces, param);
        // 白名单只替换空间范围；设备仍读取项目级缓存键，以保持实时计算时“白名单空间 +
        // 项目设备”的结果结构。
        List<DtpRwdEquipmentDTO> equipments = this.cacheObjectRepository.listByObjectIds(
                objectIdsByCacheKey.getOrDefault(
                        equipmentIndexKey, Collections.emptyList()),
                DtpRwdEquipmentDTO.class,
                AirTerminalServiceImpl.SPACE_EQUIPMENT_EQUIPMENT_FIELDS);

        // 保持历史接口约定：空间和设备都未命中时返回 null，而不是返回两个空集合的 VO。
        if (CollectionUtils.isEmpty(spaces) && CollectionUtils.isEmpty(equipments)) {
            return null;
        }

        Set<String> spaceIds = spaces.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> equipmentIds = equipments.stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> acahgIds = equipments.stream()
                .filter(item -> StringUtils.equals(
                        DtpRwdClassCodeEnum.ACAHG_GROUP.value(), item.getClassCode()))
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Set<String>> spaceIdAndRelEquipmentIds =
                this.mapSpaceEquipmentRelations(equipmentIds);
        Map<String, Set<String>> groupIdAndEquipmentIds =
                this.mapAirHandlingGroupRelations(acahgIds);
        // 规则标记保持实时查询语义，只按空间、tdb/co2 和当前部署端规则类型查询，
        // 不通过模板 Key 关系附加业务场景范围。
        Set<String> spaceIdsWithRules =
                CollectionUtils.isEmpty(spaceIds)
                        ? Collections.emptySet()
                        : this.cacheRuleRepository.mapInfoCodesByObjectIds(
                                spaceIds,
                                Arrays.asList(
                                        DtpRwdSpaceDTO.Fields.tdb,
                                        DtpRwdSpaceDTO.Fields.co2),
                                this.cacheIotRuleRepository.cacheIotRuleType())
                                .keySet();

        List<AirTerminalSpaceGraphVO> spaceGraphs = new ArrayList<>(spaces.size());
        for (DtpRwdSpaceDTO space : spaces) {
            AirTerminalSpaceGraphVO spaceGraph = this.airTerminalConverter.toSpaceGraphVo(space);
            spaceGraph.setUnableMonitor(!spaceIdsWithRules.contains(space.getId()));
            spaceGraph.setControlled(spaceIdAndRelEquipmentIds.containsKey(space.getId()));
            spaceGraph.setControlledRelEquipmentIds(
                    spaceIdAndRelEquipmentIds.get(space.getId()));
            spaceGraphs.add(spaceGraph);
        }
        List<AirTerminalEquipmentGraphVO> equipmentGraphs = new ArrayList<>(equipments.size());
        for (DtpRwdEquipmentDTO equipment : equipments) {
            AirTerminalEquipmentGraphVO equipmentGraph =
                    this.airTerminalConverter.toEquipmentGraphVo(equipment);
            equipmentGraph.setControlled(true);
            if (StringUtils.equals(
                    DtpRwdClassCodeEnum.ACAHG_GROUP.value(), equipment.getClassCode())) {
                equipmentGraph.setControlledAirHandlingUnitIds(
                        groupIdAndEquipmentIds.getOrDefault(
                                equipment.getId(), Collections.emptySet()));
            }
            equipmentGraphs.add(equipmentGraph);
        }
        AirTerminalSpaceWithStatusVO result = new AirTerminalSpaceWithStatusVO();
        result.setSpaces(spaceGraphs);
        result.setEquipments(equipmentGraphs);
        return result;
    }

    /**
     * 按请求实例过滤项目级新缓存空间。
     *
     * <p>构建任务只生成项目级缓存键，建筑和楼层请求必须在内存中使用独立列过滤；白名单键
     * 读取出的空间同样按当前实例过滤，设备集合继续保持旧接口的项目全量语义。</p>
     *
     * @param spaces 项目级空间集合
     * @param param  原始查询参数
     * @return 当前实例范围的空间集合
     * @since v7.260701.0
     */
    private @NonNull List<DtpRwdSpaceDTO>
    filterNewCacheSpacesByInstance(
            @NonNull List<DtpRwdSpaceDTO> spaces,
            @NonNull AirTerminalSpaceWithStatusQueryParam param) {
        if (CollectionUtils.isEmpty(spaces)
                || param.getInstanceType() == AirTerminalInstanceTypeEnum.PROJECT) {
            return spaces;
        }
        String instanceId = param.getInstanceId();
        if (param.getInstanceType() == AirTerminalInstanceTypeEnum.BUILDING) {
            return spaces.stream()
                    .filter(space -> StringUtils.equals(instanceId, space.getBuildingId()))
                    .collect(Collectors.toList());
        }
        return spaces.stream()
                .filter(space -> StringUtils.equals(instanceId, space.getFloorId()))
                .collect(Collectors.toList());
    }

    /**
     * 使用新关系表映射空间关联的末端设备。
     *
     * @param equipmentIds 当前设备 ID
     * @return 空间 ID 到设备 ID 集合
     * @since v7.260701.0
     */
    private @NonNull Map<String, Set<String>> mapSpaceEquipmentRelations(
            @NonNull Set<String> equipmentIds) {
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return Collections.emptyMap();
        }
        DtpRwdRelationTypeEnum serviceRelation =
                DtpRwdRelationTypeEnum.EQUIPMENT_SERVICE_SPACE;
        DtpRwdRelationTypeEnum inRelation = DtpRwdRelationTypeEnum.EQUIPMENT_IN_SPACE;
        CacheObjectRelQueryDaoParam relationParam = new CacheObjectRelQueryDaoParam();
        relationParam.setObjectIds(equipmentIds);
        relationParam.addColumns(CacheObjectRel::getObjectId, CacheObjectRel::getRelObjectId);
        Map<String, Set<String>> result = new LinkedHashMap<>();
        // 两类 RWD 关系均以设备为主体、空间为目标；接口需要按空间聚合设备，
        // 因此这里有意用 relObjectId 作为 Map Key，不能按查询方向交换实体字段。
        for (DtpRwdRelationTypeEnum relationType : Arrays.asList(serviceRelation, inRelation)) {
            relationParam.setGraphCode(relationType.value().getLeft());
            relationParam.setRelCode(relationType.value().getRight());
            for (CacheObjectRel relation : this.cacheObjectRelRepository.listForward(relationParam)) {
                MapExecutor.dynamicPut(result, relation.getRelObjectId(),
                        relation.getObjectId(), LinkedHashSet::new);
            }
        }
        return result;
    }

    /**
     * 使用新关系表映射空调机组控制组关联的空调机组。
     *
     * @param acahgIds 当前项目末端空调机组控制组 ID
     * @return 控制组 ID 到空调机组 ID 集合
     * @since v7.260701.0
     */
    private @NonNull Map<String, Set<String>>
    mapAirHandlingGroupRelations(@NonNull Set<String> acahgIds) {
        if (CollectionUtils.isEmpty(acahgIds)) {
            return Collections.emptyMap();
        }
        DtpRwdRelationTypeEnum relationType = DtpRwdRelationTypeEnum.EQUIP_IN_GROUP;
        CacheObjectRelQueryDaoParam relationParam = new CacheObjectRelQueryDaoParam();
        relationParam.setObjectIds(acahgIds);
        relationParam.setGraphCode(relationType.value().getLeft());
        relationParam.setRelCode(relationType.value().getRight());
        relationParam.addColumns(CacheObjectRel::getObjectId, CacheObjectRel::getRelObjectId);
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (CacheObjectRel relation : this.cacheObjectRelRepository.listForward(relationParam)) {
            MapExecutor.dynamicPut(result, relation.getObjectId(),
                    relation.getRelObjectId(), LinkedHashSet::new);
        }
        return result;
    }

    /**
     * 查询冷站空间列表
     *
     * @return {@link List }<{@link DtpRwdSpaceDTO }>
     */
    private List<DtpRwdSpaceDTO> listColdSpaces(AirTerminalInstanceTypeEnum instanceType, String instanceId) {
        Set<String> spaceIds = null;
        if (instanceType != AirTerminalInstanceTypeEnum.PROJECT) {
            spaceIds = instanceService.listSpaceIdsByInstance(instanceType.toDemandInstanceTypeEnum(), instanceId);
        }
        // 查询冷源系统
        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                .setClassCode(DtpRwdClassCodeEnum.COLD_SYSTEM.value()));
        List<String> coldSystemIds = dtpRwdObjectDTOS.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
        DtpRwdObjectRelationQueryParam relationQueryParam = new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE);
        relationQueryParam.setObjFrom(coldSystemIds);
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setRelationTo(relationQueryParam);
        param.addWithColumn(DtpRwdObjectDTO.Fields.id);
        if (CollectionUtils.isNotEmpty(spaceIds)) {
            param.setId(spaceIds);
        }
        return dtpRwdClientService.listSpace(param);
    }

    @Override
    public List<DtpRwdObjectDTO> listPipelineByFloor(@NonNull AirTerminalPipelineByFloorQueryParam param) {
        String floorId = param.getFloorId();
        return dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                .setClassCodeEnum(ALL_PIPELINE_CLASS_CODES).setRelationFrom(new DtpRwdObjectRelationQueryParam(
                        DtpRwdRelationTypeEnum.EQUIPMENT_IN_FLOOR).setObjTo(floorId)));
    }

    @Override
    public List<AirTerminalFloorEquipmentTypeStatisticsVO> listEquipmentStatusStatistics(
            @NonNull AirTerminalEquipmentStatusStatisticsQueryParam param) {
        AirTerminalEquipmentStatusStatisticsContext context = initEquipmentStatusStatisticsContext(param);
        if (context.isFailed()) {
            return Lists.newArrayList();
        }

        List<AirTerminalFloorEquipmentTypeStatisticsVO> result = new ArrayList<>();
        List<DtpRwdBuildingDTO> buildings = context.getBuildings();
        Map<String, TreeSet<DtpRwdFloorDTO>> buildingIdAndFloors = context.getBuildingIdAndFloors();
        for (DtpRwdBuildingDTO building : buildings) {
            String buildingId = building.getId();

            List<AirTerminalFloorEquipmentTypeStatisticsVO> floorVos = new ArrayList<>();
            Set<DtpRwdFloorDTO> floors = buildingIdAndFloors.get(buildingId);
            if (CollectionUtils.isNotEmpty(floors)) {
                for (DtpRwdFloorDTO floor : floors) {
                    String floorId = floor.getId();

                    Map<String, AirTerminalEquipmentTypeStatisticsVO> equipmentGroupStatistics =
                            context.calculateEquipmentGroupStatistics(floorId);
                    if (MapUtils.isNotEmpty(equipmentGroupStatistics)) {
                        AirTerminalEquipmentTypeStatisticsVO equipmentTypeStatistics =
                                context.calculateEquipmentStatistics(floorId, equipmentGroupStatistics);

                        AirTerminalFloorEquipmentTypeStatisticsVO floorVo =
                                airTerminalConverter.toFloorEquipmentTypeStatisticsVo(floor);
                        floorVo.setEquipmentStatistics(equipmentTypeStatistics);
                        floorVo.setEquipmentGroupStatistics(new ArrayList<>(equipmentGroupStatistics.values()));
                        floorVos.add(floorVo);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(floorVos)) {
                AirTerminalFloorEquipmentTypeStatisticsVO buildingVo =
                        airTerminalConverter.toFloorEquipmentTypeStatisticsVo(building);
                buildingVo.setRelFloors(floorVos);
                result.add(buildingVo);
            }
        }
        return result;
    }

    @Override
    public boolean statisticDashboardEquipmentVisible() {
        return CollectionUtils.isNotEmpty(this.queryDashboardEquipmentTemplates());
    }

    @Override
    public List<DashboardEquipmentVO> statisticDashboardEquipment() {
        List<InfoCodeTemplateTypeModel> templateTypes = this.queryDashboardEquipmentTemplates();
        if (CollectionUtils.isEmpty(templateTypes)) {
            return Collections.emptyList();
        }
        infoCodeGateway.fillInfoCodeTemplateTypeModel(templateTypes);
        Set<String> objectIds = new HashSet<>();
        Map<String, Long> objectIdAndTemplateTypeId = new HashMap<>();
        for (InfoCodeTemplateTypeModel templateType : templateTypes) {
            Long templateTypeId = templateType.getTemplateTypeId();
            List<DtpRwdObjectDTO> objects = templateType.getObjects();
            for (DtpRwdObjectDTO object : objects) {
                objectIds.add(object.getId());
                objectIdAndTemplateTypeId.put(object.getId(), templateTypeId);
            }
        }
        List<DtpRwdEquipmentDTO> objects = dtpRwdClientService.listEquipment(new DtpRwdObjectQueryParam()
                .setId(objectIds).addWithColumn(DtpRwdObjectDTO.Fields.id,
                        DtpRwdEquipmentDTO.Fields.floorId,
                        DtpRwdEquipmentDTO.Fields.buildingId));
        Set<String> buildingIds = new HashSet<>();
        Set<String> floorIds = new HashSet<>();
        Map<String, Set<String>> floorIdAndObjectIds = new HashMap<>();
        for (DtpRwdEquipmentDTO object : objects) {
            String objectId = object.getId();
            String floorId = object.getFloorId();
            String buildingId = object.getBuildingId();
            buildingIds.add(buildingId);
            floorIds.add(floorId);
            MapExecutor.dynamicPut(floorIdAndObjectIds, floorId, objectId, HashSet::new);
        }
        List<DtpRwdBuildingDTO> rwdBuildings = new ArrayList<>();
        List<DtpRwdFloorDTO> rwdFloors = new ArrayList<>();
        Set<String> runningObjectIds = new HashSet<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(() -> rwdBuildings.addAll(dtpRwdClientService.listBuilding(
                new DtpRwdObjectQueryParam().setId(buildingIds).addWithColumn(DtpRwdObjectDTO.Fields.id,
                        DtpRwdObjectDTO.Fields.localName))), BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> rwdFloors.addAll(dtpRwdClientService.listFloor(
                new DtpRwdObjectQueryParam().setId(floorIds).addWithColumn(DtpRwdObjectDTO.Fields.id,
                        DtpRwdObjectDTO.Fields.localName, DtpRwdFloorDTO.Fields.buildingId,
                        DtpRwdFloorDTO.Fields.floorSequenceId))), BusinessThreadPoolExecutor.ttlExecutor()));
        futures.add(CompletableFuture.runAsync(() -> {
            // noinspection unchecked
            List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIds,
                    Lists.newArrayList(InfoCodeEnum.RUN_STATUS.getCode()));
            for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
                String infoCode = infoCodeValue.getInfoCode();
                String objectId = infoCodeValue.getObjectId();
                if (!InfoCodeEnum.RUN_STATUS.getCode().equals(infoCode)) {
                    continue;
                }
                if (infoCodeValue.equalsInfoCodeValue(DtpKnowledgeRunStatusEnum.RUNNING.getValue())) {
                    runningObjectIds.add(objectId);
                }
            }
        }, BusinessThreadPoolExecutor.ttlExecutor()));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Map<String, List<DtpRwdFloorDTO>> buildingIdAndFloors = DtpRwdFloorDTO.mapBuildingIdAndFloors(rwdFloors);
        List<DashboardEquipmentVO> buildings = new ArrayList<>();
        for (DtpRwdBuildingDTO rwdBuilding : rwdBuildings) {
            String buildingId = rwdBuilding.getId();

            List<DtpRwdFloorDTO> floorsInBuilding = buildingIdAndFloors.get(buildingId);
            if (CollectionUtils.isEmpty(floorsInBuilding)) {
                continue;
            }
            List<DashboardEquipmentVO.Floor> floors = new ArrayList<>();
            for (DtpRwdFloorDTO rwdFloor : floorsInBuilding) {
                String floorId = rwdFloor.getId();

                Set<String> objectIdsInFloor = floorIdAndObjectIds.get(floorId);
                if (CollectionUtils.isEmpty(objectIdsInFloor)) {
                    continue;
                }
                Map<Long, Integer> templateTypeIdAndCount = new HashMap<>();
                Map<Long, Integer> templateTypeIdAndTotalCount = new HashMap<>();
                for (String objectId : objectIdsInFloor) {
                    Long templateTypeId = objectIdAndTemplateTypeId.get(objectId);
                    if (Objects.isNull(templateTypeId)) {
                        continue;
                    }
                    MapExecutor.dynamicIncrement(templateTypeIdAndTotalCount, templateTypeId);
                    if (runningObjectIds.contains(objectId)) {
                        MapExecutor.dynamicIncrement(templateTypeIdAndCount, templateTypeId);
                    }
                }
                List<DashboardEquipmentVO.Statistic> statistics = new ArrayList<>();
                for (InfoCodeTemplateTypeModel templateType : templateTypes) {
                    Long templateTypeId = templateType.getTemplateTypeId();
                    String templateTypeName = templateType.getTemplateTypeName();
                    Integer sequence = templateType.getSequence();

                    if (!templateTypeIdAndTotalCount.containsKey(templateTypeId)) {
                        continue;
                    }
                    DashboardEquipmentVO.Statistic statistic = new DashboardEquipmentVO.Statistic();
                    statistic.setTemplateName(templateTypeName);
                    statistic.setTemplateSequence(sequence);
                    statistic.setTotalCount(templateTypeIdAndTotalCount.get(templateTypeId));
                    statistic.setOpenCount(templateTypeIdAndCount.get(templateTypeId));
                    statistics.add(statistic);
                }
                DashboardEquipmentVO.Floor floor = new DashboardEquipmentVO.Floor();
                floor.setFloor(rwdFloor);
                floor.setStatistics(statistics);
                floors.add(floor);
            }
            DashboardEquipmentVO building = new DashboardEquipmentVO();
            building.setBuilding(rwdBuilding);
            building.setFloors(floors);
            buildings.add(building);
        }
        return buildings;
    }

    @Override
    public Object listEquipmentControllerDetails(SpaceInfoQueryParam param) {
        Map<String, JSONObject> stringObjectMap = abstractRunningMonitorServer
                .queryInfoCodeConfigByObjectId(ContextUtil.getGroupCode(), param.getProjectId()
                        , DtpRwdSystemClassCodeEnum.ACAT.value()
                        , param.getEquipmentId(), true);

        if (stringObjectMap != null) {
            return stringObjectMap.get(param.getEquipmentId());
        }
        return new JSONObject();
    }

    @Override
    public TagInfoVO listEquipmentTag(SpaceInfoQueryParam param) {
        List<ObjectClassCodeDTO> iotDataRequestVos = new ArrayList<>();
        ObjectClassCodeDTO objectClassCodeDTO = new ObjectClassCodeDTO();
        objectClassCodeDTO.setClassCode(DtpRwdClassCodeEnum.ACATAH.getValue());
        objectClassCodeDTO.setObjectId(param.getEquipmentId());
        iotDataRequestVos.add(objectClassCodeDTO);
        Map<String, TagInfoVO> tagInfoVoMap = abstractCommonServer.mapEquipmentIdAndTag(param.getProjectId(), iotDataRequestVos, null);
        if (tagInfoVoMap != null) {
            return tagInfoVoMap.get(param.getEquipmentId());
        }
        return new TagInfoVO();
    }

    @Override
    public List<ExceptionEventBaseInfoVO> getAbnormalDataList(AirTerminalAbnormalQueryParam instanceQueryParam) {
        Set<String> objectIdList = new HashSet<>();
        // 如果对象类为设备无需查空间
        InstanceTypeEnum instanceType = instanceQueryParam.getInstanceType();
        if (instanceType != null && !instanceType.value().equals(InstanceTypeEnum.EQUIPMENT.value())) {
            // 查询空间数据（查项目、建筑、楼层、空间下所有空间）
            Set<String> spaceIds = new HashSet<>();
            // 空调末端有过滤
            RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
            equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            Set<String> controlEquipmentIds = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam).stream()
                    .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            List<DtpRwdSpaceDTO> spaces;

            // 如果末端可控设备为空，则查询冷站的空间
            if (CollectionUtil.isEmpty(controlEquipmentIds)) {
                AirTerminalInstanceTypeEnum analyze = BaseEnum.analyze(instanceType.getValue(), AirTerminalInstanceTypeEnum.class);
                spaces = listColdSpaces(analyze, instanceQueryParam.getInstanceId());
            } else {
                spaces = querySpaces(instanceQueryParam.getProjectId(), AirTerminalInstanceTypeEnum.analyze(instanceType.getValue()),
                        instanceQueryParam.getInstanceId());
            }
            if (CollectionUtils.isEmpty(spaces)) {
                return null;
            }
            for (DtpRwdSpaceDTO space : spaces) {
                spaceIds.add(space.getId());
            }
            if (CollectionUtil.isEmpty(controlEquipmentIds)) {
                objectIdList.addAll(spaceIds);
            } else {
                DtpRwdObjectQueryParam dtpRwdObjectQueryParam = new DtpRwdObjectQueryParam();
                dtpRwdObjectQueryParam.setId(controlEquipmentIds);
                dtpRwdObjectQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
                List<DtpRwdEquipmentDTO> dtpRwdObjectList = dtpRwdClientService.listEquipmentInBySpaceIds(
                        spaceIds, dtpRwdObjectQueryParam);
                dtpRwdObjectList.addAll(dtpRwdClientService.listEquipmentServiceBySpaceIds(
                        spaceIds, dtpRwdObjectQueryParam));
                objectIdList.addAll(dtpRwdObjectList.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList()));
                // 空调末端查询空间+设备异常事件
                objectIdList.addAll(spaceIds);
            }
        } else {
            objectIdList.add(instanceQueryParam.getInstanceId());
        }
        if (CollectionUtil.isEmpty(objectIdList)) {
            return null;
        }
        return abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(objectIdList, null);
    }

    @Override
    public Map<String, AirTerminalSpaceTagDTO> mapSpaceIdAndTag(
            @NonNull String projectId, @Nullable Collection<String> filterSpaceIds) {
        Set<String> spaceIds;
        if (CollectionUtils.isEmpty(filterSpaceIds)) {
            List<DtpRwdSpaceDTO> spaces = querySpaces(projectId, filterSpaceIds, null);
            spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        } else {
            spaceIds = new HashSet<>(filterSpaceIds);
        }
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Maps.newHashMap();
        }
        // 空调工况
        AtomicReference<MidCalendarAirSeasonTypeEnum> airSeasonType = new AtomicReference<>();
        // 项目需求配置
        AtomicReference<DemandConfigAirconditioningValueObject> projectDemandConfig = new AtomicReference<>();
        // 空间温度IOT值
        Map<String, InfoCodeValueVO> spaceIdAndTemperature = new HashMap<>(Math.max(spaceIds.size(), 16));
        // 空间二氧化碳IOT值
        Map<String, InfoCodeValueVO> spaceIdAndCarbonDioxide = new HashMap<>(Math.max(spaceIds.size(), 16));
        // 空间需求目标
        Map<String, Map<String, AirTerminalDemandStatusEnum>> spaceIdAndDemandStatusBy60Minute = new HashMap<>(Math.max(spaceIds.size(), 16));
        // 空间异常事件
        Map<String, List<ExceptionEventBaseInfoVO>> spaceIdAndRiskEvents = new HashMap<>(Math.max(spaceIds.size(), 16));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        futures.add(CompletableFuture.runAsync(() -> airSeasonType.set(midCalendarClientService.getAirSeasonType(projectId, DateUtils.now().toLocalDate())), executor));
        futures.add(CompletableFuture.runAsync(() -> projectDemandConfig.set(queryDemandProjectGlobalConfig()), executor));
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndTemperature.putAll(mapSpaceIdAndTemperature(spaceIds)), executor));
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndCarbonDioxide.putAll(mapSpaceIdAndCarbonDioxide(spaceIds)), executor));
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndDemandStatusBy60Minute.putAll(mapSpaceIdAndDemandStatusBy60Minute(spaceIds)), executor));
        futures.add(CompletableFuture.runAsync(() -> spaceIdAndRiskEvents.putAll(mapObjectIdAndRiskEventList(spaceIds, runningMonitorAirTerminalStrategy.exceptionFilterRiskMeasureAimLevel2())), executor));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 计算空间标签
        Map<String, AirTerminalDemandStatusEnum> spaceIdAndTemperatureDemandStatus =
                spaceIdAndDemandStatusBy60Minute.get(DtpRwdSpaceDTO.Fields.tdb);
        Map<String, AirTerminalDemandStatusEnum> spaceIdAndCarbonDioxideDemandStatus =
                spaceIdAndDemandStatusBy60Minute.get(DtpRwdSpaceDTO.Fields.co2);
        DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality = null;
        DemandConfigAirconditioningCO2QualityValueObject co2Quality = null;
        if (Objects.nonNull(projectDemandConfig.get())) {
            DemandConfigAirconditioningEnvironmentQualityObject environmentQuality =
                    projectDemandConfig.get().getEnvironmentQuality();
            if (Objects.nonNull(environmentQuality)) {
                DemandConfigAirconditioningTdbQualityValueObject tdbQuality = environmentQuality.getTdbQuality();
                if (Objects.nonNull(airSeasonType.get()) && Objects.nonNull(tdbQuality)) {
                    tdbSeasonQuality = tdbQuality.getCurrentSeasonQuality(airSeasonType.get());
                }
                co2Quality = environmentQuality.getCO2Quality();
            }
        }
        Map<String, AirTerminalSpaceTagDTO> result = new LinkedHashMap<>(Math.max(spaceIds.size(), 16));
        for (String spaceId : spaceIds) {
            AirTerminalTemperatureIntervalEnum temperatureInterval = null;
            InfoCodeValueVO temperature = spaceIdAndTemperature.get(spaceId);
            if (Objects.nonNull(temperature) && Objects.nonNull(temperature.getInfoCodeValue())) {
                BigDecimal value = new BigDecimal(temperature.getInfoCodeValue().toString());
                temperatureInterval = calculateTemperatureInterval(value, tdbSeasonQuality);
            } else {
                logProjectInstance().trace("空间无温度值", Arrays.asList(
                        Pair.of("spaceId", spaceId),
                        Pair.of("temperature", temperature)
                ));
            }

            AirTerminalCarbonDioxideIntervalEnum carbonDioxideInterval = null;
            InfoCodeValueVO carbonDioxide = spaceIdAndCarbonDioxide.get(spaceId);
            if (Objects.nonNull(carbonDioxide) && Objects.nonNull(carbonDioxide.getInfoCodeValue())) {
                BigDecimal value = new BigDecimal(carbonDioxide.getInfoCodeValue().toString());
                carbonDioxideInterval = calculateCarbonDioxideInterval(value, co2Quality);
            } else {
                logProjectInstance().trace("空间无二氧化碳浓度值", Arrays.asList(
                        Pair.of("spaceId", spaceId),
                        Pair.of("temperature", temperature)
                ));
            }

            AirTerminalDemandStatusEnum temperatureDemandStatus = spaceIdAndTemperatureDemandStatus.get(spaceId);
            AirTerminalDemandStatusEnum carbonDioxideDemandStatus = spaceIdAndCarbonDioxideDemandStatus.get(spaceId);

            AirTerminalSpaceTagDTO spaceTag = new AirTerminalSpaceTagDTO();
            spaceTag.setAbnormal(spaceIdAndRiskEvents.containsKey(spaceId));
            spaceTag.setTemperatureInterval(temperatureInterval);
            spaceTag.setCarbonDioxideInterval(carbonDioxideInterval);
            spaceTag.setTemperatureDemandStatus(temperatureDemandStatus);
            spaceTag.setCarbonDioxideDemandStatus(carbonDioxideDemandStatus);
            result.put(spaceId, spaceTag);
        }
        return result;
    }

    @Override
    public Map<String, AirTerminalEquipmentTagDTO> mapEquipmentIdAndTag(
            @NonNull String projectId, @Nullable Set<String> filterEquipmentIds) {
        StopWatch stopWatch = StopWatch.create("查询设备标签");
        Set<String> equipmentIds;
        if (CollectionUtils.isEmpty(filterEquipmentIds)) {
            stopWatch.start("查询设备");
            RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
            equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
            equipmentIds = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam).stream()
                    .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            stopWatch.stop();
        } else {
            equipmentIds = new HashSet<>(filterEquipmentIds);
        }
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return Maps.newHashMap();
        }
        // 查询异常事件
        stopWatch.start("查询设备异常事件");
        Map<String, List<ExceptionEventBaseInfoVO>> equipmentIdAndRiskEvents = mapObjectIdAndRiskEventList(equipmentIds, runningMonitorAirTerminalStrategy.exceptionFilterRiskMeasureAimLevel2());
        stopWatch.stop();
        // 查询IOT信息点值
        List<String> infoCodes = Lists.newArrayList(InfoCodeEnum.ONLINE_STATUS.value(),
                InfoCodeEnum.RUN_STATUS.value(), InfoCodeEnum.ORDER_FAIL_ALARM.value());
        stopWatch.start("查询设备IOT信息点");
        Map<String, List<InfoCodeValueVO>> objectIdAndInfoCodeValues =
                mapObjectIdAndInfoCodeValues(equipmentIds, infoCodes);
        stopWatch.stop();
        Map<String, AirTerminalEquipmentTagDTO> result = new LinkedHashMap<>();
        stopWatch.start("计算设备标签");
        for (String equipmentId : equipmentIds) {
            RunStatusEnum runStatusEnum = null;
            OnlineStatusEnum onlineStatusEnum = OnlineStatusEnum.OFFLINE;
            boolean isOrderFailAlarm = false;

            List<InfoCodeValueVO> values = objectIdAndInfoCodeValues.get(equipmentId);
            if (CollectionUtils.isNotEmpty(values)) {
                Map<String, InfoCodeValueVO> infoCodeAndValue = values.stream().collect(Collectors.toMap(
                        InfoCodeValueVO::getInfoCode, Function.identity(), (o1, o2) -> o1));
                InfoCodeValueVO onlineStatus = infoCodeAndValue.get(InfoCodeEnum.ONLINE_STATUS.value());
                InfoCodeValueVO runStatus = infoCodeAndValue.get(InfoCodeEnum.RUN_STATUS.value());
                InfoCodeValueVO orderFailAlarm = infoCodeAndValue.get(InfoCodeEnum.ORDER_FAIL_ALARM.value());
                if (Objects.nonNull(onlineStatus) && onlineStatus.equalsInfoCodeValue("1")) {
                    onlineStatusEnum = OnlineStatusEnum.ONLINE;
                }
                if (Objects.nonNull(runStatus)) {
                    if (runStatus.equalsInfoCodeValue("1")) {
                        runStatusEnum = RunStatusEnum.OPEN;
                    }
                    if (runStatus.equalsInfoCodeValue("0")) {
                        runStatusEnum = RunStatusEnum.CLOSE;
                    }
                }
                isOrderFailAlarm = Objects.nonNull(orderFailAlarm) && orderFailAlarm.equalsInfoCodeValue("1");
            }
            List<AirTerminalAbnormalStatusEnum> abnormalStatuses = new ArrayList<>();
            if (equipmentIdAndRiskEvents.containsKey(equipmentId)) {
                abnormalStatuses.add(AirTerminalAbnormalStatusEnum.ABNORMAL_EVENT);
            }
            if (isOrderFailAlarm) {
                abnormalStatuses.add(AirTerminalAbnormalStatusEnum.ORDER_FAIL_ALARM);
            }
            if (onlineStatusEnum == OnlineStatusEnum.OFFLINE) {
                abnormalStatuses.add(AirTerminalAbnormalStatusEnum.EQUIPMENT_OFFLINE);
            }
            AirTerminalEquipmentTagDTO equipmentTag = new AirTerminalEquipmentTagDTO();
            equipmentTag.setRunStatus(runStatusEnum);
            equipmentTag.setOnlineStatus(onlineStatusEnum);
            equipmentTag.setAbnormalStatuses(abnormalStatuses);
            result.put(equipmentId, equipmentTag);
        }
        stopWatch.stop();
        logProjectInstance(projectId).debug("查询设备标签计时器",
                Pair.of("message", stopWatch.prettyPrint(TimeUnit.MILLISECONDS)));
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, InfoCodeValueVO> mapSpaceIdAndTemperature(@Nullable Collection<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Maps.newHashMap();
        }
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValue(
                ContextUtil.getProjectId(), DtpRwdClassCodeEnum.SPACE, spaceIds, DtpRwdSpaceDTO.Fields.tdb);
        return infoCodeValues.stream().collect(Collectors.toMap(
                InfoCodeValueVO::getObjectId, Function.identity(), (o1, o2) -> o1));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, InfoCodeValueVO> mapSpaceIdAndCarbonDioxide(@Nullable Collection<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            return Maps.newHashMap();
        }
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValue(
                ContextUtil.getProjectId(), DtpRwdClassCodeEnum.SPACE, spaceIds, DtpRwdSpaceDTO.Fields.co2);
        return infoCodeValues.stream().collect(Collectors.toMap(
                InfoCodeValueVO::getObjectId, Function.identity(), (o1, o2) -> o1));
    }

    @Override
    public AirTerminalTemperatureIntervalEnum calculateTemperatureInterval(
            @NonNull BigDecimal value,
            @Nullable DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality) {
        List<String> expresses = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        try {
            String name = LocaleMessageUtil.getMessage("key_7274b502eecc4af1ac679f90941dfcbe");
            for (AirTerminalTemperatureIntervalEnum temperatureInterval : AirTerminalTemperatureIntervalEnum.values()) {
                expresses.add(temperatureInterval.ofDetail(name, tdbSeasonQuality).getExpress());
            }
            DefaultContext<String, Object> context = new DefaultContext<>();
            for (String express : expresses) {
                context.put(name, value);
                Object executeResult = EXPRESS_RUNNER.execute(express, context, errorList, true, false);
                if (Objects.isNull(executeResult)) {
                    continue;
                }
                return BaseEnum.analyze((String) executeResult, AirTerminalTemperatureIntervalEnum.class);
            }
        } catch (Exception e) {
            logProjectInstance().error("温度区间计算失败", e, Arrays.asList(
                    Pair.of("value", value),
                    Pair.of("errorList", errorList)
            ));
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_c9cb5f74811f4c4ca9642d1723650164"));
        }
        logProjectInstance().error("温度值不在任何一个指定区间中", Arrays.asList(
                Pair.of("value", value),
                Pair.of("expresses", expresses)
        ));
        throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_c60b697fc5864ed19431367a8c9db867"));
    }

    @Override
    public AirTerminalCarbonDioxideIntervalEnum calculateCarbonDioxideInterval(
            @NonNull BigDecimal value,
            @Nullable DemandConfigAirconditioningCO2QualityValueObject co2Quality) {
        List<String> expresses = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        try {
            String name = LocaleMessageUtil.getMessage("key_972a9be0b52b4be788682b58b69f8515");
            List<AirTerminalCarbonDioxideIntervalEnum> carbonDioxideIntervalEnums = Lists.newArrayList(
                    AirTerminalCarbonDioxideIntervalEnum.APPROPRIATE,
                    AirTerminalCarbonDioxideIntervalEnum.TOO_HIGH
            );
            for (AirTerminalCarbonDioxideIntervalEnum carbonDioxideInterval : carbonDioxideIntervalEnums) {
                expresses.add(carbonDioxideInterval.ofDetail(name, co2Quality).getExpress());
            }
            DefaultContext<String, Object> context = new DefaultContext<>();
            for (String express : expresses) {
                context.put(name, value);
                Object executeResult = EXPRESS_RUNNER.execute(express, context, errorList, true, false);
                if (Objects.isNull(executeResult)) {
                    continue;
                }
                return BaseEnum.analyze((String) executeResult, AirTerminalCarbonDioxideIntervalEnum.class);
            }
        } catch (Exception e) {
            logProjectInstance().error("二氧化碳区间计算失败", e, Arrays.asList(
                    Pair.of("value", value),
                    Pair.of("errorList", errorList)
            ));
            throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_17b9004e7e0b4952b442dfd02a106665"));
        }
        logProjectInstance().error("二氧化碳值不在任何一个指定区间中", Arrays.asList(
                Pair.of("value", value),
                Pair.of("expresses", expresses)
        ));
        throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(), LocaleMessageUtil.getMessage("key_2d0dc2daa9434c96b3b40333cbd96b40"));
    }

    @Nullable
    @Override
    public DemandConfigAirconditioningValueObject queryDemandProjectGlobalConfig() {
        DemandTemplateDateSettingVO dateSetting = demandCalendarQueryService.getDateSetting(
                DemandTemplateTypeEnum.AIRCONDITIONING, DateUtils.now().toLocalDate());
        if (Objects.isNull(dateSetting)) {
            return null;
        }
        String projectId = ContextUtil.getProjectId();
        DemandConfigByInstanceQueryParam instanceQueryParam = new DemandConfigByInstanceQueryParam();
        instanceQueryParam.setTemplateId(dateSetting.getRelTemplate().getTemplateId());
        instanceQueryParam.setInstance(new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, projectId));
        Optional<DemandConfigAggregate<DemandConfigAirconditioningValueObject>> projectDemandConfig =
                demandConfigGateway.getConfigByInstance(instanceQueryParam);
        if (projectDemandConfig.isPresent()) {
            DemandConfigValueObject<DemandConfigAirconditioningValueObject> finalConfig =
                    projectDemandConfig.get().getFinalConfig();
            if (Objects.nonNull(finalConfig)) {
                return finalConfig.getGlobalConfig();
            }
        }
        return null;
    }

    /**
     * 查询空间存在温度或二氧化碳信息点规则的映射
     *
     * @param projectId 项目ID
     * @param objectIds 对象ID
     * @return spaceId -> 存在规则的 infoCode 集合
     * @since 7.260401.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, Set<String>> mapSpaceIdAndExistsRuleInfoCodes(
            @NonNull String projectId, @NonNull Collection<String> objectIds) {
        Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
        objectIds.forEach(item -> MapExecutor.dynamicPut(classCodeAndObjectIds,
                DtpRwdClassCodeEnum.SPACE.value(), item, HashSet::new));
        return abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(projectId, classCodeAndObjectIds,
                Lists.newArrayList(DtpRwdSpaceDTO.Fields.tdb, DtpRwdSpaceDTO.Fields.co2));
    }

    @Override
    public Boolean getDialogContinueLoad(DemandSpaceByGroupFloorQueryParam param) {
        List<? extends DtpRwdSpaceVO> dtpRwdSpaceVOS = airTerminalQuerySpace.querySpace(param.getInstance(), true);
        return dtpRwdSpaceVOS.size() > 500;
    }

    /**
     * 查询仪表盘设备统计模板
     *
     * @return 仪表盘设备统计模板
     * @since 7.251101.0
     */
    private List<InfoCodeTemplateTypeModel> queryDashboardEquipmentTemplates() {
        List<String> ignoreClassCodes = Lists.newArrayList(
                DtpRwdClassCodeEnum.ACVVEJ.getValue(), DtpRwdClassCodeEnum.CFCSEW.getValue(),
                DtpRwdClassCodeEnum.ACVTSF.getValue(), DtpRwdClassCodeEnum.FFSCAF.getValue(),
                DtpRwdClassCodeEnum.ACAHG_GROUP.getValue(), DtpRwdClassCodeEnum.ACVVER.getValue(),
                DtpRwdClassCodeEnum.ACVVEA.getValue(), DtpRwdClassCodeEnum.ACATHT.getValue()
        );
        InfoCodeObjectQueryParam objectQueryParam = new InfoCodeObjectQueryParam();
        objectQueryParam.setRunningMonitorType(RunningMonitorTypeEnum.AIR_TERMINAL);
        objectQueryParam.setTemplateTypes(Lists.newArrayList(InfoCodeTemplateTypeEnum.TABLE_LIST));
        objectQueryParam.setObjectType(DtpRwdObjectTypeEnum.EQUIPMENT);
        List<InfoCodeTemplateTypeModel> templateTypes = infoCodeGateway.listTemplateTypesWithObjects(objectQueryParam);
        templateTypes.removeIf(item -> ignoreClassCodes.contains(item.getClassCode())
                || CollectionUtils.isEmpty(item.getObjects()));
        return templateTypes;
    }

    /**
     * 初始化品质统计上下文
     *
     * @param param 查询参数
     * @return 品质统计上下文
     * @since 5.240502.0
     */
    private AirTerminalQualityStatisticsContext initQualityStatisticsContext(
            @NonNull AirTerminalSpaceQualityStatisticsQueryParam param) {
        String projectId = param.getProjectId();
        AirTerminalInstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();

        AirTerminalQualityStatisticsContext context = new AirTerminalQualityStatisticsContext();
        context.setParam(param);

        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeAndData = dtpKnowledgeClientService.mapInfoCodeAndData(
                false, false, DtpRwdClassCodeEnum.SPACE.value());
        DtpKnowledgeInfoCodeDTO tdbDefine = infoCodeAndData.get(DtpRwdSpaceDTO.Fields.tdb);
        DtpKnowledgeInfoCodeDTO co2Define = infoCodeAndData.get(DtpRwdSpaceDTO.Fields.co2);
        if (ObjectUtils.anyNull(tdbDefine, co2Define)) {
            throw new BusinessException(ErrorCodeConstants.SERVICE_ERROR_C0001.getCode(), LocaleMessageUtil.getMessage("key_ea0d672d471d417eba626edb13b9e9f4"));
        }
        context.setTdbDefine(tdbDefine);
        context.setCo2Define(co2Define);

        AirTerminalQualityIntervalVO qualityInterval = getQualityInterval(
                new AirTerminalQualityIntervalQueryParam().setProjectId(projectId));
        AirTerminalDemandStatusVO demandStatus = getDemandStatus(
                new AirTerminalDemandStatusQueryParam().setProjectId(projectId));
        context.setTemperatureIntervals(qualityInterval.getTemperatureIntervals());
        context.setCarbonDioxideIntervals(qualityInterval.getCarbonDioxideIntervals());
        context.setTemperatureDemandStatuses(demandStatus.getTemperatureDemandStatuses());
        context.setCarbonDioxideDemandStatuses(demandStatus.getCarbonDioxideDemandStatuses());
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        equipmentQueryParam.setFilterExcludeClassCodes(Sets.newHashSet(DtpRwdClassCodeEnum.ACVVEJ.value()));
        List<DtpRwdEquipmentDTO> controlEquipments = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        List<DtpRwdSpaceDTO> spaces;
        if (CollectionUtils.isEmpty(controlEquipments)) {
            spaces = listColdSpaces(instanceType, instanceId);
        } else {
            spaces = querySpaces(projectId, instanceType, instanceId, DtpRwdObjectDTO.Fields.id);
        }
        if (CollectionUtils.isEmpty(spaces)) {
            return context.setFailed(true);
        }
        Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        context.setSpaces(spaces);

        Map<String, Map<String, AirTerminalDemandStatusEnum>> spaceIdAndDemandStatusBy60Minute =
                mapSpaceIdAndDemandStatusBy60Minute(spaceIds);
        context.setSpaceIdAndDemandStatusBy60Minute(spaceIdAndDemandStatusBy60Minute);

        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                spaceIds, Arrays.asList(DtpRwdSpaceDTO.Fields.tdb, DtpRwdSpaceDTO.Fields.co2));
        Map<String, InfoCodeValueVO> spaceIdAndTemperature = InfoCodeValueVO.groupInfoCodeValueMap(infoCodeValues, DtpRwdSpaceDTO.Fields.tdb);
        Map<String, InfoCodeValueVO> spaceIdAndCarbonDioxide = InfoCodeValueVO.groupInfoCodeValueMap(infoCodeValues, DtpRwdSpaceDTO.Fields.co2);

        context.setSpaceIdAndTemperature(spaceIdAndTemperature);
        context.setSpaceIdAndCarbonDioxide(spaceIdAndCarbonDioxide);
        return context;
    }

    /**
     * 计算并设置调度统计VO
     *
     * @param target          目标源
     * @param types           类型列表
     * @param demandStatusBag 需求状态包
     * @since 5.240502.0
     */
    private void calculateAndSetSchedulingStatistics(
            @NonNull AirTerminalSchedulingStatisticsVO target, @NonNull List<BaseEnumVO> types,
            @NonNull Bag<AirTerminalDemandStatusEnum> demandStatusBag) {
        int totalCount = 0;
        Map<AirTerminalDemandStatusEnum, Integer> typeAndCount = new HashMap<>(16);
        for (BaseEnumVO type : types) {
            String key = type.getKey().toString();
            AirTerminalDemandStatusEnum demandStatus = BaseEnum.analyze(key, AirTerminalDemandStatusEnum.class);
            int count = demandStatusBag.getCount(demandStatus);
            typeAndCount.put(demandStatus, count);
            totalCount += count;
        }
        target.setTotalCount(totalCount);
        target.setDemandStatusCount(typeAndCount);
    }

    /**
     * 计算并设置品质统计
     *
     * @param target 目标源
     * @param scale  精度
     * @param values 数值
     * @since 5.240502.0
     */
    private void calculateAndSetQualityStatistics(
            @NonNull AirTerminalQualityStatisticsVO target,
            @NonNull Integer scale,
            @Nullable List<BigDecimal> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        target.setMinimum(BigDecimalUtils.scale(bizComputeService.calculateMinimum(values),
                scale, BizComputeService.DEFAULT_ROUNDING_MODE));
        target.setMaximum(BigDecimalUtils.scale(bizComputeService.calculateMaximum(values),
                scale, BizComputeService.DEFAULT_ROUNDING_MODE));
        target.setAverage(bizComputeService.calculateAverage(values, scale));
        target.setMedian(bizComputeService.calculateMedian(values, scale));
        target.setUpperQuartile(bizComputeService.calculateUpperQuartile(values, scale));
        target.setLowerQuartile(bizComputeService.calculateLowerQuartile(values, scale));
    }

    /**
     * 初始化设备状态统计上下文
     *
     * @param param 设备状态统计查询参数
     * @return 设备状态统计上下文
     * @since 5.240502.0
     */
    private AirTerminalEquipmentStatusStatisticsContext initEquipmentStatusStatisticsContext(
            @NonNull AirTerminalEquipmentStatusStatisticsQueryParam param) {
        AirTerminalEquipmentStatusStatisticsContext context = new AirTerminalEquipmentStatusStatisticsContext();
        context.setParam(param);

        // 初始化设备信息
        context = initEquipmentContext(context);
        if (context.isFailed()) {
            return context.setFailed(true);
        }
        // 初始化建筑信息
        context = initBuildingContext(context);
        if (context.isFailed()) {
            return context.setFailed(true);
        }
        return context;
    }

    /**
     * 初始化设备上下文
     *
     * @param context 上下文
     * @return 上下文
     * @since 5.240502.0
     */
    private AirTerminalEquipmentStatusStatisticsContext initEquipmentContext(
            @NonNull AirTerminalEquipmentStatusStatisticsContext context) {
        if (context.isFailed()) {
            return context.setFailed(true);
        }
        String projectId = context.getParam().getProjectId();
        AirTerminalInstanceTypeEnum instanceType = context.getParam().getInstanceType();
        String instanceId = context.getParam().getInstanceId();

        Set<String> floorIds = instanceService.listFloorIdsByInstance(instanceType.toDemandInstanceTypeEnum(), instanceId);
        if (CollectionUtils.isEmpty(floorIds)) {
            return context.setFailed(true);
        }
        List<DtpRwdFloorDTO> floors = dtpRwdClientService.listFloor(false, true,
                projectId, new DtpRwdObjectQueryParam().setId(floorIds));
        Map<String, DtpRwdFloorDTO> floorIdAndData = new HashMap<>(Math.max(floors.size(), 16));
        for (DtpRwdFloorDTO floor : floors) {
            String id = floor.getId();
            floorIds.add(id);
            floorIdAndData.put(id, floor);
        }
        context.setFloors(floors);
        context.setFloorIdAndData(floorIdAndData);

        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.setFilterExcludeClassCodes(Sets.newHashSet(DtpRwdClassCodeEnum.ACVVEJ.value()));
        equipmentQueryParam.setFilterFloorIds(floorIds);
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds);
        List<DtpRwdEquipmentDTO> equipments = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        if (CollectionUtils.isEmpty(equipments)) {
            return context.setFailed(true);
        }
        Map<String, DtpRwdObjectDTO> equipmentIdAndData = new HashMap<>(Math.max(equipments.size(), 16));
        Map<String, Set<DtpRwdObjectDTO>> floorIdAndEquipments = new HashMap<>(128);
        for (DtpRwdObjectDTO equipment : equipments) {
            String equipmentId = equipment.getId();
            Set<String> relFloorIds = equipment.getRelObjIds();
            if (CollectionUtils.isNotEmpty(relFloorIds)) {
                relFloorIds.forEach(item -> MapExecutor.dynamicPut(floorIdAndEquipments, item, equipment, HashSet::new));
            }
            equipmentIdAndData.put(equipmentId, equipment);
        }
        Map<String, AirTerminalEquipmentTagDTO> equipmentIdAndTag =
                mapEquipmentIdAndTag(projectId, equipmentIdAndData.keySet());
        context.setEquipments(equipments);
        context.setEquipmentIdAndTag(equipmentIdAndTag);
        context.setFloorIdAndEquipments(floorIdAndEquipments);
        return context;
    }

    /**
     * 初始化建筑上下文
     *
     * @param context 上下文
     * @return 上下文
     * @since 5.240502.0
     */
    private AirTerminalEquipmentStatusStatisticsContext initBuildingContext(
            @NonNull AirTerminalEquipmentStatusStatisticsContext context) {
        if (context.isFailed()) {
            return context.setFailed(true);
        }
        List<DtpRwdFloorDTO> floors = context.getFloors();
        Map<String, DtpRwdFloorDTO> floorIdAndData = context.getFloorIdAndData();

        Set<String> floorIds = floors.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        List<DtpRwdBuildingDTO> buildings = dtpRwdClientService.listBuildingByFloorId(floorIds,
                new DtpRwdObjectQueryParam().addWithColumn(DtpRwdBuildingDTO.Fields.class));
        if (CollectionUtils.isEmpty(buildings)) {
            return context.setFailed(true);
        }
        Map<String, TreeSet<DtpRwdFloorDTO>> buildingIdAndFloors = new HashMap<>(16);
        for (DtpRwdBuildingDTO building : buildings) {
            String buildingId = building.getId();
            Set<String> relFloorIds = building.getRelObjIds();
            if (CollectionUtils.isEmpty(relFloorIds)) {
                continue;
            }
            for (String relFloorId : relFloorIds) {
                DtpRwdFloorDTO relFloor = floorIdAndData.get(relFloorId);
                if (Objects.nonNull(relFloor)) {
                    MapExecutor.dynamicPut(buildingIdAndFloors, buildingId, relFloor, TreeSet::new);
                }
            }
        }
        context.setBuildings(buildings);
        context.setBuildingIdAndFloors(buildingIdAndFloors);
        return context;
    }

    /**
     * 根据实例类型查询空间
     *
     * @param projectId    项目ID
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @param withColumns  查询字段
     * @return 空间
     * @since 5.240502.0
     */
    private List<DtpRwdSpaceDTO> querySpaces(
            @NonNull String projectId,
            @NonNull AirTerminalInstanceTypeEnum instanceType,
            @NonNull String instanceId,
            @Nullable String... withColumns) {
        Set<String> spaceIds = null;
        if (instanceType != AirTerminalInstanceTypeEnum.PROJECT) {
            spaceIds = instanceService.listSpaceIdsByInstance(instanceType.toDemandInstanceTypeEnum(), instanceId);
        }
        return querySpaces(projectId, spaceIds, null, withColumns);
    }

    /**
     * 查询空间
     *
     * @param projectId          项目ID
     * @param filterSpaceIds     筛选空间ID, 为空时不进行过滤
     * @param filterEquipmentIds 筛选设备ID, 为空时不进行过滤
     * @param withColumns        查询字段, 未空时查询默认字段
     * @return 空间
     * @since 5.240502.0
     */
    private List<DtpRwdSpaceDTO> querySpaces(
            @NonNull String projectId,
            @Nullable Collection<String> filterSpaceIds,
            @Nullable Set<String> filterEquipmentIds,
            @Nullable String... withColumns) {
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        if (CollectionUtils.isNotEmpty(filterEquipmentIds)) {
            equipmentQueryParam.setFilterObjectIds(filterEquipmentIds);
        }
        Set<String> equipmentIds = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(equipmentIds)) {
            return Lists.newArrayList();
        }

        AtomicReference<List<DtpRwdSpaceDTO>> serviceSpacesAtomic = new AtomicReference<>();
        AtomicReference<List<DtpRwdSpaceDTO>> inSpacesAtomic = new AtomicReference<>();
        // 设备服务空间
        CompletableFuture<AtomicReference<List<DtpRwdSpaceDTO>>> serviceSpacesFuture =
                CompletableFuture.supplyAsync(() ->
                {
                    serviceSpacesAtomic.set(queryServiceSpaces(filterSpaceIds, equipmentIds, withColumns));
                    return null;
                }, BusinessThreadPoolExecutor.ttlExecutor());
        // 设备所在空间
        CompletableFuture<AtomicReference<List<DtpRwdSpaceDTO>>> inSpacesFuture =
                CompletableFuture.supplyAsync(() ->
                {
                    inSpacesAtomic.set(dtpRwdClientService.listSpaceInByEquipmentId(false, false,
                            projectId, equipmentIds, new DtpRwdObjectQueryParam().setId(filterSpaceIds)
                                    .addWithColumn(DtpRwdSpaceDTO.Fields.class, withColumns)
                                    .addInfoCode(DtpRwdSpaceDTO.Fields.roomFuncType,
                                            DtpRwdCriteriaSignEnum.LIKE_RIGHT, "SFT-02.04.")));
                    return null;
                }, BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                serviceSpacesFuture,
                inSpacesFuture
        );
        allFutures.join();

        List<DtpRwdSpaceDTO> serviceSpaces = serviceSpacesAtomic.get();
        List<DtpRwdSpaceDTO> inSpaces = inSpacesAtomic.get();
        if (CollectionUtils.isNotEmpty(inSpaces)) {
            Set<String> inSpaceIds = inSpaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            serviceSpaces.removeIf(item -> inSpaceIds.contains(item.getId()));
            serviceSpaces.addAll(inSpaces);
        }
        return serviceSpaces;
    }

    /**
     * 查询设备服务空间
     *
     * @param filterSpaceIds     筛选空间ID, 为空时不进行过滤
     * @param filterEquipmentIds 筛选设备ID, 为空时不进行过滤
     * @param withColumns        查询字段, 为空时查询默认字段
     * @return 设备服务空间
     * @since 5.240502.0
     */
    private List<DtpRwdSpaceDTO> queryServiceSpaces(
            @Nullable Collection<String> filterSpaceIds,
            @Nullable Collection<String> filterEquipmentIds,
            @Nullable String... withColumns) {
        return dtpRwdClientService.listSpaceServiceByEquipmentId(filterEquipmentIds, new DtpRwdObjectQueryParam()
                .addWithColumn(DtpRwdSpaceDTO.Fields.class, withColumns)
                .setId(filterSpaceIds));
    }

    /**
     * 查询空间ID和需求状态映射
     *
     * @param spaceIds 空间ID
     * @return 温度|二氧化碳 -> [空间ID -> 需求状态]
     * @since 5.240502.0
     */
    private Map<String, Map<String, AirTerminalDemandStatusEnum>> mapSpaceIdAndDemandStatusBy60Minute(
            @NonNull Set<String> spaceIds) {
        Map<String, Map<String, AirTerminalDemandStatusEnum>> result = new HashMap<>(16);
        Map<String, AirTerminalDemandStatusEnum> spaceIdAndTemperatureDemandStatus =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        Map<String, AirTerminalDemandStatusEnum> spaceIdAndCarbonDioxideDemandStatus =
                new HashMap<>(Math.max(spaceIds.size(), 16));
        result.put(DtpRwdSpaceDTO.Fields.tdb, spaceIdAndTemperatureDemandStatus);
        result.put(DtpRwdSpaceDTO.Fields.co2, spaceIdAndCarbonDioxideDemandStatus);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return result;
        }

        LocalDateTime now = DateUtils.now();
        LocalDate today = now.toLocalDate();
        LocalTime time = now.toLocalTime();
        String queryCalculateResultDateString = DateTimeFormatter.ofPattern("yyyyMMdd").format(today);

        List<RcCalculateResult> rcCalculateResults = rcCalculateResultService.queryCalculateResultList(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(), spaceIds, queryCalculateResultDateString, null);
        if (CollectionUtils.isEmpty(rcCalculateResults)) {
            logProjectInstance().warn("未查询到价值计算结果", Arrays.asList(
                    Pair.of("spaceIds", spaceIds),
                    Pair.of("date", today)
            ));
            return result;
        }

        for (RcCalculateResult rcCalculateResult : rcCalculateResults) {
            String spaceId = rcCalculateResult.getSpaceId();
            String workDate = rcCalculateResult.getWorkDate();
            CalculateResult calculateResult = rcCalculateResult.getCalculateResult();
            if (Objects.isNull(calculateResult)) {
                continue;
            }
            TreeMap<LocalTime, CalculateResultDetail> tdb = calculateResult.getTdb();
            TreeMap<LocalTime, CalculateResultDetail> co2 = calculateResult.getCO2();

            AirTerminalDemandStatusEnum temperatureDemandStatus =
                    analyzeCalculateValueTypeBy60Minus(spaceId, workDate, time, tdb);
            if (Objects.nonNull(temperatureDemandStatus)) {
                spaceIdAndTemperatureDemandStatus.put(spaceId, temperatureDemandStatus);
            }

            AirTerminalDemandStatusEnum carbonDioxideDemandStatus =
                    analyzeCalculateValueTypeBy60Minus(spaceId, workDate, time, co2);
            if (Objects.nonNull(carbonDioxideDemandStatus)) {
                spaceIdAndCarbonDioxideDemandStatus.put(spaceId, carbonDioxideDemandStatus);
            }
        }
        return result;
    }

    /**
     * 解析需求状态
     *
     * @param spaceId  空间ID
     * @param workDate 计算日期
     * @param time     计算视角
     * @param source   价值计算结果详情
     * @return 需求状态
     * @since 5.240502.0
     */
    private AirTerminalDemandStatusEnum analyzeCalculateValueTypeBy60Minus(
            @NonNull String spaceId, @NonNull String workDate,
            @NonNull LocalTime time, @NonNull TreeMap<LocalTime, CalculateResultDetail> source) {
        if (MapUtils.isEmpty(source)) {
            return null;
        }
        LocalTime recent5MinuteEnd = DateUtils.getRecent5Minute(time);
        // 查询最近一小时的价值选择结果
        LocalTime recent5MinuteBegin = recent5MinuteEnd.minusHours(1);
        Map.Entry<LocalTime, CalculateResultDetail> entry = source.floorEntry(recent5MinuteEnd);
        if (Objects.isNull(entry) || entry.getKey().isBefore(recent5MinuteBegin)) {
            logProjectInstance().debug("最近一小时无价值计算结果", Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate),
                    Pair.of("recent5MinuteBegin", recent5MinuteBegin),
                    Pair.of("recent5MinuteEnd", recent5MinuteEnd)
            ));
            return null;
        }
        LocalTime calculateTime = entry.getKey();
        CalculateResultDetail detail = entry.getValue();
        if (Objects.isNull(detail)) {
            return null;
        }
        Boolean enable = detail.getEnable();
        if (BooleanUtils.isNotTrue(enable)) {
            logProjectInstance().debug("最近一小时价值计算无需求", Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate),
                    Pair.of("calculateTime", calculateTime)
            ));
            return null;
        }
        String type = detail.getType();
        if (StringUtils.isBlank(type)) {
            return null;
        }
        EnumCalculateValueType result = BaseEnum.analyze(type, EnumCalculateValueType.class, false);
        if (Objects.isNull(result)) {
            logProjectInstance().warn("价值计算类型解析失败", Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate),
                    Pair.of("calculateTime", calculateTime),
                    Pair.of("type", type)
            ));
            return null;
        }
        return AirTerminalDemandStatusEnum.of(result);
    }

    /**
     * 查询对象ID和信息点值映射
     *
     * @param objectIds 对象ID
     * @param infoCodes 信息点编码
     * @return 对象ID和信息点值映射
     * @since 5.240601.0
     */
    @SuppressWarnings("unchecked")
    private Map<String, List<InfoCodeValueVO>> mapObjectIdAndInfoCodeValues(
            @NonNull Collection<String> objectIds,
            @NonNull Collection<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Maps.newHashMap();
        }
        String projectId = ContextUtil.getProjectId();
        Function<Collection<String>, Supplier<List<InfoCodeValueVO>>> infoCodeValueFunction = source ->
                () -> abstractIotDataCacheServer.listInfoCodeValue(
                        projectId, null, source, infoCodes);
        List<InfoCodeValueVO> infoCodeValues;
        if (objectIds.size() < RPC_QUERY_PARTITION_SIZE) {
            infoCodeValues = infoCodeValueFunction.apply(objectIds).get();
        } else {
            List<List<String>> partitions = ListUtils.partition(new ArrayList<>(objectIds), RPC_QUERY_PARTITION_SIZE);
            List<CompletableFuture<List<InfoCodeValueVO>>> infoCodeFutures = partitions.stream()
                    .map(item -> CompletableFuture.supplyAsync(infoCodeValueFunction.apply(item),
                            TtlExecutors.getTtlExecutor(executor)))
                    .collect(Collectors.toList());
            infoCodeValues = futureGet(infoCodeFutures, ArrayList::new, List::addAll,
                    () -> new BusinessException(ErrorCodeConstants.SERVICE_ERROR_C0110.getCode(), LocaleMessageUtil.getMessage("key_4cc2225797bd4024ad0138510e22f223")));
        }
        return infoCodeValues.stream().collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));
    }

    /**
     * 查询对象ID和异常事件映射
     *
     * @param objectIds             对象ID
     * @param riskMeasureAimLevel2s 二级风控目标
     * @return 对象ID -> 异常事件列表
     * @since 5.240502.0
     */
    private Map<String, List<ExceptionEventBaseInfoVO>> mapObjectIdAndRiskEventList(@NonNull Set<String> objectIds, @Nullable Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return Maps.newHashMap();
        }
        return abstractCommonServer.mapRiskEvent(objectIds, riskMeasureAimLevel2s);
    }

    @Override
    public String logTag() {
        return "空调末端";
    }

    @Override
    public TagInfoVO getObjectTag(TagInfoQueryParam tagInfoQueryParam) {
        TagInfoVO tagInfoVO = new TagInfoVO();
        Optional<DtpRwdObjectDTO> dtpRwdObjectDTO = dtpRwdClientService.getObjectOpt(new DtpRwdObjectQueryParam()
                .setId(tagInfoQueryParam.getObjectId()));
        if (!dtpRwdObjectDTO.isPresent()) {
            return tagInfoVO;
        }
        String objType = dtpRwdObjectDTO.get().getObjType();
        // 兼容设备组
        if (objType.equals(ObjectTypeEnum.EQUIPMENT.value()) || objType.equals(ObjectTypeEnum.EQUIP_GROUP.value())) {
            List<ObjectClassCodeDTO> iotDataRequestVos = new ArrayList<>();
            ObjectClassCodeDTO objectClassCodeDTO = new ObjectClassCodeDTO();
            objectClassCodeDTO.setClassCode(dtpRwdObjectDTO.get().getClassCode());
            objectClassCodeDTO.setObjectId(tagInfoQueryParam.getObjectId());
            iotDataRequestVos.add(objectClassCodeDTO);
            Map<String, TagInfoVO> tagInfoVoMap = abstractCommonServer.mapEquipmentIdAndTag(tagInfoQueryParam.getProjectId(), iotDataRequestVos, null);
            if (MapUtils.isNotEmpty(tagInfoVoMap)) {
                tagInfoVO = tagInfoVoMap.getOrDefault(tagInfoQueryParam.getObjectId(), new TagInfoVO());
            }

        } else if (objType.equals(ObjectTypeEnum.SPACE.value())) {
            String spaceId = tagInfoQueryParam.getObjectId();
            Map<String, AirTerminalSpaceTagDTO> spaceTagMap =
                    this.mapSpaceIdAndTag(tagInfoQueryParam.getProjectId(), Lists.newArrayList(spaceId));
            if (MapUtils.isNotEmpty(spaceTagMap)) {
                tagInfoVO = tagConverter.toCommonTag(spaceTagMap.get(spaceId));
            }
        }
        tagInfoVO.setId(dtpRwdObjectDTO.get().getId());
        tagInfoVO.setLocalName(dtpRwdObjectDTO.get().getLocalName());
        tagInfoVO.setLocalId(dtpRwdObjectDTO.get().getLocalId());
        Object floorIdObject = dtpRwdObjectDTO.get().getUndefinedProperties().get("floorId");

        if (floorIdObject != null) {
            List<String> floorIdList = new ArrayList<>();
            if (floorIdObject instanceof String) {
                if (StringUtils.isNotBlank(floorIdObject.toString())) {
                    floorIdList.add(floorIdObject.toString());
                }
            } else if (floorIdObject instanceof ArrayList) {
                floorIdList.addAll((ArrayList) floorIdObject);
            }

            if (CollectionUtils.isNotEmpty(floorIdList)) {

                List<DtpRwdFloorDTO> dtpRwdObjectDTOList = dtpRwdClientService.listFloor(false, false,
                        tagInfoQueryParam.getProjectId(), new DtpRwdObjectQueryParam()
                                .addWithColumn(DtpRwdFloorDTO.Fields.class)
                                .setId(floorIdList));
                if (CollectionUtils.isNotEmpty(dtpRwdObjectDTOList)) {
                    // 当一个设备查询所在多个楼层时，直接处理成它就在最下面那一层(20240614产品陈觅提出 )
                    // floorSequenceID
                    DtpRwdObjectDTO dtpRwdObjectDTOFloor = dtpRwdObjectDTOList.get(0);
                    if (dtpRwdObjectDTOList.size() > 1) {
                        // 查找最小楼层
                        dtpRwdObjectDTOFloor = getMinFloorSequenceID(dtpRwdObjectDTOList);
                    }
                    tagInfoVO.setFloorId(dtpRwdObjectDTOFloor.getId());
                    tagInfoVO.setFloorLocalName(dtpRwdObjectDTOFloor.getLocalName());
                    tagInfoVO.setFloorLocalId(dtpRwdObjectDTOFloor.getLocalId());
                }
            }

        }
        return tagInfoVO;
    }

    /**
     * 最小楼层
     */
    public DtpRwdObjectDTO getMinFloorSequenceID(List<DtpRwdFloorDTO> dtpRwdObjectDTOList) {

        DtpRwdObjectDTO minDtpRwdObjectDTO = dtpRwdObjectDTOList.get(0);
        int min = dtpRwdObjectDTOList.get(0).getFloorSequenceId(); // 将第一个元素设为最大值

        for (int i = 1; i < dtpRwdObjectDTOList.size(); i++) {
            int floorSequenceID = dtpRwdObjectDTOList.get(i).getFloorSequenceId(); // 将第一个元素设为最大值
            if (floorSequenceID < min) {
                min = floorSequenceID; // 如果找到更大的值，则更新最大值
                minDtpRwdObjectDTO = dtpRwdObjectDTOList.get(i);
            }
        }

        return minDtpRwdObjectDTO;

    }

    @Override
    public AirTerminalEquipManualAutoStatusVo queryManualAutoStatusByObjectId(SingleObjectDTO dto) {
        String projectId = dto.getProjectId();
        String objectId = dto.getObjectId();
        AirTerminalEquipManualAutoStatusVo result = new AirTerminalEquipManualAutoStatusVo();
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setRelationFrom(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjTo(objectId));
        String manualAutoStatus = this.abstractIotDataCacheServer.queryIotRealData(ContextUtil.getGroupCode(), projectId, objectId,
                dto.getSystemClassCode(), InfoCodeEnum.MANUAL_AUTO_STATUS.getCode());
        result.setManualAutoStatus(manualAutoStatus);
        String expireTime = null;
        if ("0".equals(manualAutoStatus)) {
            try {
                EquipRecord equipRecord = this.equipRecordService.getEquipRecordByInfoCodeDesc(ContextUtil.getGroupCode(), projectId, null, objectId, InfoCodeEnum.MANUAL_AUTO_SET.getCode(), -1);
                expireTime = equipRecord == null ? null : equipRecord.getChangeContent() == null ? null : equipRecord.getChangeContent().getExecTime();
            } catch (Exception e) {
                log().error("云端拿不到过期时间，忽略错误", e);
            }

        }
        result.setExpireTime(expireTime);
        return result;
    }

    @Override
    public Map<String, Set<String>> mapClaasCode2objectIds(String projectId, String instanceId, AirTerminalInstanceTypeEnum instanceType) {
        Map<String, Set<String>> map = new HashMap<>(8);
        // 空间
        List<DtpRwdSpaceDTO> spaces = querySpaces(projectId, instanceType, instanceId);
        if (CollectionUtil.isNotEmpty(spaces)) {
            // 获取space列表中的id集合
            Set<String> spaceIds = spaces.stream().map(DtpRwdSpaceDTO::getId).collect(Collectors.toSet());
            map.put(CommonConst.DtpRwdConst.GeneralZone, spaceIds);
        } else {
            map.put(CommonConst.DtpRwdConst.GeneralZone, new HashSet<>(0));
        }
        // 设备
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.classCode);
        List<DtpRwdEquipmentDTO> dtpRwdObjectDTOS = runningMonitorAirTerminalStrategy.listEquipments(equipmentQueryParam);
        // 根据classCode分组对象id列表
        Map<String, Set<String>> objectIdMap = dtpRwdObjectDTOS.stream().collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        for (DtpRwdClassCodeEnum clazzCodeEnum : ALL_EQUIPMENT_CLASS_CODES) {
            if (!objectIdMap.containsKey(clazzCodeEnum.getValue())) {
                objectIdMap.put(clazzCodeEnum.getValue(), new HashSet<>(0));
            }
        }
        map.putAll(objectIdMap);
        return map;
    }

    @Override
    public TreeMap<LocalTime, AirTerminalHistoryPlaybackVO> historyPlaybackByTime(String groupCode, AirTerminalHistoryPlaybackParam param) {
        TreeMap<LocalTime, AirTerminalHistoryPlaybackVO> historyPlaybackVOTreeMap = new TreeMap<>();

        boolean floor = DemandInstanceTypeEnum.FLOOR.equals(param.getInstanceType());
        String workDate = param.getStartDateTime().toLocalDate().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATE_PATTERN));
        List<LocalDateTime> localDateTimes = CalculateComputeUtil.spiltDateTimeByMinute(param.getStartDateTime(), param.getEndDateTime(), 15, true);

        // 1.获取所有的空间ID
        Set<String> spaceIds = this.rcCalculateResultService.getSpaceCalculateResultIds(groupCode, param.getProjectId(), workDate);

        // 2.判断是否需要进行楼层过滤
        if (floor) {
            Pair<String, String> spaceInFloor = DtpRwdRelationTypeEnum.SPACE_IN_FLOOR.value();
            List<JSONObject> jsonObjects = this.bizDtpRwdService.queryRelation(groupCode, param.getProjectId(), spaceInFloor.getLeft(), spaceInFloor.getRight(), DtpRwdClassCodeEnum.SPACE.getValue(), Lists.newArrayList(param.getInstanceId()), null);
            Set<String> floorSpaceIds = CollectionUtil.isEmpty(jsonObjects) ? new HashSet<>() : jsonObjects.stream().map(item -> item.getString("objTo")).collect(Collectors.toSet());
            spaceIds.retainAll(floorSpaceIds);
        }
        if (CollectionUtils.isEmpty(spaceIds)) {
            return historyPlaybackVOTreeMap;
        }

        // 3.获取空间其他参数的信息点配置，及绑点规则
        List<InfoCodeConfigVO> infoCodeConfigs = this.getInfoCodeConfig();
        Set<String> infoCodes = infoCodeConfigs.stream().map(InfoCodeConfigVO::getInfoCode).collect(Collectors.toSet());
        Map<String, Set<String>> objectsExistRuleMap = this.abstractCommonServer.queryObjectsExistRuleMap(groupCode, param.getProjectId(), Lists.newArrayList(new ClassCode(DtpRwdClassCodeEnum.SPACE.getValue(), infoCodes)));

        // 4.根据空间，获取此时间段内所有的调度情况数据
        List<CompletableFuture<TreeMap<LocalTime, AirTerminalHistoryPlaybackVO>>> spaceFutures = new ArrayList<>();
        for (String spaceId : spaceIds) {
            CompletableFuture<TreeMap<LocalTime, AirTerminalHistoryPlaybackVO>> spaceFuture = CompletableFuture.supplyAsync(() ->
                    this.singleSpaceHistoryPlaybackByTime(groupCode, param.getProjectId(), spaceId, workDate, infoCodeConfigs, objectsExistRuleMap.get(spaceId), localDateTimes), BusinessThreadPoolExecutor.ttlExecutor());
            spaceFutures.add(spaceFuture);
        }

        List<TreeMap<LocalTime, AirTerminalHistoryPlaybackVO>> treeMaps = BusinessThreadPoolExecutor.getResult(spaceFutures);
        if (CollectionUtils.isEmpty(treeMaps)) {
            return historyPlaybackVOTreeMap;
        }

        // 5.获取价值计算方案对应的曲线图数据
        TreeMap<LocalTime, AirTerminalHistoryPlaybackVO> firstHistoryPlaybackMap = treeMaps.get(0);
        Map<BasicsConfigTypeEnum, List<ValueSelectionTrendTableDto.ElecData>> valueSelectionMap = new HashMap<>();
        LocalDateTime endDateTime = LocalDateTime.of(param.getStartDateTime().toLocalDate(), LocalTime.MAX);
        Set<BasicsConfigTypeEnum> configTypeEnums = firstHistoryPlaybackMap.values().stream().map(selection -> selection.getValueSelection().getValueSelectType()).collect(Collectors.toSet());
        for (BasicsConfigTypeEnum configTypeEnum : configTypeEnums) {
            if (BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CAST.equals(configTypeEnum) || BasicsConfigTypeEnum.VALUE_SELECT_LOWEST_CARBON_EMISSIONS.equals(configTypeEnum)) {
                // 查询此日的曲线图数据
                List<ValueSelectionTrendTableDto.ElecData> elecDataList = this.abstractCommonServer.queryValueSelectionDataList(groupCode, param.getProjectId(), endDateTime, configTypeEnum);
                valueSelectionMap.put(configTypeEnum, elecDataList);
            }
        }

        // 6.响应数据组装
        for (LocalDateTime localDateTime : localDateTimes) {
            LocalTime localTime = localDateTime.toLocalTime();
            AirTerminalHistoryPlaybackVO playbackVO = firstHistoryPlaybackMap.get(localTime);

            List<AirTerminalHistoryPlaybackVO.SpaceDetail> spaceDetailList = new ArrayList<>();
            TreeMap<String, List<AirTerminalHistoryPlaybackVO.SpaceStatistics>> resultStatistics = new TreeMap<>();
            Map<AirTerminalDemandStatusEnum, List<AirTerminalHistoryPlaybackVO.SpaceDetail>> tdbdemandStatusEnumListMap = new HashMap<>();
            Map<AirTerminalDemandStatusEnum, List<AirTerminalHistoryPlaybackVO.SpaceDetail>> cO2demandStatusEnumListMap = new HashMap<>();

            for (TreeMap<LocalTime, AirTerminalHistoryPlaybackVO> spaceMap : treeMaps) {
                AirTerminalHistoryPlaybackVO historyPlaybackVO = spaceMap.get(localTime);
                AirTerminalHistoryPlaybackVO.SpaceDetail spaceDetail = historyPlaybackVO == null || historyPlaybackVO.getSpaceDetail() == null ? null : historyPlaybackVO.getSpaceDetail().get(0);
                if (spaceDetail != null) {
                    spaceDetailList.add(spaceDetail);
                    tdbdemandStatusEnumListMap.computeIfAbsent(spaceDetail.getTemperatureDemandStatus(), e -> Lists.newArrayList()).add(spaceDetail);
                    cO2demandStatusEnumListMap.computeIfAbsent(spaceDetail.getCarbonDioxideDemandStatus(), e -> Lists.newArrayList()).add(spaceDetail);
                }
            }

            TreeMap<String, List<AirTerminalHistoryPlaybackVO.SpaceStatistics>> spaceStatistics = playbackVO == null ? null : playbackVO.getSpaceStatistics();
            if (spaceStatistics != null) {
                int demandTotal = spaceDetailList.size();
                for (Map.Entry<String, List<AirTerminalHistoryPlaybackVO.SpaceStatistics>> stringListEntry : spaceStatistics.entrySet()) {
                    String infoCode = stringListEntry.getKey();
                    List<AirTerminalHistoryPlaybackVO.SpaceStatistics> statistics = stringListEntry.getValue();
                    for (AirTerminalHistoryPlaybackVO.SpaceStatistics statistic : statistics) {
                        AirTerminalDemandStatusEnum demandStatus = statistic.getDemandStatus();
                        List<AirTerminalHistoryPlaybackVO.SpaceDetail> spaceDetails = InfoCodeEnum.SPACE_TDB.getCode().equals(infoCode) ? tdbdemandStatusEnumListMap.get(demandStatus) : cO2demandStatusEnumListMap.get(demandStatus);
                        Integer demandCount = CollectionUtils.isEmpty(spaceDetails) ? null : spaceDetails.size();
                        resultStatistics.computeIfAbsent(infoCode, e -> Lists.newArrayList()).add(new AirTerminalHistoryPlaybackVO.SpaceStatistics(demandStatus, demandTotal, demandCount));
                    }
                }
            }

            AirTerminalHistoryPlaybackVO.ValueSelection valueSelection = playbackVO == null ? null : playbackVO.getValueSelection();
            AirTerminalHistoryPlaybackVO.ValueSelection resultSelection = valueSelection == null ? new AirTerminalHistoryPlaybackVO.ValueSelection()
                    : new AirTerminalHistoryPlaybackVO.ValueSelection(valueSelectionMap.get(valueSelection.getValueSelectType()), valueSelection.getFillRate(), valueSelection.getValueSelectType());
            historyPlaybackVOTreeMap.put(localTime, new AirTerminalHistoryPlaybackVO(spaceDetailList, resultStatistics, resultSelection));
        }

        return historyPlaybackVOTreeMap;
    }

    /**
     * 处理一个时间点的调度数据
     */
    private TreeMap<LocalTime, AirTerminalHistoryPlaybackVO> singleSpaceHistoryPlaybackByTime(String groupCode, String projectId, String spaceId, String workDate, List<InfoCodeConfigVO> infoCodeConfigs,
                                                                                              Set<String> filterRuleCodes, List<LocalDateTime> localDateTimes) {
        TreeMap<LocalTime, AirTerminalHistoryPlaybackVO> terminalHistoryPlaybackVOTreeMap = new TreeMap<>();

        try {
            // 1.获取空间的调度情况
            RcCalculateResult rcCalculateResult = this.rcCalculateResultService.getSpaceCalculateResult(groupCode, projectId, spaceId, workDate);
            if (rcCalculateResult == null) {
                return terminalHistoryPlaybackVOTreeMap;
            }
            TreeMap<LocalTime, CalculateResultConditionDetail> conditionDetailTreeMap = rcCalculateResult.getExtendInfo() == null ? null : rcCalculateResult.getExtendInfo().getConditionDetailTreeMap();
            MidCalendarAcSeasonTypeEnum seasonTypeEnum = null;
            for (LocalDateTime localDateTime : localDateTimes) {
                CalculateResultConditionDetail conditionDetail = conditionDetailTreeMap == null ? null : conditionDetailTreeMap.get(localDateTime.toLocalTime());
                seasonTypeEnum = conditionDetail == null ? null : MidCalendarAcSeasonTypeEnum.analyze(conditionDetail.getSeason());
                if (seasonTypeEnum != null) {
                    break;
                }
            }
            List<AirTerminalDemandStatusEnum> tdbStatusEnumList = AirTerminalDemandStatusEnum.of(seasonTypeEnum, InfoCodeEnum.SPACE_TDB);
            List<AirTerminalDemandStatusEnum> cO2StatusEnumList = AirTerminalDemandStatusEnum.of(seasonTypeEnum, InfoCodeEnum.SPACE_CO2);

            // 2.获取此时间段内所有的IOT数据
            Set<String> infoCodes = infoCodeConfigs.stream().map(InfoCodeConfigVO::getInfoCode).collect(Collectors.toSet());
            infoCodes.add(InfoCodeEnum.SPACE_TDB.getCode());
            infoCodes.add(InfoCodeEnum.SPACE_CO2.getCode());
            List<EnergyDataResponse> energyDataResponses = this.dtpRwdClientService.queryIPDataInPeriod(groupCode, projectId, Sets.newHashSet(spaceId), infoCodes, localDateTimes.get(0), localDateTimes.get(localDateTimes.size() - 1).plusMinutes(15), "15min");
            Map<String, Map<String, EnergyDataResponse>> infoCodeTimeValueMap = CollectionUtils.isEmpty(energyDataResponses) ? new HashMap<>()
                    : energyDataResponses.stream().collect(Collectors.groupingBy(EnergyDataResponse::getIpTime, Collectors.toMap(EnergyDataResponse::getInfoCode, Function.identity(), (v1, v2) -> v2)));

            // 3.按时间段进行数据组装
            for (LocalDateTime localDateTime : localDateTimes) {
                LocalTime currentTime = localDateTime.toLocalTime();
                AirTerminalHistoryPlaybackVO.SpaceDetail spaceDetail = new AirTerminalHistoryPlaybackVO.SpaceDetail();
                TreeMap<String, List<AirTerminalHistoryPlaybackVO.SpaceStatistics>> spaceStatisticsList = new TreeMap<>();
                Map<String, EnergyDataResponse> infoCodeValueMap = infoCodeTimeValueMap.get(localDateTime.format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATETIME_PATTERN)));

                // 价值方案数据
                CalculateResultConditionDetail conditionDetail = conditionDetailTreeMap == null ? null : conditionDetailTreeMap.get(currentTime);
                AirTerminalHistoryPlaybackVO.ValueSelection valueSelection = new AirTerminalHistoryPlaybackVO.ValueSelection();
                valueSelection.setValueSelectType(conditionDetail == null ? null : BasicsConfigTypeEnum.resolve(conditionDetail.getCalculateType()));
                valueSelection.setFillRate(conditionDetail == null ? null : conditionDetail.getFillRate());

                // hover相关数据，受绑点规则限制
                List<InfoCodeConfigVO> infoCodeConfigVOList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(filterRuleCodes)) {
                    for (InfoCodeConfigVO infoCodeConfig : infoCodeConfigs) {
                        if (filterRuleCodes.contains(infoCodeConfig.getInfoCode())) {
                            InfoCodeConfigVO temp = new InfoCodeConfigVO();
                            BeanUtil.copyProperties(infoCodeConfig, temp);

                            EnergyDataResponse dataResponse = infoCodeValueMap == null ? null : infoCodeValueMap.get(infoCodeConfig.getInfoCode());
                            if (dataResponse != null) {
                                temp.setDataTime(dataResponse.toDataTime());
                                temp.setDataValue(dataResponse.convertDataValueStr(infoCodeConfig.getScale()));
                            }
                            infoCodeConfigVOList.add(temp);
                        }

                    }
                }

                // 详情数据
                TreeMap<LocalTime, CalculateResultDetail> tdbTreeMap = rcCalculateResult.getCalculateResult() == null ? null : rcCalculateResult.getCalculateResult().getTdb();
                TreeMap<LocalTime, CalculateResultDetail> cO2TreeMap = rcCalculateResult.getCalculateResult() == null ? null : rcCalculateResult.getCalculateResult().getCO2();
                CalculateResultDetail tdbCalculateResultDetail = tdbTreeMap == null ? null : tdbTreeMap.get(currentTime);
                CalculateResultDetail cO2CalculateResultDetail = cO2TreeMap == null ? null : cO2TreeMap.get(currentTime);
                boolean tdbEnable = BooleanUtil.isTrue(tdbCalculateResultDetail != null && tdbCalculateResultDetail.getEnable());
                boolean cO2Enable = BooleanUtil.isTrue(cO2CalculateResultDetail != null && cO2CalculateResultDetail.getEnable());

                // 数据处理
                if (tdbEnable) {
                    spaceDetail.setTemperatureDemandValue(tdbCalculateResultDetail.getValue());
                    spaceDetail.setTemperatureDemandStatus(AirTerminalDemandStatusEnum.of(EnumCalculateValueType.analyze(tdbCalculateResultDetail.getType())));
                }
                if (cO2Enable) {
                    spaceDetail.setCarbonDioxideDemandValue(cO2CalculateResultDetail.getValue());
                    spaceDetail.setCarbonDioxideDemandStatus(AirTerminalDemandStatusEnum.of(EnumCalculateValueType.analyze(cO2CalculateResultDetail.getType())));
                }

                if (tdbEnable || cO2Enable) {
                    EnergyDataResponse tdbDataResponse = infoCodeValueMap == null ? null : infoCodeValueMap.get(InfoCodeEnum.SPACE_TDB.getCode());
                    EnergyDataResponse cO2DataResponse = infoCodeValueMap == null ? null : infoCodeValueMap.get(InfoCodeEnum.SPACE_CO2.getCode());

                    spaceDetail.setId(rcCalculateResult.getSpaceId());
                    spaceDetail.setLocalId(rcCalculateResult.getSpaceLocalId());
                    spaceDetail.setLocalName(rcCalculateResult.getSpaceLocalName());
                    spaceDetail.setTemperatureCurrentValue(tdbDataResponse == null ? null : tdbDataResponse.convertDataValue(1));
                    spaceDetail.setCarbonDioxideCurrentValue(cO2DataResponse == null ? null : cO2DataResponse.convertDataValue(1));
                    spaceDetail.setRelInfoCodeConfigs(infoCodeConfigVOList);
                }

                // 统计数据，默认需要存在标签，所以必须要有这一步
                if (CollectionUtils.isNotEmpty(tdbStatusEnumList)) {
                    for (AirTerminalDemandStatusEnum tdbStatusEnum : tdbStatusEnumList) {
                        spaceStatisticsList.computeIfAbsent(InfoCodeEnum.SPACE_TDB.getCode(), e -> Lists.newArrayList()).add(new AirTerminalHistoryPlaybackVO.SpaceStatistics(tdbStatusEnum, null, null));
                    }
                }
                if (CollectionUtils.isNotEmpty(cO2StatusEnumList)) {
                    for (AirTerminalDemandStatusEnum cO2StatusEnum : cO2StatusEnumList) {
                        spaceStatisticsList.computeIfAbsent(InfoCodeEnum.SPACE_CO2.getCode(), e -> Lists.newArrayList()).add(new AirTerminalHistoryPlaybackVO.SpaceStatistics(cO2StatusEnum, null, null));
                    }
                }
                terminalHistoryPlaybackVOTreeMap.put(localDateTime.toLocalTime(), new AirTerminalHistoryPlaybackVO(tdbEnable || cO2Enable ? Lists.newArrayList(spaceDetail) : null, spaceStatisticsList, valueSelection));
            }
        } catch (Exception e) {
            logProjectInstance(projectId).log(Level.ERROR, "单空间价值计算结果解析失败", e, Arrays.asList(
                    Pair.of("spaceId", spaceId),
                    Pair.of("workDate", workDate)
            ));
        }

        return terminalHistoryPlaybackVOTreeMap;
    }

    /**
     * 获取信息点配置
     */
    private List<InfoCodeConfigVO> getInfoCodeConfig() {
        List<InfoCodeConfigVO> infoCodeConfigs = new ArrayList<>();
        try {
            DtpRwdObjectDTO dtpRwdObjectDTO = new DtpRwdObjectDTO();
            // dtpRwdObjectDTO.setId(spaceId);
            dtpRwdObjectDTO.setObjType("space");
            dtpRwdObjectDTO.setClassCode(DtpRwdClassCodeEnum.SPACE.value());

            InfoCodeInitByObjectParam equipmentInfoCodeInitByObjectParam = new InfoCodeInitByObjectParam();
            equipmentInfoCodeInitByObjectParam.setRunningMonitorType(RunningMonitorTypeEnum.AIR_TERMINAL);
            equipmentInfoCodeInitByObjectParam.setTemplateTypes(Lists.newArrayList(InfoCodeTemplateTypeEnum.HOVER));
            equipmentInfoCodeInitByObjectParam.setGroupCodes(Lists.newArrayList("enviroParameter"));
            equipmentInfoCodeInitByObjectParam.setObjects(Lists.newArrayList(dtpRwdObjectDTO));
            equipmentInfoCodeInitByObjectParam.setNotFilterIotInfoCodeRule(true);
            List<InfoCodeAggregate> aggregates = infoCodeGateway.of(equipmentInfoCodeInitByObjectParam);

            infoCodeGateway.fillInfoCodeDefineModel(aggregates);
            if (CollectionUtils.isEmpty(aggregates)) {
                return infoCodeConfigs;
            }

            aggregates.sort(Comparator.comparing(item -> item.getConfigModel().getSequence()));
            aggregates.forEach(aggregate -> infoCodeConfigs.add(InfoCodeConfigVO.of(aggregate, null)));
        } catch (Exception e) {
            logProjectInstance().error("获取信息点配置失败", e);
        }
        return infoCodeConfigs;
    }

}
