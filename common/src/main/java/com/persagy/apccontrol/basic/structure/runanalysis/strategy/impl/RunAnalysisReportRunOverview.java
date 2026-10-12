package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.DemandConfigAirconditioningCustomDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.BasicsAnalysisParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.SpaceSummaryVo;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunOverviewEnergyAnalysisDto;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunOverviewEnergyBudgetDto;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.runanalysis.RunAnalysisService;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.common.WeatherDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.runoverview.RunOverviewDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.runoverview.SpaceEnvironmentalQualityDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.runoverview.SystemRunningResultDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 运行概述策略
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportRunOverview extends AbstractRunAnalysisReportStrategy<RunOverviewDTO> {

    private final DtpRwdClientService dtpRwdClientService;
    private final RunAnalysisService runAnalysisService;
    private final RunAnalysisReportEnergyBudget runAnalysisReportEnergyBudget;
    private final RunAnalysisReportEnergyAnalysis runAnalysisReportEnergyAnalysis;

    @Override
    public Boolean isSupport(@NonNull RunAnalysisReportTypeEnum type) {
        return type == RunAnalysisReportTypeEnum.DAILY;
    }

    @Override
    public RunOverviewDTO buildWithConfig(
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
        // 查询项目用电量
        CompletableFuture<Optional<RunOverviewEnergyAnalysisDto>> projectElectricityFuture = CompletableFuture.supplyAsync(
                () -> Optional.ofNullable(this.runAnalysisReportEnergyAnalysis.runOverviewEnergyAnalysisCalculate(allConfigData, date)),
                BusinessThreadPoolExecutor.ttlExecutor());

        // 查询当日预算使用率
        CompletableFuture<Optional<RunOverviewEnergyBudgetDto>> dayBudgetUtilizationRateFuture = CompletableFuture.supplyAsync(
                () -> Optional.ofNullable(this.runAnalysisReportEnergyBudget.runOverviewEnergyBudgetCalculate(allConfigData, date)),
                BusinessThreadPoolExecutor.ttlExecutor());

        // 查询天气
        CompletableFuture<WeatherDTO> weatherFuture = CompletableFuture.supplyAsync(
                () -> this.queryWeather(date), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询需求模板
        CompletableFuture<Optional<DemandTemplate>> templateFuture = CompletableFuture.supplyAsync(
                () -> this.queryTemplate(date), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询价值选择
        CompletableFuture<DemandConfigAirconditioningCustomDTO> customConfigFuture = CompletableFuture.supplyAsync(
                () -> this.queryValueSelect(date), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询空间ID
        CompletableFuture<Set<String>> spaceIdsFuture = CompletableFuture.supplyAsync(
                () -> this.querySpaceIds(date, allConfigData), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询空间环境品质
        CompletableFuture<@Nullable SpaceEnvironmentalQualityDTO> spaceEnvironmentalQualityFuture = this.hasChapterConfig(
                allConfigData, RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENV_QUALITY)
                ? spaceIdsFuture.thenApplyAsync(
                spaceIds -> this.querySpaceEnvironmentalQuality(date, spaceIds), BusinessThreadPoolExecutor.ttlExecutor())
                : CompletableFuture.completedFuture(null);

        // 查询系统运行结果
        CompletableFuture<@Nullable List<SystemRunningResultDTO>> systemRunningResultsFuture = templateFuture.thenCombineAsync(
                spaceIdsFuture, (templateOpt, spaceIds) -> this.querySystemRunningResults(
                        date, templateOpt, spaceIds, allConfigData), BusinessThreadPoolExecutor.ttlExecutor());

        CompletableFuture.allOf(weatherFuture, templateFuture, customConfigFuture, spaceIdsFuture,
                spaceEnvironmentalQualityFuture, systemRunningResultsFuture).get();

        RunOverviewDTO result = new RunOverviewDTO();
        result.setProjectElectricity(projectElectricityFuture.get().orElse(null));
        result.setDayBudgetUtilizationRate(dayBudgetUtilizationRateFuture.get().orElse(null));
        result.setWeather(weatherFuture.get());
        result.setTemplateName(templateFuture.get().map(DemandTemplate::getDemandTemplateName).orElse(null));
        result.setValueSelectType(Optional.ofNullable(customConfigFuture.get())
                .map(DemandConfigAirconditioningCustomDTO::getValueSelectType)
                .map(BasicsConfigTypeEnum::getCode)
                .orElse(null));
        result.setSpaceEnvironmentalQuality(spaceEnvironmentalQualityFuture.get());
        result.setSystemRunningResults(systemRunningResultsFuture.get());
        logInstance().info("构建-报告完成", Arrays.asList(
                Pair.of("date", date),
                Pair.of("costMs", System.currentTimeMillis() - startTime),
                Pair.of("result", result)
        ));
        return result;
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_RUN_OVERVIEW;
    }

    /**
     * 判断章节配置是否存在。
     *
     * @param allConfigData 报告章节配置
     * @param chapter       章节
     * @return 章节是否已勾选并存在配置
     * @since 7.260601.0
     */
    private boolean hasChapterConfig(
            @NonNull Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData,
            @NonNull RunAnalysisReportChapterEnum chapter) {
        return MapUtils.isNotEmpty(allConfigData) && CollectionUtils.isNotEmpty(allConfigData.get(chapter));
    }

    /**
     * 查询运行概述需要展示的冷站系统 ID。
     *
     * @param allConfigData 报告章节配置
     * @return 已勾选的冷站系统实例 ID
     * @since 7.260601.0
     */
    private @NonNull Set<String> queryConfiguredCoolingSystemIds(
            @NonNull Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData) {
        if (MapUtils.isEmpty(allConfigData)) {
            return Collections.emptySet();
        }
        return Optional.ofNullable(allConfigData.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN_ITEM))
                .orElseGet(Collections::emptyList).stream()
                .map(RunAnalysisReportConfig::getObjectId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    /**
     * 查询空间环境品质
     *
     * @param date     日期
     * @param spaceIds 空间 ID 集合
     * @return 空间运行结果，配置后无有效空间范围或无汇总数据时返回空对象
     * @since 7.260102.0
     */
    private @NonNull SpaceEnvironmentalQualityDTO querySpaceEnvironmentalQuality(
            @NonNull LocalDate date,
            @NonNull Set<String> spaceIds) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            logProjectInstance().warn("未配置有效空间范围，无法查询空间环境品质", Pair.of("date", date));
            return SpaceEnvironmentalQualityDTO.empty();
        }
        BasicsAnalysisParam analysisParam = new BasicsAnalysisParam();
        analysisParam.setType(RunningMonitorSecondTypeEnum.RUN_QUALIFY_TDB);
        analysisParam.setInstanceIds(new ArrayList<>(spaceIds));
        analysisParam.setInstanceType(InstanceTypeEnum.SPACE);
        analysisParam.setDate(date);
        SpaceSummaryVo spaceSummary = runAnalysisService.getSpaceSummary(analysisParam);
        logProjectInstance().info("查询-空间环境品质", Arrays.asList(
                Pair.of("date", date),
                Pair.of("spaceIds", spaceIds),
                Pair.of("spaceSummary", spaceSummary)
        ));
        if (spaceSummary == null) {
            return SpaceEnvironmentalQualityDTO.empty();
        }
        SpaceEnvironmentalQualityDTO result = new SpaceEnvironmentalQualityDTO();
        result.setTdbComplianceRate(spaceSummary.getSatisfyPercentage());
        result.setTagCount(Optional.ofNullable(spaceSummary.getTagCount()).orElseGet(Collections::emptyMap));
        return result;
    }

    /**
     * 查询系统运行结果
     *
     * @param date          日期
     * @param templateOpt   模板
     * @param spaceIds      空间 ID 集合
     * @param allConfigData 报告章节配置
     * @return 系统运行结果，无已配置或有效系统时为 null
     * @since 7.260102.0
     */
    private @Nullable List<SystemRunningResultDTO> querySystemRunningResults(
            @NonNull LocalDate date,
            @NonNull Optional<DemandTemplate> templateOpt,
            @NonNull Set<String> spaceIds,
            @NonNull Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData) {
        // 查询系统名称
        Set<String> configuredSystemIds = this.queryConfiguredCoolingSystemIds(allConfigData);
        if (CollectionUtils.isEmpty(configuredSystemIds)) {
            logProjectInstance().warn("未配置冷站系统运行实例，无法查询系统运行结果", Pair.of("date", date));
            return null;
        }
        Map<String, String> systemId2LocalName = dtpRwdClientService.listObject(new DtpRwdObjectQueryParam()
                        .setId(configuredSystemIds)
                        .setClassCodeEnum(DtpRwdClassCodeEnum.ACAC_SYSTEM)
                        .filterMonitoringSurveillanceStatus()
                        .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName)).stream()
                .collect(Collectors.toMap(DtpRwdObjectDTO::getId, DtpRwdObjectDTO::getLocalName));
        if (MapUtils.isEmpty(systemId2LocalName)) {
            logProjectInstance().warn("无有效 ACAC 系统，无法查询系统运行结果", Pair.of("date", date));
            return null;
        }
        Set<String> systemIds = systemId2LocalName.keySet();

        // 查询系统运行时间
        Map<String, List<Range<ChronoLocalDateTime<?>>>> systemId2RunningTimes = this.querySystemId2RunningTimes(date, systemIds);

        // 查询系统服务空间达标率
        Map<String, String> systemId2TdbComplianceRate = this.querySpaceTdbComplianceRate(date, systemIds, spaceIds);

        // 查询系统累计电量
        List<LocalDate> similarWeekDates = this.querySimilarWeekDates(templateOpt, date);
        List<LocalDate> queryDates = new ArrayList<>(similarWeekDates);
        queryDates.add(date);
        Map<LocalDate, Map<String, @NonNull BigDecimal>> date2SystemId2AccElecConsum =
                this.queryAccElecConsumByDate(queryDates, systemIds);

        // 计算系统耗电量环比
        Map<String, @NonNull BigDecimal> systemId2AccElecConsumMom = this.calculateMom(
                date, similarWeekDates, date2SystemId2AccElecConsum);

        return systemId2LocalName.entrySet().stream()
                .map(entry -> {
                    String systemId = entry.getKey();
                    SystemRunningResultDTO result = new SystemRunningResultDTO();
                    result.setId(systemId);
                    result.setLocalName(entry.getValue());
                    result.setSystemRunningTimes(systemId2RunningTimes.getOrDefault(systemId, Collections.emptyList()));
                    result.setServiceSpaceSatisfyPercentage(Optional.ofNullable(systemId2TdbComplianceRate.get(systemId)).map(BigDecimal::new).orElse(null));
                    result.setDayAccElecConsum(MapExecutor.dynamicGet(date2SystemId2AccElecConsum, date, systemId));
                    result.setAccElecConsumMom(systemId2AccElecConsumMom.get(systemId));
                    return result;
                }).collect(Collectors.toList());
    }

    /**
     * 查询系统服务空间达标率
     *
     * @param date      日期
     * @param systemIds 系统 ID 集合
     * @param spaceIds  全量空间 ID 集合
     * @return 系统ID -> 服务空间达标率
     * @since 7.260201.0
     */
    private @NonNull Map<String, String> querySpaceTdbComplianceRate(
            @NonNull LocalDate date,
            @NonNull Set<String> systemIds,
            @NonNull Set<String> spaceIds) {
        // 查询系统服务空间
        SpaceQueryParam spaceQueryParam = new SpaceQueryParam();
        spaceQueryParam.setFilterSpaceServiceSystemIds(systemIds);
        spaceQueryParam.setFilterObjectIds(spaceIds);
        List<DtpRwdSpaceDTO> serviceSystemSpaceIds = spaceQueryParam.queryObject(new DtpRwdObjectQueryParam()
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds));
        Map<String, Set<String>> systemId2SpaceIds = DtpRwdObjectDTO.mapRelObjIdAndIds(serviceSystemSpaceIds);
        logProjectInstance().info("查询-系统服务空间", Arrays.asList(
                Pair.of("systemIds", systemIds),
                Pair.of("spaceIds", spaceIds),
                Pair.of("systemId2SpaceIds", systemId2SpaceIds)
        ));
        // 计算服务空间达标率
        Map<String, String> systemId2TdbComplianceRate = new ConcurrentHashMap<>();
        if (MapUtils.isNotEmpty(systemId2SpaceIds)) {
            List<CompletableFuture<Void>> futures = systemId2SpaceIds.entrySet().stream()
                    .map(entry -> CompletableFuture.runAsync(() -> {
                        SpaceEnvironmentalQualityDTO qualityDTO = this.querySpaceEnvironmentalQuality(date, entry.getValue());
                        if (qualityDTO != null && qualityDTO.getTdbComplianceRate() != null) {
                            systemId2TdbComplianceRate.put(entry.getKey(), qualityDTO.getTdbComplianceRate());
                        }
                    }, BusinessThreadPoolExecutor.ttlExecutor()))
                    .collect(Collectors.toList());
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
            } catch (Exception e) {
                logProjectInstance().error("查询-服务空间达标率失败", e,
                        Pair.of("systemId2SpaceIds", systemId2SpaceIds));
            }
        }
        return systemId2TdbComplianceRate;
    }

    /**
     * 查询系统运行时间
     *
     * @param systemIds 系统 ID 集合
     * @param date      日期
     * @return 系统运行时间
     * @since 7.260102.0
     */
    private @NonNull Map<String, List<Range<ChronoLocalDateTime<?>>>> querySystemId2RunningTimes(
            @NonNull LocalDate date, @NonNull Set<String> systemIds) {
        LocalDateTime today = date.atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);
        List<InfoCodeValueVO> infoCodeValues = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIVE_MINUTES, systemIds,
                Sets.newHashSet(InfoCodeEnum.COOLING_SUPPLY_SWITCH_SET.getCode()),
                today, tomorrow);
        logProjectInstance().info("查询-系统供冷开关设定15min数据", Arrays.asList(
                Pair.of("date", date),
                Pair.of("systemIds", systemIds),
                Pair.of("infoCodeValues", infoCodeValues)
        ));

        Map<String, List<Range<ChronoLocalDateTime<?>>>> systemId2RunningTimes = new HashMap<>();
        if (CollectionUtils.isNotEmpty(infoCodeValues)) {
            Map<String, List<InfoCodeValueVO>> systemId2Values = infoCodeValues.stream()
                    .filter(vo -> vo.getDataTime() != null)
                    .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId));

            systemId2Values.forEach((systemId, values) -> {
                values.sort(Comparator.comparing(InfoCodeValueVO::getDataTime));
                List<Range<ChronoLocalDateTime<?>>> ranges = new ArrayList<>();
                LocalDateTime startRunTime = null;
                for (InfoCodeValueVO vo : values) {
                    if (vo.equalsInfoCodeValue("1")) {
                        if (startRunTime == null) {
                            startRunTime = vo.getDataTime();
                        }
                    } else {
                        if (startRunTime != null) {
                            ranges.add(Range.between(startRunTime, vo.getDataTime()));
                            startRunTime = null;
                        }
                    }
                }
                if (startRunTime != null) {
                    ranges.add(Range.between(startRunTime, tomorrow));
                }
                systemId2RunningTimes.put(systemId, ranges);
            });
        }
        return systemId2RunningTimes;
    }

    @Override
    public String logTag() {
        return "运行分析报告-运行概述";
    }
}
