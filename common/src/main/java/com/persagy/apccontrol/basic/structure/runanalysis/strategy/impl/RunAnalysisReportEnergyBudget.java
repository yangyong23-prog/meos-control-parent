package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunAnalysisReportEnergyBudgetDto;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunAnalysisReportEnergyBudgetTreeDto;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunOverviewEnergyBudgetDto;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyDetailResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyTopItemInfoListResponse;
import com.persagy.apccontrol.basic.outside.feign.service.impl.EnergyBudgetServerServiceImpl;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.BenchmarkEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.EnergyTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.EnergyBudgetConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/22 13:41
　　 * @description: 能耗预算相关实现
*/
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportEnergyBudget implements RunAnalysisReportStrategy<RunAnalysisReportEnergyBudgetDto, EnergyBudgetConfig>, LogComponent  {

    private final EnergyBudgetServerServiceImpl energyBudgetServerServiceImpl;

    /**配置中选中的管控制或目标值对应的字段编码*/
    public final static String BENCHMARK = "benchmark";


    @Override
    public Boolean isSupport(@NotNull RunAnalysisReportTypeEnum type) {
        return RunAnalysisReportTypeEnum.DAILY.equals(type) || RunAnalysisReportTypeEnum.MONTHLY.equals(type);
    }

    @Override
    public RunAnalysisReportEnergyBudgetDto buildWithConfig(RunAnalysisReportBuildParam param, EnergyBudgetConfig config, Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData) {
        switch (param.getReportType()){
            case DAILY:
                //日报生成
                return dayReportGenerate(param,config);
            case MONTHLY:
                //todo: 月报生成
               return null;
        }
        return null;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/23 11:19
    　　 * @description: 生成日报的逻辑
    */
    public RunAnalysisReportEnergyBudgetDto dayReportGenerate(RunAnalysisReportBuildParam param,EnergyBudgetConfig reportChapterEnergyBudget){
        RunAnalysisReportEnergyBudgetDto runAnalysisReportEnergyBudgetDto = new RunAnalysisReportEnergyBudgetDto();

        //获取公共基础数据
        CommonEnergyData commonData = getCommonEnergyData(param.getReportDate(), reportChapterEnergyBudget);
        if(commonData == null){
            return null;
        }

        BenchmarkEnum benchmarkEnum = commonData.getBenchmarkEnum();
        List<EnergyDetailResponse> energyDetailResponses = commonData.getEnergyDetailResponses();
        Map<String, EnergyDetailResponse> itemIdToDayDetailMap = commonData.getItemIdToDayDetailMap();
        EnergyTopItemInfoListResponse energyTopItemInfo = commonData.getEnergyTopItemInfo();

        /**查询分项跨天数据(报告日对应月的第一日到报告日)*/
        List<EnergyDetailResponse> energyDetailCrossDayResponses = energyBudgetServerServiceImpl.crossDayEnergyDetail(
                DateUtils.getDayBoundaryByDate(DateUtils.getFirstDayOfMonth(param.getReportDate()), 0, Boolean.TRUE),
                DateUtils.getDayBoundaryByDate(param.getReportDate(), 1, Boolean.TRUE),
                EnergyTypeEnum.ELECTRIC);

        /**查询分项跨天数据(报告日对应月)*/
        List<EnergyDetailResponse> energyDetailCrossDayResponsesMonth = energyBudgetServerServiceImpl.crossDayEnergyDetail(
                DateUtils.getDayBoundaryByDate(DateUtils.getFirstDayOfMonth(param.getReportDate()), 0, Boolean.TRUE),
                DateUtils.getDayBoundaryByDate(DateUtils.getFirstDayOfLastMonth(param.getReportDate()), 0, Boolean.TRUE),
                EnergyTypeEnum.ELECTRIC);


        /**多日数据map*/
        Map<String, EnergyDetailResponse> itemIdToCrossDayMap = energyDetailCrossDayResponses.stream().collect(Collectors.toMap(EnergyDetailResponse::getItemId, e -> e, (k1, k2) -> k1));
        Map<String, EnergyDetailResponse> itemIdToCrossDayMapMonth = energyDetailCrossDayResponsesMonth.stream().collect(Collectors.toMap(EnergyDetailResponse::getItemId, e -> e, (k1, k2) -> k1));

        /**1、计算分项列表数据*/
        calculateBudgetItemList(energyDetailResponses, benchmarkEnum, itemIdToCrossDayMap, energyTopItemInfo, runAnalysisReportEnergyBudgetDto,itemIdToCrossDayMapMonth);

        //计算公共字段
        RunOverviewEnergyBudgetDto runOverviewEnergyBudgetDto = runOverviewEnergyBudget(itemIdToDayDetailMap, energyTopItemInfo, benchmarkEnum);

        /**2、运行概述-当日预算统计*/
        calculateRunOverview(runOverviewEnergyBudgetDto, runAnalysisReportEnergyBudgetDto);

        /** 3、能耗预算概述的指标计算*/
        calculateBudgetOverview(runOverviewEnergyBudgetDto, itemIdToCrossDayMap, energyTopItemInfo, benchmarkEnum, runAnalysisReportEnergyBudgetDto,itemIdToCrossDayMapMonth);

        /** 4、保存一下当时使用的配置值*/
        runAnalysisReportEnergyBudgetDto.setBenchmark(benchmarkEnum.getValue());
        return runAnalysisReportEnergyBudgetDto;
    }


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/2/2 14:38
    　　 * @description: 运行概述能耗分析
    */
    public RunOverviewEnergyBudgetDto runOverviewEnergyBudgetCalculate(Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData, LocalDate reportDate){
        if(ObjectUtil.isEmpty(allConfigData.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENERGY_BUDGET))){
            return null;
        }

        EnergyBudgetConfig reportChapterEnergyBudget = (EnergyBudgetConfig) allConfigData.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENERGY_BUDGET).get(0);

        //获取公共基础数据
        CommonEnergyData commonData = getCommonEnergyData(reportDate, reportChapterEnergyBudget);
        if(commonData == null){
            return null;
        }
        return runOverviewEnergyBudget(commonData.getItemIdToDayDetailMap(), commonData.getEnergyTopItemInfo(), commonData.getBenchmarkEnum());
    }

    /**
     * 抽取的公共方法：获取基础能耗数据
     * 包含配置验证、异步查询日数据、分项信息、基础数据处理
     */
    private CommonEnergyData getCommonEnergyData(LocalDate date, EnergyBudgetConfig reportChapterEnergyBudget){
        //判断配置数据据
        if(ObjectUtil.isEmpty(reportChapterEnergyBudget) ||
                ObjectUtil.isEmpty(reportChapterEnergyBudget.getBenchmark()) ||
                ObjectUtil.isEmpty(BenchmarkEnum.analyze(reportChapterEnergyBudget.getBenchmark()))){
            //为空、没有指定字段、指定字段没有值，则返回null
            log.info("能耗预算配置数据为空:{}",JSONObject.toJSONString(reportChapterEnergyBudget));
            return null;
        }
        //获取配置数据
        BenchmarkEnum benchmarkEnum = BenchmarkEnum.analyze(reportChapterEnergyBudget.getBenchmark());
        /**查询分项日(当日)数据*/
        CompletableFuture<List<EnergyDetailResponse>> energyDetailResponsesFuture = CompletableFuture.supplyAsync(
                () -> energyBudgetServerServiceImpl.dayEnergyDetail(
                        DateUtils.getDayBoundaryByDate(date, 0, Boolean.TRUE),
                        DateUtils.getDayBoundaryByDate(date, 1, Boolean.TRUE),
                        EnergyTypeEnum.ELECTRIC), BusinessThreadPoolExecutor.ttlExecutor());

        /**查询分项信息列表,目的是获取顶级分项节点*/
        CompletableFuture<EnergyTopItemInfoListResponse> energyItemInfoListResponsesFuture = CompletableFuture.supplyAsync(
                () -> energyBudgetServerServiceImpl.queryTopItem(EnergyTypeEnum.ELECTRIC), BusinessThreadPoolExecutor.ttlExecutor());

        // 等待所有任务完成
        CompletableFuture.allOf(energyDetailResponsesFuture, energyItemInfoListResponsesFuture).join();
        List<EnergyDetailResponse> energyDetailResponses = energyDetailResponsesFuture.join();
        //判空
        if(ObjectUtil.isEmpty(energyDetailResponses)){
            return null;
        }
        //日数据map
        Map<String, EnergyDetailResponse> itemIdToDayDetailMap = energyDetailResponses.stream().collect(Collectors.toMap(EnergyDetailResponse::getItemId, e -> e, (k1, k2) -> k1));

        EnergyTopItemInfoListResponse energyTopItemInfo = energyItemInfoListResponsesFuture.join();
        if(ObjectUtil.isEmpty(energyTopItemInfo)){
            return null;
        }
        //分项数据中没有顶级分项节点数据，返回空
        if(!itemIdToDayDetailMap.containsKey(energyTopItemInfo.getTopItemId())){
            return null;
        }

        CommonEnergyData result = new CommonEnergyData();
        result.setBenchmarkEnum(benchmarkEnum);
        result.setEnergyDetailResponses(energyDetailResponses);
        result.setItemIdToDayDetailMap(itemIdToDayDetailMap);
        result.setEnergyTopItemInfo(energyTopItemInfo);
        return result;
    }

    /**
     * 基础能耗数据持有类
     */
    public static class CommonEnergyData {
        private BenchmarkEnum benchmarkEnum;
        private List<EnergyDetailResponse> energyDetailResponses;
        private Map<String, EnergyDetailResponse> itemIdToDayDetailMap;
        private EnergyTopItemInfoListResponse energyTopItemInfo;

        public BenchmarkEnum getBenchmarkEnum() {
            return benchmarkEnum;
        }

        public void setBenchmarkEnum(BenchmarkEnum benchmarkEnum) {
            this.benchmarkEnum = benchmarkEnum;
        }

        public List<EnergyDetailResponse> getEnergyDetailResponses() {
            return energyDetailResponses;
        }

        public void setEnergyDetailResponses(List<EnergyDetailResponse> energyDetailResponses) {
            this.energyDetailResponses = energyDetailResponses;
        }

        public Map<String, EnergyDetailResponse> getItemIdToDayDetailMap() {
            return itemIdToDayDetailMap;
        }

        public void setItemIdToDayDetailMap(Map<String, EnergyDetailResponse> itemIdToDayDetailMap) {
            this.itemIdToDayDetailMap = itemIdToDayDetailMap;
        }

        public EnergyTopItemInfoListResponse getEnergyTopItemInfo() {
            return energyTopItemInfo;
        }

        public void setEnergyTopItemInfo(EnergyTopItemInfoListResponse energyTopItemInfo) {
            this.energyTopItemInfo = energyTopItemInfo;
        }
    }


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/27 11:01
    　　 * @description: 计算一些公共字段
     */
    public RunOverviewEnergyBudgetDto runOverviewEnergyBudget(Map<String, EnergyDetailResponse> itemIdToDayDetailMap, EnergyTopItemInfoListResponse top, BenchmarkEnum benchmarkEnum) {
        RunOverviewEnergyBudgetDto energyBudgetCommon = new RunOverviewEnergyBudgetDto();
        //获取顶节点
        EnergyDetailResponse topEnergyDetail = itemIdToDayDetailMap.get(top.getTopItemId());
        //计算数据
        energyBudgetCommon.setDayBudget(todayBudgetCalculate(topEnergyDetail, benchmarkEnum));
        energyBudgetCommon.setDayRealEnergy(budgetRealEnergyCalculate(topEnergyDetail));
        energyBudgetCommon.setDayEnergyConsumptionBudgetRatio(energyBudgetCommon.dayEnergyConsumptionBudgetRatioCalculate());
        return energyBudgetCommon;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/27 11:01
    　　 * @description: 计算能耗预算概览数据
     */
    public void calculateBudgetOverview(RunOverviewEnergyBudgetDto energyBudgetCommon, Map<String, EnergyDetailResponse> itemIdToCrossDayMap, EnergyTopItemInfoListResponse top,
                                        BenchmarkEnum benchmarkEnum, RunAnalysisReportEnergyBudgetDto runAnalysisReportEnergyBudgetDto,
                                        Map<String, EnergyDetailResponse> itemIdToCrossDayMapMonth) {
        RunAnalysisReportEnergyBudgetDto.EnergyBudgetOverview energyBudgetOverview = new RunAnalysisReportEnergyBudgetDto.EnergyBudgetOverview();
        //本日预算相关指标
        BeanUtils.copyProperties(energyBudgetCommon,energyBudgetOverview);
        //获取顶节点
        EnergyDetailResponse energyDetailWeekTopResponse = itemIdToCrossDayMap.get(top.getTopItemId());
        //计算本月累计预算
        energyBudgetOverview.setMonthCumulativeTotalBudget(todayBudgetCalculate(energyDetailWeekTopResponse, benchmarkEnum));
        //计算月累计实际能耗
        energyBudgetOverview.setMonthCumulativeRealEnergyConsumption(budgetRealEnergyCalculate(energyDetailWeekTopResponse));
        //计算月累计占比
        energyBudgetOverview.setMonthCumulativeTotalBudgetRatio(energyBudgetOverview.monthCumulativeTotalBudgetRatioCalculate());
        //计算月总预算
        EnergyDetailResponse energyDetailResponseMonth = itemIdToCrossDayMapMonth.get(top.getTopItemId());
        energyBudgetOverview.setMonthTotalBudget(monthTotalBudgetCalculate(energyDetailResponseMonth));
        //计算已用月总预算比例
        energyBudgetOverview.setMonthTotalBudgetRatio(energyBudgetOverview.monthTotalBudgetRatioCalculate());
        runAnalysisReportEnergyBudgetDto.setEnergyBudgetOverview(energyBudgetOverview);
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/27 11:01
    　　 * @description: 计算运行概述-当日预算统计
     */
    public void calculateRunOverview(RunOverviewEnergyBudgetDto energyBudgetCommon, RunAnalysisReportEnergyBudgetDto runAnalysisReportEnergyBudgetDto) {
        // 2、运行概述-当日预算统计的指标计算
        RunOverviewEnergyBudgetDto runOverviewEnergyBudget = new RunOverviewEnergyBudgetDto();
        BeanUtils.copyProperties(energyBudgetCommon,runOverviewEnergyBudget);
        runAnalysisReportEnergyBudgetDto.setRunOverviewEnergyBudgetDto(runOverviewEnergyBudget);
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/2/2 15:27
    　　 * @description: 计算能耗-预算树列表数据
    */
    public void calculateBudgetItemList(List<EnergyDetailResponse> energyDetailResponses, BenchmarkEnum benchmarkEnum, Map<String, EnergyDetailResponse> itemIdToCrossDayMap,
                                        EnergyTopItemInfoListResponse top, RunAnalysisReportEnergyBudgetDto runAnalysisReportEnergyBudgetDto,
                                        Map<String, EnergyDetailResponse> itemIdToCrossDayMapMonth) {
        List<RunAnalysisReportEnergyBudgetTreeDto> budgetTreeList = Lists.newArrayList();
        //计算各项指标
        for (EnergyDetailResponse energyDetailResponse : energyDetailResponses) {
            //构造数据
            RunAnalysisReportEnergyBudgetTreeDto itemTree = new RunAnalysisReportEnergyBudgetTreeDto();
            //基础数据
            itemTree.setItemId(energyDetailResponse.getItemId());
            itemTree.setItemName(energyDetailResponse.getItemName());
            //计算本日预算指标
            itemTree.setDayBudget(todayBudgetCalculate(energyDetailResponse, benchmarkEnum));
            //计算日实际能耗
            itemTree.setDayRealEnergy(budgetRealEnergyCalculate(energyDetailResponse));
            //计算本日预算占预算比
            itemTree.setDayEnergyConsumptionBudgetRatio(itemTree.dayEnergyConsumptionBudgetRatioCalculate());
            //计算日实际能耗与预算差值
            itemTree.setDayRealEnergyDifference(itemTree.dayRealEnergyDifferenceCalculate());

            //计算月相关的(报告日对应月的第一日到报告日)
            EnergyDetailResponse energyDetailWeek = itemIdToCrossDayMap.get(energyDetailResponse.getItemId());
            //计算月累计实际能耗
            itemTree.setMonthCumulativeRealEnergyConsumption(budgetRealEnergyCalculate(energyDetailWeek));
            //计算月总预算
            EnergyDetailResponse energyDetailResponseMonth = itemIdToCrossDayMapMonth.get(energyDetailResponse.getItemId());
            itemTree.setMonthTotalBudget(monthTotalBudgetCalculate(energyDetailResponseMonth));
            //计算月总预算占预算比
            itemTree.setMonthTotalBudgetRatio(itemTree.monthTotalBudgetRatioCalculate());
            budgetTreeList.add(itemTree);
        }
        //过滤出顶级树节点,组装数据
        RunAnalysisReportEnergyBudgetTreeDto topBudget = budgetTreeList.stream().filter(item -> item.getItemId().equals(top.getTopItemId())).collect(Collectors.toList()).get(0);
        topBudget.setChildren(budgetTreeList.stream().filter(item -> !item.getItemId().equals(top.getTopItemId())).collect(Collectors.toList()));
        runAnalysisReportEnergyBudgetDto.setTrees(Lists.newArrayList(topBudget));
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 16:17
    　　 * @description: 计算指标：本日预算
     */
    public BigDecimal todayBudgetCalculate(EnergyDetailResponse energyDetailResponse, BenchmarkEnum benchmarkEnum){
        if(ObjectUtil.isNull(energyDetailResponse)){
            return null;
        }
        //报告配置中选择的是"目标值"：获取顶级节点对应的日目标值、报告配置中选择的是"管控值"：获取顶级节点对应的日管控值
        return BenchmarkEnum.TARGET.equals(benchmarkEnum) ? energyDetailResponse.getTargetData() : energyDetailResponse.getControlData();
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 16:31
    　　 * @description: 计算指标：日或月实际能耗
     */
    public BigDecimal budgetRealEnergyCalculate(EnergyDetailResponse energyDetailResponse){
        if(ObjectUtil.isNull(energyDetailResponse)){
            return null;
        }
        //从能耗预算平台获取预算管理的顶级节点的报告日的实际能耗
        return energyDetailResponse.getRealEnergy();
    }


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 17:42
    　　 * @description: 计算指标：月总预算  计算公式：从能耗预算平台获取预算管理的顶级节点的数据，报告日对应月的目标值，固定取目标值，跟报告配置没关系
     */
    public BigDecimal monthTotalBudgetCalculate(EnergyDetailResponse energyDetailResponse){
        if(ObjectUtil.isNull(energyDetailResponse)){
            return null;
        }
        //从能耗预算平台获取预算管理的顶级节点的报告日的实际能耗
        return  energyDetailResponse.getTargetData();
    }


    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENERGY_BUDGET;
    }

    @Override
    public String logTag()  {
        return "运行分析报告-能耗预算";
    }
}
