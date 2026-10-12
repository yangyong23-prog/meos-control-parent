package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.ValueSelectQueryParam;
import com.persagy.apccontrol.basic.model.param.midcalendar.MidCalendarWorkTimeBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.midcalendar.MidCalendarWorkTimeVO;
import com.persagy.apccontrol.basic.service.demand.airconditioning.ValueSelectionService;
import com.persagy.apccontrol.basic.service.midcalendar.MidCalendarService;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.collingtemptarget.CoolingTempTargetDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 空间温度目标分析策略
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportCoolingTempTarget extends AbstractRunAnalysisReportStrategy<CoolingTempTargetDTO> {

    private final MidCalendarService midCalendarService;
    private final ValueSelectionService valueSelectionService;

    @Override
    public Boolean isSupport(@NonNull RunAnalysisReportTypeEnum type) {
        return type == RunAnalysisReportTypeEnum.DAILY;
    }

    @Override
    public CoolingTempTargetDTO buildWithConfig(
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
        // 查询价值选择配置
        CompletableFuture<JSONObject> currentTempAndCarbonRateFuture = CompletableFuture.supplyAsync(() ->
                this.queryCurrentTempAndCarbonRate(date), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询空间ID
        CompletableFuture<Set<String>> spaceIdsFuture = CompletableFuture.supplyAsync(() ->
                this.querySpaceIds(date, allConfigData), BusinessThreadPoolExecutor.ttlExecutor());

        // 查询空间营业时间
        CompletableFuture<Map<String, List<Range<LocalTime>>>> spaceId2OpeningTimeFuture = spaceIdsFuture.thenApplyAsync(
                spaceIds -> this.querySpaceId2OpeningTime(spaceIds, date), BusinessThreadPoolExecutor.ttlExecutor());

        // 计算温度孪生控制目标平均值
        CompletableFuture<Map<LocalDateTime, BigDecimal>> tempTwinControlAimFuture = spaceId2OpeningTimeFuture.thenApplyAsync(
                spaceId2OpeningTime -> this.calculateInfoCodeAverageByOpeningTime(
                        date, spaceId2OpeningTime, InfoCodeEnum.TEMP_TWIN_CONTROL_AIM), BusinessThreadPoolExecutor.ttlExecutor());

        // 计算空气（干球）温度平均值
        CompletableFuture<Map<LocalDateTime, BigDecimal>> tdbFuture = spaceId2OpeningTimeFuture.thenApplyAsync(
                spaceId2OpeningTime -> this.calculateInfoCodeAverageByOpeningTime(
                        date, spaceId2OpeningTime, InfoCodeEnum.SPACE_TDB), BusinessThreadPoolExecutor.ttlExecutor());

        CompletableFuture.allOf(currentTempAndCarbonRateFuture, tempTwinControlAimFuture, tdbFuture).get();

        CoolingTempTargetDTO coolingAnalysis = new CoolingTempTargetDTO();
        coolingAnalysis.setCurrentTempAndCarbonRate(currentTempAndCarbonRateFuture.get());
        coolingAnalysis.setDateTime2TempTwinControlAim(tempTwinControlAimFuture.get());
        coolingAnalysis.setDateTimeTdb(tdbFuture.get());
        logInstance().info("构建-报告完成", Arrays.asList(
                Pair.of("date", date),
                Pair.of("costMs", System.currentTimeMillis() - startTime),
                Pair.of("result", coolingAnalysis)
        ));
        return coolingAnalysis;
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_TEMP_TARGET;
    }

    /**
     * 查询当前温度和碳因子
     *
     * @param date 日期
     * @return JSONObject
     * @since 7.251202.0
     */
    private JSONObject queryCurrentTempAndCarbonRate(@NonNull LocalDate date) {
        ValueSelectQueryParam valueSelectQueryParam = new ValueSelectQueryParam();
        valueSelectQueryParam.setProjectId(ContextUtil.getProjectId());
        valueSelectQueryParam.setDate(date);
        valueSelectQueryParam.fillIpTime(date.atStartOfDay());
        valueSelectQueryParam.fillEndTime(date.plusDays(1).atStartOfDay());
        JSONObject jsonObject = valueSelectionService.currentTempAndCarbonRate(valueSelectQueryParam);
        logProjectInstance().info("查询-当前温度和碳因子", Arrays.asList(
                Pair.of("queryParam", valueSelectQueryParam),
                Pair.of("jsonObject", jsonObject)
        ));
        return jsonObject;
    }

    /**
     * 根据营业时间计算信息点平均值
     *
     * @param date                日期
     * @param spaceId2OpeningTime 空间营业时间
     * @param infoCode            信息点
     * @return 时间 -> 平均温度
     * @since 7.260102.0
     */
    private @NonNull Map<LocalDateTime, BigDecimal> calculateInfoCodeAverageByOpeningTime(
            @NonNull LocalDate date,
            @NonNull Map<String, List<Range<LocalTime>>> spaceId2OpeningTime,
            @NonNull InfoCodeEnum infoCode) {
        if (MapUtils.isEmpty(spaceId2OpeningTime)) {
            logProjectInstance().warn("无空间营业时间，无法计算信息点平均值", Arrays.asList(
                    Pair.of("date", date),
                    Pair.of("infoCode", infoCode)
            ));
            return Collections.emptyMap();
        }
        LocalDateTime today = date.atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);
        Set<String> spaceIds = spaceId2OpeningTime.keySet();
        Set<String> infoCodes = Sets.newHashSet(infoCode.getCode());
        List<InfoCodeValueVO> infoCodeValues = abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.FIFTEEN_MINUTES, spaceIds, infoCodes, today, tomorrow);
        infoCodeValues.removeIf(infoCodeValue -> {
            String objectId = infoCodeValue.getObjectId();
            LocalTime dateTime = infoCodeValue.getDataTime().toLocalTime();
            List<Range<LocalTime>> openingTimes = spaceId2OpeningTime.get(objectId);
            if (CollectionUtils.isEmpty(openingTimes)) {
                return true;
            }
            // 数据时间必须在营业时间中，营业时间结束区间作为开区间处理
            return openingTimes.stream().noneMatch(range -> Range.between(range.getMinimum(),
                    range.getMaximum().minusSeconds(1)).contains(dateTime));
        });
        Map<LocalDateTime, BigDecimal> dateTime2Average = this.calculateInfoCodeAverage(infoCodeValues);
        logProjectInstance().info("计算-信息点平均值", Arrays.asList(
                Pair.of("date", date),
                Pair.of("spaceIds", spaceIds),
                Pair.of("infoCodes", infoCodes),
                Pair.of("infoCodeValues", infoCodeValues),
                Pair.of("dateTime2Average", dateTime2Average)
        ));
        return dateTime2Average;
    }

    /**
     * 查询空间营业时间
     *
     * @param spaceIds 空间 ID 集合
     * @param date     日期
     * @return 空间ID -> 营业时间
     * @since 7.260102.0
     */
    private @NonNull Map<String, List<Range<LocalTime>>> querySpaceId2OpeningTime(
            @NonNull Set<String> spaceIds, @NonNull LocalDate date) {
        if (CollectionUtils.isEmpty(spaceIds)) {
            logProjectInstance().warn("无空间ID，无法查询空间营业时间");
            return Collections.emptyMap();
        }
        MidCalendarWorkTimeBatchQueryParam batchQueryParam = new MidCalendarWorkTimeBatchQueryParam();
        batchQueryParam.setObjectIds(spaceIds);
        batchQueryParam.setBeginDate(date);
        batchQueryParam.setEndDate(date.plusDays(1));
        List<MidCalendarWorkTimeVO> workTimes = midCalendarService.listWorkTimes(batchQueryParam);
        logProjectInstance().info("查询-空间营业时间", Arrays.asList(
                Pair.of("queryParam", batchQueryParam),
                Pair.of("workTimes", workTimes)
        ));
        return workTimes.stream()
                .filter(item -> date.isEqual(item.getDate()))
                .flatMap(item -> item.getDetails().stream())
                .collect(Collectors.groupingBy(MidCalendarWorkTimeVO.MidCalendarWorkTimeDetailVO::getObjectId,
                        Collectors.mapping(item -> Range.between(
                                item.getBeginWorkTime(), item.getEndWorkTime()), Collectors.toList())));
    }

    @Override
    public String logTag() {
        return "运行分析报告-空间温度目标";
    }
}
