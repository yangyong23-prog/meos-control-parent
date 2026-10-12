package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.coolingenergy.CoolingEnergyDTO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 冷量及能耗分析策略
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportCoolingEnergy extends AbstractRunAnalysisReportStrategy<CoolingEnergyDTO> {

    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public Boolean isSupport(@NonNull RunAnalysisReportTypeEnum type) {
        return type == RunAnalysisReportTypeEnum.DAILY;
    }

    @Override
    public CoolingEnergyDTO buildWithConfig(
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
        // 查询系统ID
        CompletableFuture<List<DtpRwdObjectDTO>> systemsFuture = CompletableFuture.supplyAsync(
                this::querySystems, BusinessThreadPoolExecutor.ttlExecutor());

        // 查询日累计差分供冷量
        CompletableFuture<BigDecimal> dayAccCoolFuture = systemsFuture.thenApplyAsync(
                systems -> {
                    Map<LocalDateTime, BigDecimal> dateTime2Value = this.calculateInfoCodeSumBySystemId(date,
                            DtpRwdPersistenceTimeTypeEnum.DAY, systems, InfoCodeEnum.ACC_COOL);
                    return MapUtils.isNotEmpty(dateTime2Value) ? dateTime2Value.values().stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add) : null;
                },
                BusinessThreadPoolExecutor.ttlExecutor());

        // 查询日累计差分耗电量
        CompletableFuture<BigDecimal> dayAccElecConsumFuture = systemsFuture.thenApplyAsync(
                systems -> {
                    Map<LocalDateTime, BigDecimal> dateTime2Value = this.calculateInfoCodeSumBySystemId(date,
                            DtpRwdPersistenceTimeTypeEnum.DAY, systems, InfoCodeEnum.ACC_ELEC_CONSUM);
                    return MapUtils.isNotEmpty(dateTime2Value) ? dateTime2Value.values().stream()
                            .reduce(BigDecimal.ZERO, BigDecimal::add) : null;
                },
                BusinessThreadPoolExecutor.ttlExecutor());

        // 计算累计差分供冷量总值
        CompletableFuture<Map<LocalDateTime, BigDecimal>> dateTime2AccCoolFuture = systemsFuture.thenApplyAsync(
                systems -> this.calculateInfoCodeSumBySystemId(date,
                        DtpRwdPersistenceTimeTypeEnum.HOUR, systems, InfoCodeEnum.ACC_COOL),
                BusinessThreadPoolExecutor.ttlExecutor());

        // 计算累计差分耗电量总值
        CompletableFuture<Map<LocalDateTime, BigDecimal>> dateTime2AccElecConsumFuture = systemsFuture.thenApplyAsync(
                systems -> this.calculateInfoCodeSumBySystemId(date,
                        DtpRwdPersistenceTimeTypeEnum.HOUR, systems, InfoCodeEnum.ACC_ELEC_CONSUM),
                BusinessThreadPoolExecutor.ttlExecutor());

        CompletableFuture.allOf(systemsFuture, dateTime2AccCoolFuture, dateTime2AccElecConsumFuture).get();

        List<DtpRwdObjectDTO> systems = systemsFuture.get();
        if (CollectionUtils.isEmpty(systems)) {
            logProjectInstance().warn("无有效 ACAC 系统，无法构建报告", Pair.of("date", date));
            return null;
        }

        CoolingEnergyDTO coolingEnergy = new CoolingEnergyDTO();
        coolingEnergy.setRelSystems(systems);
        coolingEnergy.setDayAccCool(dayAccCoolFuture.get());
        coolingEnergy.setDayAccElecConsum(dayAccElecConsumFuture.get());
        coolingEnergy.setDateTime2AccCool(dateTime2AccCoolFuture.get());
        coolingEnergy.setDateTime2AccElecConsum(dateTime2AccElecConsumFuture.get());
        logInstance().info("构建-报告完成", Arrays.asList(
                Pair.of("date", date),
                Pair.of("costMs", System.currentTimeMillis() - startTime),
                Pair.of("result", coolingEnergy)
        ));
        return coolingEnergy;
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_ENERGY;
    }

    /**
     * 根据系统ID计算信息点总数值
     *
     * @param date     日期
     * @param timeType 时间类型
     * @param systems  系统列表
     * @param infoCode 信息点编码
     * @return 信息点总数值
     * @since 7.260201.0
     */
    private @NonNull Map<LocalDateTime, BigDecimal> calculateInfoCodeSumBySystemId(
            @NonNull LocalDate date,
            @NonNull DtpRwdPersistenceTimeTypeEnum timeType,
            @NonNull List<DtpRwdObjectDTO> systems,
            @NonNull InfoCodeEnum infoCode) {
        if (CollectionUtils.isEmpty(systems)) {
            return Collections.emptyMap();
        }
        Set<String> systemIds = systems.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        LocalDateTime today = date.atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);
        Set<String> infoCodes = Sets.newHashSet(infoCode.getCode());
        List<InfoCodeValueVO> infoCodeValues = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                timeType, systemIds, infoCodes, today, tomorrow);
        Map<LocalDateTime, BigDecimal> dateTime2Sum = this.calculateInfoCodeSum(infoCodeValues);
        logProjectInstance().info("计算-信息点总数值", Arrays.asList(
                Pair.of("date", date),
                Pair.of("systemIds", systemIds),
                Pair.of("infoCodes", infoCodes),
                Pair.of("infoCodeValues", infoCodeValues),
                Pair.of("dateTime2Sum", dateTime2Sum)
        ));
        return dateTime2Sum;
    }

    /**
     * 查询系统列表
     *
     * @return 系统列表
     * @since 7.250201.0
     */
    private @NonNull List<DtpRwdObjectDTO> querySystems() {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.setClassCodeEnum(DtpRwdClassCodeEnum.ACAC_SYSTEM);
        queryParam.filterMonitoringSurveillanceStatus();
        queryParam.addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName);
        List<DtpRwdObjectDTO> systems = this.dtpRwdClientService.listObject(queryParam);
        if (CollectionUtils.isEmpty(systems)) {
            logProjectInstance().warn("无有效 ACAC 系统，无法查询空调数据");
            return Collections.emptyList();
        }
        logProjectInstance().info("查询-系统列表", Pair.of("systems =", systems));
        return systems;
    }

    @Override
    public String logTag() {
        return "运行分析报告-冷量及能耗";
    }
}
