package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.domain.model.ClassCodeDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataSourceDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.weather.WeatherDayHistoryDataDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplateCalendar;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.WeatherResourceEnum;
import com.persagy.apccontrol.basic.model.enumtype.weather.WeatherQueryTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarWorkTimeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectClassCodeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.resource.demand.DemandConfigAirconditioningCustomService;
import com.persagy.apccontrol.basic.resource.demand.DemandTemplateCalendarService;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateCalendarQueryDaoParam;
import com.persagy.apccontrol.basic.service.BizComputeService;
import com.persagy.apccontrol.basic.service.WeatherService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.DemandConfigGateway;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.DemandSpacesQueryParam;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.ExceptionRecordDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.WeatherDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.EnvQualityConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.DateTimeTypeEnum;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 运行分析报告策略抽象类
 *
 * <p><b>-------------------------------- 天气数据 --------------------------------</b></p>
 * <ul>
 *     <li><b>查询天气数据</b> - {@link #queryWeather(LocalDate)}</li>
 *     <li><b>查询气象站数据</b> - {@link #queryWeatherStation(LocalDate)}</li>
 *     <li><b>查询天气小时历史数据</b> - {@link #queryWeatherHourHistories(LocalDate)}</li>
 * </ul>
 * <hr/>
 * <p><b>-------------------------------- 需求配置 --------------------------------</b></p>
 * <ul>
 *     <li><b>查询需求模板配置</b> - {@link #queryTemplate(LocalDate)}</li>
 *     <li><b>查询价值选择配置</b> - {@link #queryValueSelect(LocalDate)}</li>
 * </ul>
 * <hr/>
 * <p><b>-------------------------------- IOT数据 --------------------------------</b></p>
 * <ul>
 *     <li><b>查询指定对象的信息点值</b> - {@link #queryInfoCodeValues(LocalDate, Set, DtpRwdPersistenceTimeTypeEnum, Collection)}</li>
 *     <li><b>查询系统累计电量</b> - {@link #queryAccElecConsumByDate(List, Set)}</li>
 *     <li><b>计算系统耗电量环比</b> - {@link #calculateMom(LocalDate, List, Map)}</li>
 *     <li><b>计算信息点总和</b> - {@link #calculateInfoCodeSum(List)}</li>
 *     <li><b>计算信息点平均值</b> - {@link #calculateInfoCodeAverage(List)}</li>
 * </ul>
 * <hr/>
 * <p><b>-------------------------------- 数据字典 --------------------------------</b></p>
 * <ul>
 *     <li><b>查询设备类定义</b> - {@link #queryClassCode2Define(Collection)}</li>
 *     <li><b>查询设备类指定信息点定义</b> - {@link #queryInfoCodeDefines(Map)}</li>
 *     <li><b>查询设备类信息点定义</b> - {@link #queryClassCode2InfoCodeDefines(Collection)}</li>
 *     <li><b>合并多个设备类下信息点定义数据</b> - {@link #mergeClassCode2InfoCodeDefines(List)}</li>
 * </ul>
 * <hr/>
 * <p><b>-------------------------------- 工作历 --------------------------------</b></p>
 * <ul>
 *     <li><b>查询对象工作时间</b> - {@link #queryObjectId2WorkTimes(LocalDate, Set)}</li>
 * </ul>
 * <hr/>
 * <p><b>-------------------------------- 空间范围 --------------------------------</b></p>
 * <ul>
 *     <li><b>查询报告分析的空间范围</b> - {@link #querySpaceIds(LocalDate, Map)}</li>
 * </ul>
 * <hr/>
 * <p><b>-------------------------------- 相似日 --------------------------------</b></p>
 * <ul>
 *     <li><b>查询相似的上周日期</b> - {@link #querySimilarWeekDates(Optional, LocalDate)}</li>
 * </ul>
 * <hr/>
 * <p><b>-------------------------------- 异常记录 --------------------------------</b></p>
 * <ul>
 *     <li><b>查询异常记录</b> - {@link #queryExceptionRecords(LocalDate, List, Map)}</li>
 *     <li><b>查询有效的信息点定义</b> - {@link #queryExceptionRecordValidInfoCodeDefines(List)}</li>
 *     <li><b>查询信息点值数据</b> - {@link #queryExceptionRecordInfoCodeValues(LocalDate, List, Map)}</li>
 *     <li><b>构建异常记录</b> - {@link #buildExceptionRecords(List, List, Map)}</li>
 * </ul>
 * <hr/>
 *
 * @author JiYinchuan
 * @since 7.250102.0
 */
public abstract class AbstractRunAnalysisReportStrategy<T extends RunAnalysisReportDataDto>
        implements RunAnalysisReportStrategy<T, RunAnalysisReportConfig>, LogComponent {

    @Resource
    protected DemandConfigGateway<DemandConfigAirconditioningValueObject> demandConfigGateway;
    @Resource
    protected DemandConfigAirconditioningCustomService demandConfigAirconditioningCustomService;
    @Resource
    protected WeatherClientService weatherClientService;
    @Resource
    protected WeatherService weatherService;
    @Resource
    protected AbstractCommonServer abstractCommonServer;
    @Resource
    protected DtpKnowledgeClientService dtpKnowledgeClientService;
    @Resource
    protected AbstractIotDataCacheServer<?> abstractIotDataCacheServer;
    @Resource
    protected MidCalendarClientService midCalendarClientService;
    @Resource
    protected DemandTemplateCalendarService demandTemplateCalendarService;
    @Resource
    protected BizComputeService bizComputeService;

    // region 天气数据

    /**
     * 查询天气数据
     *
     * @param date 日期
     * @return 天气数据对象
     * @since 7.260102.0
     */
    public @NonNull WeatherDTO queryWeather(@NonNull LocalDate date) {
        WeatherDayHistoryDataDTO weatherDayHistory = this.queryWeatherDayHistory(date);
        logProjectInstance().info("查询-天气日历史数据", Arrays.asList(
                Pair.of("date", date),
                Pair.of("weatherDayHistory", weatherDayHistory)
        ));
        Optional<Pair<String, String>> sunRiseAndSetPairOpt = Optional.ofNullable(weatherDayHistory)
                .map(WeatherDayHistoryDataDTO::getTextPair);

        // 根据配置选择天气数据源
        List<WeatherDTO.Detail> weatherHourHistories;
        // 温度和湿度范围（气象监测站数据源时使用原始数据的范围）
        Range<BigDecimal> temperatureRange;
        Range<BigDecimal> humidityRange;
        //气象站每15分钟的原始数据
        List<WeatherDTO.Detail> weatherStationHourHistories = null;

        WeatherResourceEnum shellConfigWeatherChange = this.abstractCommonServer.getShellConfigWeatherChange();
        if (shellConfigWeatherChange == WeatherResourceEnum.WEATHER_STATION) {
            // 气象监测站数据源：使用原始数据的温度/湿度范围
            WeatherStationData weatherStationData = this.queryWeatherStation(date);
            weatherHourHistories = weatherStationData.hourDetails;
            weatherStationHourHistories = weatherStationData.originalStationDetails;
            temperatureRange = weatherStationData.temperatureRange;
            humidityRange = weatherStationData.humidityRange;
            logProjectInstance().info("查询-气象监测站历史小时数据", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("weatherHourHistories", weatherHourHistories)
            ));
        } else {
            // 天气服务数据源：从小时数据中计算温度/湿度范围
            weatherHourHistories = this.queryWeatherHourHistories(date);
            temperatureRange = WeatherDTO.parseTemperatureRange(weatherHourHistories);
            humidityRange = WeatherDTO.parseHumidityRange(weatherHourHistories);
            logProjectInstance().info("查询-天气服务历史小时数据", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("weatherHourHistories", weatherHourHistories)
            ));
        }

        // 解析温度平均值
        BigDecimal temperatureAverage = WeatherDTO.parseTemperatureAverage(weatherHourHistories);
        // 解析湿度平均值
        BigDecimal humidityAverage = WeatherDTO.parseHumidityAverage(weatherHourHistories);

        WeatherDTO weather = new WeatherDTO();
        weather.setAvgTemperature(temperatureAverage);
        Optional.ofNullable(temperatureRange).ifPresent(range -> {
            weather.setMinTemperature(range.getMinimum());
            weather.setMaxTemperature(range.getMaximum());
        });
        weather.setAvgHumidity(humidityAverage);
        Optional.ofNullable(humidityRange).ifPresent(range -> {
            weather.setMinHumidity(range.getMinimum());
            weather.setMaxHumidity(range.getMaximum());
        });
        weather.setDayText(sunRiseAndSetPairOpt.map(Pair::getLeft).orElse(null));
        weather.setNightText(sunRiseAndSetPairOpt.map(Pair::getRight).orElse(null));
        weather.setDetails(weatherHourHistories);
        weather.setOriginalStationDetails(weatherStationHourHistories);
        return weather;
    }

    /**
     * 查询天气日历史数据
     *
     * @param date 日期
     * @return 天气日历史数据对象
     * @since 7.260102.0
     */
    private @Nullable WeatherDayHistoryDataDTO queryWeatherDayHistory(@NonNull LocalDate date) {
        LocalDateTime beginDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();
        return this.weatherClientService.<WeatherDayHistoryDataDTO>queryUnifier
                        (WeatherQueryTypeEnum.DAY_HISTORY_DATA, beginDateTime, endDateTime).stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 查询气象站数据
     * <p>
     * 返回的 WeatherStationData 包含：
     * <ul>
     *     <li>hourDetails: 按小时合并后的平均数据</li>
     *     <li>temperatureRange: 原始数据的温度范围</li>
     *     <li>humidityRange: 原始数据的湿度范围</li>
     * </ul>
     *
     * @param date 日期
     * @return 天气站数据
     * @since 7.260102.0
     */
    private @NonNull WeatherStationData queryWeatherStation(@NonNull LocalDate date) {
        //查询气象监测站实例id
        String equipId = this.abstractCommonServer.getShellConfig(
                ContextUtil.getGroupCode(), ContextUtil.getProjectId(), null, ShellConfigItemEnum.ALG_WEATHER_DATA_SOURCE.getCode(), null);
        // 获取原始数据并转换为 WeatherDTO.Detail
        List<WeatherDTO.Detail> rawDetails = this.weatherService.getWeatherStationData(
                        date.atStartOfDay(), date.plusDays(1).atStartOfDay(),
                        DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES,equipId).stream()
                .filter(detail -> detail.getTime() != null)
                .map(weather -> {
                    WeatherDTO.Detail weatherDTO = new WeatherDTO.Detail();
                    weatherDTO.setTime(weather.getTime());
                    weatherDTO.setTemperature(weather.getTemperature());
                    weatherDTO.setHumidity(weather.getHumidity());
                    weatherDTO.setWetBulbTemp(weather.getWetBulbTemp());
                    weatherDTO.setEnthalpyValue(weather.getEnthalpyValue());
                    return weatherDTO;
                })
                .collect(Collectors.toList());

        // 从原始数据计算温度和湿度的范围（最大最小值）
        Range<BigDecimal> temperatureRange = WeatherDTO.parseTemperatureRange(rawDetails);
        Range<BigDecimal> humidityRange = WeatherDTO.parseHumidityRange(rawDetails);

        // 按小时分组合并，计算每小时的平均值
        List<WeatherDTO.Detail> hourDetails = rawDetails.stream()
                .collect(Collectors.groupingBy(detail -> DateTimeTypeEnum.HOUR.truncate(detail.getTime())))
                .entrySet().stream()
                .map(entry -> {
                    LocalDateTime hourTime = entry.getKey();
                    List<WeatherDTO.Detail> hourGroupDetails = entry.getValue();

                    WeatherDTO.Detail hourDetail = new WeatherDTO.Detail();
                    hourDetail.setTime(hourTime);
                    hourDetail.setTemperature(this.bizComputeService.calculateAverage(hourGroupDetails.stream()
                            .map(WeatherDTO.Detail::getTemperature)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()), NEW_SCALE));
                    hourDetail.setHumidity(this.bizComputeService.calculateAverage(hourGroupDetails.stream()
                            .map(WeatherDTO.Detail::getHumidity)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()), NEW_SCALE));
                    hourDetail.setWetBulbTemp(this.bizComputeService.calculateAverage(hourGroupDetails.stream()
                            .map(WeatherDTO.Detail::getWetBulbTemp)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()), NEW_SCALE));
                    hourDetail.setEnthalpyValue(this.bizComputeService.calculateAverage(hourGroupDetails.stream()
                            .map(WeatherDTO.Detail::getEnthalpyValue)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()), NEW_SCALE));
                    return hourDetail;
                })
                .sorted(Comparator.comparing(WeatherDTO.Detail::getTime))
                .collect(Collectors.toList());
        //按照时间升序
        rawDetails.sort(Comparator.comparing(WeatherDTO.Detail::getTime));
        return new WeatherStationData(hourDetails,rawDetails,temperatureRange, humidityRange);
    }

    /**
     * 气象站数据结构，包含小时数据和原始数据的温度/湿度范围
     *
     * @author JiYinchuan
     * @since 7.260102.0
     */
    @AllArgsConstructor
    private static class WeatherStationData {

        /**
         * 按小时合并后的平均数据
         */
        final List<WeatherDTO.Detail> hourDetails;

        /**
         * 15min 原始数据的数据
         */
        final List<WeatherDTO.Detail> originalStationDetails;

        /**
         * 原始数据的温度范围
         */
        final Range<BigDecimal> temperatureRange;

        /**
         * 原始数据的湿度范围
         */
        final Range<BigDecimal> humidityRange;


    }

    /**
     * 查询天气小时历史数据
     *
     * @param date 日期
     * @return 天气小时历史数据
     * @since 7.260102.0
     */
    private @NonNull List<WeatherDTO.Detail> queryWeatherHourHistories(@NonNull LocalDate date) {
        return this.weatherClientService.listHourHistoryData(ContextUtil.getProjectId(),
                        date.atStartOfDay(), date.plusDays(1).atStartOfDay()).stream()
                .map(weather -> {
                    WeatherDTO.Detail weatherDTO = new WeatherDTO.Detail();
                    weatherDTO.setTime(weather.getHourTime());
                    weatherDTO.setTemperature(weather.getTemperature());
                    weatherDTO.setHumidity(weather.getHumidity());
                    weatherDTO.setWetBulbTemp(weather.getWetBulbTemp());
                    weatherDTO.setEnthalpyValue(weather.getEnthalpyValue());
                    weatherDTO.setText(weather.getText());
                    return weatherDTO;
                })
                .sorted(Comparator.comparing(WeatherDTO.Detail::getTime))
                .collect(Collectors.toList());
    }

    // endregion

    // region 需求配置

    /**
     * 查询需求模板配置
     *
     * @param date 日期
     * @return 需求模板对象
     * @since 7.260102.0
     */
    public @NonNull Optional<DemandTemplate> queryTemplate(@NonNull LocalDate date) {
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        templateParam.setDate(date);
        Optional<DemandTemplate> templateOpt = demandConfigGateway.getByTemplate(templateParam);
        logProjectInstance().info("查询需求模版配置", Arrays.asList(
                Pair.of("date", date),
                Pair.of("template", templateOpt.orElse(null))
        ));
        return templateOpt;
    }

    /**
     * 查询价值选择配置
     *
     * @param date 日期
     * @return 价值选择对象
     * @since 7.260102.0
     */
    protected @Nullable DemandConfigAirconditioningCustomDTO queryValueSelect(@NonNull LocalDate date) {
        BaseDemandTemplateParam templateParam = new BaseDemandTemplateParam();
        templateParam.setTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        templateParam.setDate(date);
        DemandConfigAirconditioningCustomDTO valueSelect = demandConfigAirconditioningCustomService.getCustomConfig(templateParam);
        logProjectInstance().info("查询价值选择配置", Arrays.asList(
                Pair.of("date", date),
                Pair.of("valueSelect", valueSelect)
        ));
        return valueSelect;
    }

    // endregion

    // region IOT 数据

    /**
     * 查询指定对象的信息点值
     *
     * @param date      日期
     * @param objectIds 对象 ID 集合
     * @param timeType  时间类型
     * @param infoCodes 信息点编码集合
     * @return 信息点值列表
     * @since 7.260201.0
     */
    protected List<InfoCodeValueVO> queryInfoCodeValues(
            @NonNull LocalDate date,
            @NonNull Set<String> objectIds,
            @NonNull DtpRwdPersistenceTimeTypeEnum timeType,
            @NonNull Collection<String> infoCodes) {
        if (CollectionUtils.isEmpty(objectIds) || CollectionUtils.isEmpty(infoCodes)) {
            logProjectInstance().info("对象ID或信息点编码为空，无法查询信息点值", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("objectIds", objectIds),
                    Pair.of("timeType", timeType),
                    Pair.of("infoCodes", infoCodes)
            ));
            return Collections.emptyList();
        }
        return this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                        timeType, objectIds, infoCodes,
                        date.atStartOfDay(), date.plusDays(1).atStartOfDay()).stream()
                .filter(infoCodeValue -> StringUtils.isNotBlank(infoCodeValue.toDataValueString()))
                .collect(Collectors.toList());
    }

    /**
     * 查询系统累计电量
     *
     * @param systemIds 系统 ID 集合
     * @param dates     日期列表
     * @return 日期 -> 系统 ID -> 累计电量
     * @since 7.260102.0
     */
    protected @NonNull Map<LocalDate, Map<String, @NonNull BigDecimal>> queryAccElecConsumByDate(
            @NonNull List<LocalDate> dates,
            @NonNull Set<String> systemIds) {
        if (CollectionUtils.isEmpty(systemIds) || CollectionUtils.isEmpty(dates)) {
            logProjectInstance().info("系统ID或日期为空，无法查询系统累计电量", Arrays.asList(
                    Pair.of("dates", dates),
                    Pair.of("systemIds", systemIds)
            ));
            return Collections.emptyMap();
        }
        Map<LocalDate, Map<String, BigDecimal>> result = new ConcurrentHashMap<>();
        Function<LocalDate, CompletableFuture<Void>> futureFunction = date -> CompletableFuture.runAsync(() -> {
            List<InfoCodeValueVO> infoCodeValues = this.queryInfoCodeValues(date, systemIds,
                    DtpRwdPersistenceTimeTypeEnum.DAY, Sets.newHashSet(InfoCodeEnum.ACC_ELEC_CONSUM.getCode()));
            logProjectInstance().info("查询-系统累计电量", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("systemIds", systemIds),
                    Pair.of("infoCodeValues", infoCodeValues)
            ));
            infoCodeValues.forEach(infoCodeValue -> MapExecutor.dynamicPut(result,
                    infoCodeValue.getDataTime().toLocalDate(),
                    infoCodeValue.getObjectId(),
                    new BigDecimal(infoCodeValue.toDataValueString()),
                    ConcurrentHashMap::new));
        }, BusinessThreadPoolExecutor.ttlExecutor());
        CompletableFuture.allOf(dates.stream().map(futureFunction).toArray(CompletableFuture[]::new)).join();
        return result;
    }

    /**
     * 计算系统耗电量环比
     *
     * @param date                日期
     * @param similarWeekDates    相似周日期列表
     * @param date2SystemId2Value 日期 -> 系统 ID -> 累计电量
     * @return 系统耗电量环比映射
     * @since 7.260102.0
     */
    protected @NonNull Map<String, BigDecimal> calculateMom(
            @NonNull LocalDate date,
            @NonNull List<LocalDate> similarWeekDates,
            @NonNull Map<LocalDate, Map<String, @NonNull BigDecimal>> date2SystemId2Value) {
        if (CollectionUtils.isEmpty(similarWeekDates) || MapUtils.isEmpty(date2SystemId2Value)) {
            return Collections.emptyMap();
        }
        // 系统ID -> 值
        Map<String, @NonNull BigDecimal> systemId2Value = Optional.ofNullable(
                date2SystemId2Value.get(date)).orElse(Collections.emptyMap());
        // 系统ID -> 所有相似周值
        Map<String, @NonNull BigDecimal> systemId2SimilarWeekValueSum = new HashMap<>();
        for (LocalDate similarWeekDate : similarWeekDates) {
            Map<String, BigDecimal> systemId2AccElecConsum = date2SystemId2Value.get(similarWeekDate);
            if (MapUtils.isEmpty(systemId2AccElecConsum) || similarWeekDate.isEqual(date)) {
                continue;
            }
            systemId2AccElecConsum.forEach((systemId, accElecConsum) -> {
                BigDecimal accElecConsumSum = Optional.ofNullable(
                        systemId2SimilarWeekValueSum.get(systemId)).orElse(BigDecimal.ZERO);
                accElecConsumSum = accElecConsumSum.add(accElecConsum);
                systemId2SimilarWeekValueSum.put(systemId, accElecConsumSum);
            });
        }
        // 系统ID -> 环比
        Map<String, BigDecimal> systemId2Mom = new HashMap<>();
        systemId2Value.forEach((systemId, accElecConsum) -> {
            BigDecimal similarWeekValueSum = systemId2SimilarWeekValueSum.get(systemId);
            if (similarWeekValueSum == null || similarWeekValueSum.compareTo(BigDecimal.ZERO) == 0) {
                return;
            }
            // 环比 =（耗电量-对比日耗电量） / 对比日耗电量
            systemId2Mom.put(systemId, accElecConsum.subtract(similarWeekValueSum)
                    .divide(similarWeekValueSum, NEW_SCALE, RoundingMode.HALF_UP));
        });
        return systemId2Mom;
    }

    /**
     * 计算信息点总和
     *
     * @param infoCodeValues 信息点值列表
     * @return 时间 -> 总和
     * @since 7.260201.0
     */
    protected @NonNull Map<LocalDateTime, BigDecimal> calculateInfoCodeSum(
            @NonNull List<InfoCodeValueVO> infoCodeValues) {
        return infoCodeValues.stream().collect(Collectors.groupingBy(
                InfoCodeValueVO::getDataTime,
                Collectors.collectingAndThen(Collectors.toList(), values -> {
                    AtomicReference<BigDecimal> sum = new AtomicReference<>(BigDecimal.ZERO);
                    values.stream()
                            .map(InfoCodeValueVO::toDataValueString)
                            .filter(StringUtils::isNotBlank)
                            .forEach(dataValue -> sum.set(sum.get().add(new BigDecimal(dataValue))));
                    return sum.get();
                })
        ));
    }

    /**
     * 计算信息点平均值
     *
     * @param infoCodeValues 信息点值列表
     * @return 时间 -> 平均值
     * @since 7.260201.0
     */
    protected @NonNull Map<LocalDateTime, BigDecimal> calculateInfoCodeAverage(
            @NonNull List<InfoCodeValueVO> infoCodeValues) {
        return infoCodeValues.stream().collect(Collectors.groupingBy(
                InfoCodeValueVO::getDataTime,
                Collectors.collectingAndThen(Collectors.toList(), values -> {
                    AtomicReference<BigDecimal> sum = new AtomicReference<>(BigDecimal.ZERO);
                    AtomicInteger count = new AtomicInteger(0);
                    values.stream()
                            .map(InfoCodeValueVO::toDataValueString)
                            .filter(StringUtils::isNotBlank)
                            .forEach(dataValue -> {
                                sum.set(sum.get().add(new BigDecimal(dataValue)));
                                count.getAndIncrement();
                            });
                    if (count.get() == 0) {
                        return null;
                    }
                    return sum.get().divide(BigDecimal.valueOf(count.get()), NEW_SCALE, RoundingMode.HALF_UP);
                })
        ));
    }

    // endregion

    // region 数据字典

    /**
     * 查询数据字典-设备类定义
     *
     * @param classCodes 设备类编码集合
     * @return 设备类编码 -> 设备类定义
     * @since 7.260201.0
     */
    protected @NonNull Map<String, ClassCodeDefineModel> queryClassCode2Define(@NonNull Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            logProjectInstance().warn("查询-数据字典-设备类编码为空，无法查询设备类定义", Pair.of("classCodes", classCodes));
            return Collections.emptyMap();
        }
        Map<String, DtpKnowledgeObjectClassCodeResponse> classCode2Define = this.dtpKnowledgeClientService.mapClassCodeAndDefine(false, classCodes);
        return classCode2Define.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                        ClassCodeDefineModel.from(entry.getValue())));
    }

    protected @NonNull Map<String, List<InfoCodeDefineModel>> queryInfoCodeDefines(
            @NonNull Map<String, Set<String>> classCode2InfoCodes) {
        if (MapUtils.isEmpty(classCode2InfoCodes)) {
            return Collections.emptyMap();
        }
        Map<String, List<DtpKnowledgeInfoCodeDTO>> classCode2InfoCodeDefines =
                dtpKnowledgeClientService.mapClassCodeAndInfoCodeDefines(classCode2InfoCodes.keySet());
        if (MapUtils.isEmpty(classCode2InfoCodeDefines)) {
            logProjectInstance().error("查询-未查询到信息点定义", Pair.of("classCodes", classCode2InfoCodes.keySet()));
            return Collections.emptyMap();
        }
        return classCode2InfoCodeDefines.entrySet().stream()
                // 根据入参过滤出符合条件的信息点定义
                .filter(entry -> {
                    Set<String> infoCodes = classCode2InfoCodes.get(entry.getKey());
                    entry.getValue().removeIf(define -> !infoCodes.contains(define.getCode()));
                    return CollectionUtils.isNotEmpty(infoCodes) && CollectionUtils.isNotEmpty(entry.getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(InfoCodeDefineModel::from)
                        .collect(Collectors.toList())));
    }

    /**
     * 查询数据字典-信息点定义
     *
     * @param classCodes 设备类编码集合
     * @return 设备类编码 -> 信息点定义
     * @since 7.260201.0
     */
    public @NonNull Map<String, List<InfoCodeDefineModel>> queryClassCode2InfoCodeDefines(
            @NonNull Collection<String> classCodes) {
        if (CollectionUtils.isEmpty(classCodes)) {
            logProjectInstance().warn("查询-数据字典-设备类编码为空，无法查询信息点定义", Pair.of("classCodes", classCodes));
            return Collections.emptyMap();
        }
        Map<String, List<DtpKnowledgeInfoCodeDTO>> classCode2InfoCodeDefines =
                this.dtpKnowledgeClientService.mapClassCodeAndInfoCodeDefines(classCodes);
        if (MapUtils.isEmpty(classCode2InfoCodeDefines)) {
            logProjectInstance().error("查询-数据字典-未查询到信息点定义", Pair.of("classCodes", classCodes));
            return Collections.emptyMap();
        }
        return classCode2InfoCodeDefines.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, entry -> entry.getValue().stream()
                        .map(InfoCodeDefineModel::from)
                        .collect(Collectors.toList())));
    }

    /**
     * 合并多个设备类下信息点定义数据
     *
     * @param classCode2InfoCodeDefines 设备类编码 -> 信息点定义
     * @return 合并后的设备类编码 -> 信息点定义
     * @since 7.260201.0
     */
    protected @NonNull Map<String, List<InfoCodeDefineModel>> mergeClassCode2InfoCodeDefines(
            @NonNull List<Map<String, List<InfoCodeDefineModel>>> classCode2InfoCodeDefines) {
        if (CollectionUtils.isEmpty(classCode2InfoCodeDefines)) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, InfoCodeDefineModel>> classCode2InfoCode2Define = new LinkedHashMap<>();
        classCode2InfoCodeDefines.forEach(classCode2InfoCodeDefine ->
                classCode2InfoCodeDefine.forEach((classCode, infoCodeDefines) ->
                        infoCodeDefines.forEach(infoCodeDefine -> MapExecutor.dynamicPut(classCode2InfoCode2Define,
                                classCode, infoCodeDefine.getCode(), infoCodeDefine, LinkedHashMap::new))));

        Map<String, List<InfoCodeDefineModel>> finalResult = new LinkedHashMap<>();
        classCode2InfoCode2Define.forEach((classCode, infoCodeDefines) ->
                infoCodeDefines.values().forEach(define -> MapExecutor.dynamicPut(
                        finalResult, classCode, define, ArrayList::new)));
        return finalResult;
    }

    // endregion

    // region 工作历


    /**
     * 查询对象 ID -> 工作时间
     *
     * @param date      日期
     * @param objectIds 对象 ID 集合
     * @return 对象 ID -> 工作时间
     * @since 7.260201.0
     */
    protected @NonNull Map<String, List<Range<LocalTime>>> queryObjectId2WorkTimes(
            @NonNull LocalDate date,
            @NonNull Set<String> objectIds) {
        Map<String, List<Range<LocalTime>>> objectId2WorkTimes = new ConcurrentHashMap<>();
        CompletableFuture.allOf(objectIds.stream().map(objectId -> CompletableFuture.runAsync(() -> {
            MidCalendarWorkTimeQueryParam workTimeQueryParam = new MidCalendarWorkTimeQueryParam();
            workTimeQueryParam.setObjId(objectId);
            workTimeQueryParam.setWorkDateStart(date);
            workTimeQueryParam.setWorkDateEnd(date.plusDays(1));
            List<Range<LocalTime>> workTimes = this.midCalendarClientService.listCalendarWorkTime(workTimeQueryParam).stream()
                    .map(workTime -> Range.between(workTime.getBeginWorkTime(), workTime.getEndWorkTime()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(workTimes)) {
                objectId2WorkTimes.put(objectId, workTimes);
            }
        })).toArray(CompletableFuture[]::new)).join();
        return objectId2WorkTimes;
    }

    // endregion

    // region 空间范围

    /**
     * 查询报告分析的空间范围
     *
     * @param allConfigData 报告配置数据
     * @return 空间范围 ID 集合
     * @since 7.260102.0
     */
    public @NonNull Set<String> querySpaceIds(
            @NonNull LocalDate date,
            @NonNull Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData) {
        if (MapUtils.isEmpty(allConfigData)) {
            return Collections.emptySet();
        }
        Set<String> configSpaceIds = Optional.ofNullable(allConfigData.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENV_QUALITY))
                .filter(CollectionUtils::isNotEmpty)
                .map(config -> ObjectMapperUtils.convertValue(config.get(0), EnvQualityConfig.class).getSpaces())
                .orElse(Collections.emptySet());
        if (CollectionUtils.isEmpty(configSpaceIds)) {
            logProjectInstance().warn("未配置空间，无法查询空间范围", Pair.of("allConfigData", allConfigData));
            return Collections.emptySet();
        }
        DemandSpacesQueryParam spaceQueryParam = new DemandSpacesQueryParam(DemandTemplateTypeEnum.AIRCONDITIONING);
        spaceQueryParam.setDate(date);
        spaceQueryParam.queryOnlyControlled();
        spaceQueryParam.setFilterInfoCodeRules(Sets.newHashSet(InfoCodeEnum.SPACE_TDB.getCode()));
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setId(configSpaceIds);
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        Set<String> spaceIds = spaceQueryParam.queryObject(queryParam).stream()
                .map(DtpRwdObjectDTO::getId)
                .collect(Collectors.toSet());
        // 查询空间温度屏蔽设定信息点值，当查询出来的数据全部为 1 时则移除
        Map<String, List<InfoCodeValueVO>> objectId2InfoCodeValues = this.queryInfoCodeValues(date, spaceIds,
                        DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES,
                        Sets.newHashSet(InfoCodeEnum.ZONE_TEMP_MASK_SET.getCode())).stream()
                .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId, Collectors.toList()));
        spaceIds.removeIf(spaceId -> Optional.ofNullable(objectId2InfoCodeValues.get(spaceId))
                .map(infoCodeValues -> infoCodeValues.stream()
                        .allMatch(infoCodeValue -> infoCodeValue.equalsInfoCodeValue("1")))
                .orElse(false));
        logProjectInstance().info("查询-空间范围", Arrays.asList(
                Pair.of("configSpaceIds", configSpaceIds),
                Pair.of("finalSpaceIds", spaceIds)
        ));
        return spaceIds;
    }

    // endregion

    // region 相似日

    /**
     * 查询相似的上周日期
     *
     * @param templateOpt 需求模板
     * @param date        日期
     * @return 相似的上周日期列表
     * @since 7.260102.0
     */
    protected @NonNull List<LocalDate> querySimilarWeekDates(
            @NonNull Optional<DemandTemplate> templateOpt,
            @NonNull LocalDate date) {
        if (!templateOpt.isPresent()) {
            logInstance().warn("无需求模版，无法查询相似日", Pair.of("date", date));
            return Collections.emptyList();
        }
        String projectId = ContextUtil.getProjectId();
        DemandTemplate template = templateOpt.get();

        // 计算上周时间
        LocalDate thisWeekMonday = date.with(DayOfWeek.MONDAY);
        LocalDate lastWeekMonday = thisWeekMonday.minusWeeks(1);
        LocalDate lastWeekSunday = lastWeekMonday.plusDays(7);

        // 查询当天空调季
        MidCalendarAirSeasonTypeEnum todaySeasonType = this.midCalendarClientService
                .getAirSeasonType(projectId, date);
        logProjectInstance().info("查询-当天空调季", Arrays.asList(
                Pair.of("date", date),
                Pair.of("todaySeasonType", todaySeasonType)
        ));
        Map<LocalDate, MidCalendarAirSeasonTypeEnum> date2SeasonType = this.midCalendarClientService
                .mapAirSeasonType(projectId, lastWeekMonday, lastWeekSunday);
        logProjectInstance().info("查询-上周空调季", Arrays.asList(
                Pair.of("date", date),
                Pair.of("lastWeekMonday", lastWeekMonday),
                Pair.of("lastWeekSunday", lastWeekSunday)
        ));
        // 过滤出和当天空调季匹配的日期
        List<LocalDate> matchDate = date2SeasonType.entrySet().stream()
                .filter(entry -> entry.getValue() == todaySeasonType)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(matchDate)) {
            logProjectInstance().warn("无匹配的空调季", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("todaySeasonType", todaySeasonType),
                    Pair.of("date2SeasonType", date2SeasonType)
            ));
            return Collections.emptyList();
        }

        // 根据模板和指定日期查询匹配的需求日期
        DemandTemplateCalendarQueryDaoParam queryDaoParam = new DemandTemplateCalendarQueryDaoParam();
        queryDaoParam.setDemandTemplateType(DemandTemplateTypeEnum.AIRCONDITIONING);
        queryDaoParam.setWorkDates(matchDate);
        queryDaoParam.setRelDemandTemplateId(template.getId());
        List<DemandTemplateCalendar> templateCalendars = demandTemplateCalendarService.list(queryDaoParam);
        logProjectInstance().info("查询-系统累计电量", Arrays.asList(
                Pair.of("date", date),
                Pair.of("queryDaoParam", queryDaoParam),
                Pair.of("templateCalendars", templateCalendars)
        ));
        matchDate = templateCalendars.stream()
                .map(DemandTemplateCalendar::getWorkDate)
                .collect(Collectors.toList());
        return matchDate;
    }

    // endregion

    // region 空调

    // endregion

    // region 异常记录

    /**
     * 查询异常记录
     *
     * @param date    日期
     * @param objects 对象列表
     * @return 异常记录列表
     * @since 7.260201.0
     */
    protected @NonNull List<ExceptionRecordDTO> queryExceptionRecords(
            @NonNull LocalDate date, @NonNull List<DtpRwdObjectDTO> objects) {
        return this.queryExceptionRecords(date, objects, this.queryExceptionRecordValidInfoCodeDefines(objects));
    }

    /**
     * 查询异常记录
     *
     * @param date                      日期
     * @param objects                   对象列表
     * @param classCode2InfoCodeDefines 设备类编码 -> 信息点定义列表
     * @return 异常记录列表
     * @since 7.260201.0
     */
    protected @NonNull List<ExceptionRecordDTO> queryExceptionRecords(
            @NonNull LocalDate date, @NonNull List<DtpRwdObjectDTO> objects,
            @NonNull Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines) {
        if (CollectionUtils.isEmpty(objects) || MapUtils.isEmpty(classCode2InfoCodeDefines)) {
            return Collections.emptyList();
        }

        // 查询信息点值
        List<InfoCodeValueVO> allInfoCodeValues = this.queryExceptionRecordInfoCodeValues(
                date, objects, classCode2InfoCodeDefines);
        if (CollectionUtils.isEmpty(allInfoCodeValues)) {
            return Collections.emptyList();
        }

        // 构建结果
        return buildExceptionRecords(objects, allInfoCodeValues, classCode2InfoCodeDefines);
    }

    /**
     * 查询有效的信息点定义
     *
     * @param objects 对象列表
     * @return 设备类编码 -> 信息点定义列表
     * @since 7.260201.0
     */
    protected @NonNull Map<String, List<InfoCodeDefineModel>> queryExceptionRecordValidInfoCodeDefines(
            @NonNull List<DtpRwdObjectDTO> objects) {
        Set<String> classCodes = objects.stream()
                .map(DtpRwdObjectDTO::getClassCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(classCodes)) {
            logProjectInstance().error("查询-异常记录-无对象类型编码", Pair.of("objects", objects));
            return Collections.emptyMap();
        }

        // 获取信息点定义
        Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines =
                this.queryClassCode2InfoCodeDefines(classCodes);
        if (MapUtils.isEmpty(classCode2InfoCodeDefines)) {
            logProjectInstance().error("查询-异常记录-未查询到信息点定义", Pair.of("classCodes", classCodes));
            return Collections.emptyMap();
        }
        logProjectInstance().info("查询-异常记录-信息点定义", Arrays.asList(
                Pair.of("classCodes", classCodes),
                Pair.of("classCode2InfoCodeDefines", classCode2InfoCodeDefines)
        ));

        // 仅保留事件消息或故障状态的信息点
        classCode2InfoCodeDefines.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(infoCodeDefine ->
                    !DtpKnowledgeClientService.FIRST_TAG_EVENT_MESSAGE.equals(infoCodeDefine.getFirstTag())
                            && !InfoCodeEnum.FAULT_STATUS.getCode().equals(infoCodeDefine.getCode()));
            return CollectionUtils.isEmpty(entry.getValue());
        });
        if (MapUtils.isEmpty(classCode2InfoCodeDefines)) {
            return Collections.emptyMap();
        }

        // 查询存在规则的信息点
        Map<String, Set<String>> classCode2ObjectIds = objects.stream()
                .collect(Collectors.groupingBy(
                        DtpRwdObjectDTO::getClassCode,
                        Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));
        Set<String> allInfoCodes = classCode2InfoCodeDefines.values().stream()
                .flatMap(Collection::stream)
                .map(InfoCodeDefineModel::getCode)
                .collect(Collectors.toSet());
        Map<String, Set<String>> objectId2ExistsRuleInfoCodes = abstractIotDataCacheServer
                .mapObjectIdAndExistsRuleInfoCodes(classCode2ObjectIds, allInfoCodes);
        if (MapUtils.isEmpty(objectId2ExistsRuleInfoCodes)) {
            logProjectInstance().warn("查询-异常记录-无对象信息点规则", Arrays.asList(
                    Pair.of("classCode2ObjectIds", classCode2ObjectIds),
                    Pair.of("allInfoCodes", allInfoCodes)
            ));
            return Collections.emptyMap();
        }
        logProjectInstance().info("查询-异常记录-信息点规则", Arrays.asList(
                Pair.of("classCode2ObjectIds", classCode2ObjectIds),
                Pair.of("allInfoCodes", allInfoCodes),
                Pair.of("objectId2ExistsRuleInfoCodes", objectId2ExistsRuleInfoCodes)
        ));

        // 过滤不存在规则的信息点
        classCode2InfoCodeDefines.entrySet().removeIf(entry -> {
            Set<String> existsRuleInfoCodes = classCode2ObjectIds.get(entry.getKey()).stream()
                    .map(objectId2ExistsRuleInfoCodes::get)
                    .filter(CollectionUtils::isNotEmpty)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            if (CollectionUtils.isEmpty(existsRuleInfoCodes)) {
                return true;
            }

            entry.getValue().removeIf(infoCodeDefine ->
                    !existsRuleInfoCodes.contains(infoCodeDefine.getCode()));
            return CollectionUtils.isEmpty(entry.getValue());
        });

        return classCode2InfoCodeDefines;
    }

    /**
     * 查询信息点值数据
     *
     * @param date                      日期
     * @param objects                   对象列表
     * @param classCode2InfoCodeDefines 信息点定义映射
     * @return 信息点值列表
     * @since 7.260201.0
     */
    private @NonNull List<InfoCodeValueVO> queryExceptionRecordInfoCodeValues(
            @NonNull LocalDate date,
            @NonNull List<DtpRwdObjectDTO> objects,
            @NonNull Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines) {

        LocalDateTime today = date.atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);

        // 设备类编码 -> 对象ID
        Map<String, Set<String>> classCode2ObjectIds = objects.stream()
                .collect(Collectors.groupingBy(
                        DtpRwdObjectDTO::getClassCode,
                        Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toSet())));

        Map<String, Map<String, InfoCodeDefineModel>> classCode2InfoCode2Define = new HashMap<>();
        classCode2InfoCodeDefines.forEach((classCode, defines) ->
                classCode2InfoCode2Define.put(classCode, defines.stream().collect(Collectors.toMap(
                        InfoCodeDefineModel::getCode, Function.identity(),
                        (v1, v2) -> v2))));

        List<InfoCodeValueVO> allInfoCodeValues = new ArrayList<>();

        // 遍历每个类型编码，批量查询信息点值
        classCode2ObjectIds.forEach((classCode, objectIds) -> {
            List<InfoCodeDefineModel> infoCodeDefines = classCode2InfoCodeDefines.get(classCode);
            if (CollectionUtils.isEmpty(infoCodeDefines)) {
                return;
            }

            Set<String> infoCodes = infoCodeDefines.stream()
                    .map(InfoCodeDefineModel::getCode)
                    .collect(Collectors.toSet());

            // 批量查询信息点值
            List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueByCloud(
                    DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES, objectIds, infoCodes, today, tomorrow);
            if (CollectionUtils.isEmpty(infoCodeValues)) {
                logProjectInstance().warn("查询-异常记录-无IOT值", Arrays.asList(
                        Pair.of("date", date),
                        Pair.of("objectIds", objectIds),
                        Pair.of("infoCodes", infoCodes)
                ));
                return;
            }
            logProjectInstance().info("查询-异常记录-IOT值", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("objectIds", objectIds),
                    Pair.of("infoCodes", infoCodes),
                    Pair.of("infoCodeValues", infoCodeValues)
            ));

            for (InfoCodeValueVO infoCodeValue : infoCodeValues) {
                Optional.ofNullable(classCode2InfoCode2Define.get(classCode))
                        .map(infoCode2Define -> infoCode2Define.get(infoCodeValue.getInfoCode()))
                        .ifPresent(infoCodeValue::fillInfoCodeDefine);
            }
            allInfoCodeValues.addAll(infoCodeValues);
        });
        return allInfoCodeValues;
    }

    /**
     * 构建异常记录
     *
     * @param objects                   对象列表
     * @param allInfoCodeValues         所有信息点值
     * @param classCode2InfoCodeDefines 信息点定义映射
     * @return 异常记录列表
     * @since 7.260201.0
     */
    private @NonNull List<ExceptionRecordDTO> buildExceptionRecords(
            @NonNull List<DtpRwdObjectDTO> objects,
            @NonNull List<InfoCodeValueVO> allInfoCodeValues,
            @NonNull Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines) {
        if (CollectionUtils.isEmpty(allInfoCodeValues)) {
            return Collections.emptyList();
        }

        Map<String, DtpRwdObjectDTO> objectId2Data = DtpRwdObjectDTO.mapIdAndData(objects);

        // 将对象ID和信息点编码进行分组，每个对象的每个信息点单独处理
        Map<String, List<InfoCodeValueVO>> groupedValues = allInfoCodeValues.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getObjectId() + "_" + v.getInfoCode(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<ExceptionRecordDTO> exceptionRecords = new ArrayList<>();

        groupedValues.forEach((key, infoCodeValues) -> {
            // IOT 查询的是 15 分钟精度数据；同一个对象、同一个信息点、同一个时间点只应保留一个状态值。
            // 如果底层返回了重复时间点，按查询结果的后值覆盖前值，避免同一采样点重复参与异常区间计算。
            List<InfoCodeValueVO> orderedInfoCodeValues = infoCodeValues.stream()
                    .sorted(Comparator.comparing(InfoCodeValueVO::getDataTime))
                    .collect(Collectors.collectingAndThen(Collectors.toMap(
                            InfoCodeValueVO::getDataTime,
                            Function.identity(),
                            (v1, v2) -> v2,
                            LinkedHashMap::new), valueMap -> new ArrayList<>(valueMap.values())));
            for (int i = 0; i < orderedInfoCodeValues.size(); i++) {
                InfoCodeValueVO infoCodeValue = orderedInfoCodeValues.get(i);
                String objectId = infoCodeValue.getObjectId();
                String infoCode = infoCodeValue.getInfoCode();
                LocalDateTime dataTime = infoCodeValue.getDataTime();
                String dataValue = infoCodeValue.toDataValueString();

                DtpRwdObjectDTO object = objectId2Data.get(objectId);
                if (object == null) {
                    logProjectInstance().error("查询-异常记录-无匹配对象", Arrays.asList(
                            Pair.of("objectId", objectId),
                            Pair.of("dataValue", dataValue)
                    ));
                    continue;
                }

                // 获取信息点定义，根据 dataValue 找到对应的数据源，判断其 name 是否为恢复状态
                Optional<InfoCodeDefineModel> infoCodeDefineOpt = Optional.ofNullable(this.getInfoCodeDefine(
                        classCode2InfoCodeDefines, object.getClassCode(), infoCode));
                Optional<InfoCodeDataSourceDefineModel> dataSourceOpt = infoCodeDefineOpt.map(
                        infoCodeDefine -> this.getDataSourceByCode(infoCodeDefine, dataValue));

                // 判断是否为恢复状态：name 等于 "正常"、"Normal"、"其他"、"Other"
                if (this.isRecoverStatus(dataSourceOpt.map(InfoCodeDataSourceDefineModel::getName).orElse(null))) {
                    continue;
                }

                // 找到第一个非恢复状态时，认为异常从当前 15 分钟采样点开始。
                // 后续连续非恢复状态仍属于同一次异常，不再重复生成记录。
                ExceptionRecordDTO exceptionRecord = this.buildExceptionRecord(dataTime, object, infoCode, dataValue);
                for (int j = i + 1; j < orderedInfoCodeValues.size(); j++) {
                    InfoCodeValueVO nextInfoCodeValue = orderedInfoCodeValues.get(j);
                    String nextDataValue = nextInfoCodeValue.toDataValueString();
                    Optional<InfoCodeDataSourceDefineModel> nextDataSourceOpt = infoCodeDefineOpt.map(
                            infoCodeDefine -> this.getDataSourceByCode(infoCodeDefine, nextDataValue));
                    // 当前异常的恢复时间，是触发后第一个恢复状态采样点的时间。
                    if (this.isRecoverStatus(nextDataSourceOpt.map(InfoCodeDataSourceDefineModel::getName).orElse(null))) {
                        exceptionRecord.setRecoverTime(nextInfoCodeValue.getDataTime());
                        i = j;
                        break;
                    }
                }
                exceptionRecords.add(exceptionRecord);
                if (exceptionRecord.getRecoverTime() == null) {
                    // 截止查询窗口结束仍未恢复时，本信息点后续采样点都属于同一条未恢复异常，直接结束本组扫描。
                    break;
                }
            }
        });
        exceptionRecords.sort(Comparator.comparing(ExceptionRecordDTO::getTriggerTime));
        return exceptionRecords;
    }

    /**
     * 根据连续异常区间的第一个异常采样点构建异常记录
     *
     * @param triggerTime   触发时间
     * @param object        对象
     * @param infoCode      信息点编码
     * @param infoCodeValue 信息点值
     * @return 异常记录
     * @since 7.260601.0
     */
    private @NonNull ExceptionRecordDTO buildExceptionRecord(
            @NonNull LocalDateTime triggerTime,
            @NonNull DtpRwdObjectDTO object,
            @NonNull String infoCode,
            @Nullable String infoCodeValue) {
        ExceptionRecordDTO exceptionRecord = new ExceptionRecordDTO();
        exceptionRecord.setTriggerTime(triggerTime);
        exceptionRecord.setObject(object);
        exceptionRecord.setInfoCode(infoCode);
        exceptionRecord.setInfoCodeValue(infoCodeValue);
        return exceptionRecord;
    }

    // endregion

    /**
     * 根据设备类编码和信息点编码获取信息点定义
     *
     * @param classCode2InfoCodeDefines 设备类编码 -> 信息点定义列表
     * @param classCode                 设备类编码
     * @param infoCode                  信息点编码
     * @return 信息点定义，未找到返回 null
     * @since 7.260201.0
     */
    private @Nullable InfoCodeDefineModel getInfoCodeDefine(
            @NonNull Map<String, List<InfoCodeDefineModel>> classCode2InfoCodeDefines,
            @Nullable String classCode,
            @NonNull String infoCode) {
        if (StringUtils.isBlank(classCode)) {
            return null;
        }
        List<InfoCodeDefineModel> infoCodeDefines = classCode2InfoCodeDefines.get(classCode);
        if (CollectionUtils.isEmpty(infoCodeDefines)) {
            return null;
        }
        return infoCodeDefines.stream()
                .filter(define -> infoCode.equals(define.getCode()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据信息点值编码获取数据源定义
     *
     * @param infoCodeDefine 信息点定义
     * @param dataValue      信息点值编码
     * @return 数据源定义，未找到返回 null
     * @since 7.260201.0
     */
    private @Nullable InfoCodeDataSourceDefineModel getDataSourceByCode(
            @NonNull InfoCodeDefineModel infoCodeDefine,
            @Nullable String dataValue) {
        if (StringUtils.isBlank(dataValue)) {
            return null;
        }
        List<InfoCodeDataSourceDefineModel> dataSources = infoCodeDefine.getDataSources();
        if (CollectionUtils.isEmpty(dataSources)) {
            return null;
        }
        return dataSources.stream()
                .filter(dataSource -> dataValue.equals(dataSource.getCode()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断是否为恢复状态
     *
     * <p>当信息点名称等于 "正常"、"Normal"、"其他"、"Other" 时，视为恢复状态</p>
     *
     * @param infoCodeName 信息点名称
     * @return 是否为恢复状态
     * @since 7.260201.0
     */
    private boolean isRecoverStatus(@Nullable String infoCodeName) {
        return "正常".equals(infoCodeName)
                || "Normal".equals(infoCodeName)
                || "其他".equals(infoCodeName)
                || "Other".equals(infoCodeName);
    }
}
