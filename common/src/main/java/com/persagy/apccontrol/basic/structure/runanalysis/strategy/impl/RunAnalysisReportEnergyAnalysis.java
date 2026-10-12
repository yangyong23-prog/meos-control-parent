package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandInstantiationDateTagVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunAnalysisReportEnergyAnalysisDto;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunAnalysisReportEnergyAnalysisTreeDto;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunOverviewEnergyAnalysisDto;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.EnergyAnalysisConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.EnergyAnalysisTreeConfigTree;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy;
import com.persagy.apccontrol.basic.utils.BeanListCopyUtil;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/22 13:41
　　 * @description: 能耗分析相关实现
*/
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportEnergyAnalysis implements RunAnalysisReportStrategy<RunAnalysisReportEnergyAnalysisDto, EnergyAnalysisConfig>, LogComponent  {

    private final AbstractIotDataCacheServer<?> abstractIotDataCacheServer;

    private final MidCalendarClientService midCalendarClientService;

    @Override
    public Boolean isSupport(@NotNull RunAnalysisReportTypeEnum type) {
        return RunAnalysisReportTypeEnum.DAILY.equals(type) || RunAnalysisReportTypeEnum.MONTHLY.equals(type);
    }

    @Override
    public RunAnalysisReportEnergyAnalysisDto buildWithConfig(RunAnalysisReportBuildParam param, EnergyAnalysisConfig config, Map<RunAnalysisReportChapterEnum,List<RunAnalysisReportConfig>> allConfigData) {
        switch (param.getReportType()){
            case DAILY:
                //日报生成
                return dayReportGenerate(param,config);
            case MONTHLY:
                return null;
        }
        return null;
    }


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/2/2 15:39
    　　 * @description: 计算日报数据
    */
    public RunAnalysisReportEnergyAnalysisDto dayReportGenerate(RunAnalysisReportBuildParam param, EnergyAnalysisConfig config) {
        RunAnalysisReportEnergyAnalysisDto runAnalysisReportEnergyAnalysisDto = new RunAnalysisReportEnergyAnalysisDto();
        //处理解析配置
        TreeContext ctx = extractTreeContext(config);
        if (ctx == null) {
            return null;
        }

        // 日期计算逻辑保持不变
        List<LocalDate> weekDayLocalDateTime = getWeekDayLocalDateTime(param.getReportDate());
        LocalDate beginWeekDay = CollectionUtils.isEmpty(weekDayLocalDateTime)? LocalDate.MAX : weekDayLocalDateTime.get(0);
        LocalDate firstDayOfMonth = DateUtils.getFirstDayOfMonth(param.getReportDate());
        LocalDate startLocalDate = beginWeekDay.isBefore(firstDayOfMonth) ? beginWeekDay : firstDayOfMonth;

        //当前日到所在月1号的所有日期集合
        List<LocalDate> datesFromMonthStart = DateUtils.getDatesFromMonthStart(param.getReportDate());

        // 组装objectIds（原方法特有：包含所有后代）
        Set<String> objectIds = Sets.newHashSet();
        ctx.getCopTopIdToAllDescendantIdsMap().forEach((topId, allDescendantIds) -> {
            if (!ObjectUtil.isEmpty(ctx.getItemIdToRwdId().get(topId))) {
                objectIds.add(ctx.getItemIdToRwdId().get(topId));
            }
            for (String allDescendantId : allDescendantIds) {
                if (!ObjectUtil.isEmpty(ctx.getItemIdToRwdId().get(allDescendantId))) {
                    objectIds.add(ctx.getItemIdToRwdId().get(allDescendantId));
                }
            }
        });
        //添加最顶级节点
        if (!ObjectUtil.isEmpty(ctx.getItemIdToRwdId().get(ctx.getMainItemId()))) {
           objectIds.add( ctx.getItemIdToRwdId().get(ctx.getMainItemId()));
        }

        // 查询基础数据
        List<InfoCodeValueVO> infoCodeValueVOS = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.DAY,
                objectIds,
                Sets.newHashSet(InfoCodeEnum.SUBENTRY_ACC_CONSUM.getCode()),
                DateUtils.getDayBoundaryByDate(startLocalDate, 0, Boolean.TRUE),
                DateUtils.getDayBoundaryByDate(param.getReportDate(), 1, Boolean.TRUE)
        );
        Map<String, Map<String, Map<LocalDateTime, Object>>> objectIdInfoCodeTimeValueMap =
                InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueVOS);
        if(CollectionUtils.isEmpty(objectIdInfoCodeTimeValueMap)){
            return null;
        }

        //1、计算运行概览指标
        runAnalysisReportEnergyAnalysisDto.setRunOverviewEnergyAnalysisDto(
                runEnergyAnalysisCalculate(DateUtils.getDayBoundaryByDate(param.getReportDate(), 0, Boolean.TRUE),weekDayLocalDateTime,datesFromMonthStart,objectIdInfoCodeTimeValueMap,ctx)
        );
        //2、能耗分析概述
        RunAnalysisReportEnergyAnalysisDto.EnergyConsumptionOverview energyConsumptionOverview = new RunAnalysisReportEnergyAnalysisDto.EnergyConsumptionOverview();
        if(!ObjectUtil.isEmpty(runAnalysisReportEnergyAnalysisDto.getRunOverviewEnergyAnalysisDto())){
            BeanUtils.copyProperties(runAnalysisReportEnergyAnalysisDto.getRunOverviewEnergyAnalysisDto(), energyConsumptionOverview);
        }
        runAnalysisReportEnergyAnalysisDto.setEnergyConsumptionOverview(energyConsumptionOverview);

        //3、计算树列表结构
        List<RunAnalysisReportEnergyAnalysisTreeDto> treeData = convertToTreeList(ctx.getTreeData());
        fillDataRecursive(treeData,objectIdInfoCodeTimeValueMap, DateUtils.getDayBoundaryByDate(param.getReportDate(), 0, Boolean.TRUE),
                runAnalysisReportEnergyAnalysisDto.getRunOverviewEnergyAnalysisDto(),weekDayLocalDateTime,datesFromMonthStart);
        //计算其他
        RunAnalysisReportEnergyAnalysisTreeDto otherItemNode = otherItemGenerate(ctx, treeData, runAnalysisReportEnergyAnalysisDto);
        if(ObjectUtil.isNotEmpty(otherItemNode)){
            treeData.add(otherItemNode);
        }
        runAnalysisReportEnergyAnalysisDto.setTrees(treeData);
        return runAnalysisReportEnergyAnalysisDto;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/2/2 16:58
    　　 * @description: 计算其他项
    */
    private static RunAnalysisReportEnergyAnalysisTreeDto otherItemGenerate(TreeContext ctx, List<RunAnalysisReportEnergyAnalysisTreeDto> treeData, RunAnalysisReportEnergyAnalysisDto runAnalysisReportEnergyAnalysisDto) {
        //主分项id
        String mainItemId = ctx.getMainItemId();
        if(!ctx.getCropTopIdToDirectChildrenIdsMap().containsKey(mainItemId)){
            //表没有选择主分项
            //顶节点到所有直接后代ID的映射（基于原始树）
            Map<String, Set<String>> topIdToDirectChildrenIdsMap = ctx.getTopIdToDirectChildrenIdsMap();
            //裁剪后的直接子节点映射（第一个方法需要）
            Map<String, Set<String>> cropTopIdToDirectChildrenIdsMap = ctx.getCropTopIdToDirectChildrenIdsMap();

            //判断裁剪后的顶节点集合是否是裁剪前的全集
            Set<String> diffItemIds = topIdToDirectChildrenIdsMap.values().stream().flatMap(Set::stream)
                    .filter(e -> !cropTopIdToDirectChildrenIdsMap.containsKey(e))
                    .collect(Collectors.toSet());
            if(!CollectionUtils.isEmpty(diffItemIds)){
                RunAnalysisReportEnergyAnalysisTreeDto otherItem = new RunAnalysisReportEnergyAnalysisTreeDto();
                //日能耗
                BigDecimal dayEnergyConsumptionTotal = null;
                //"上周相似日平均能耗"
                BigDecimal weekLastSimilarDayAverageEnergyConsumptionTotal = null;
                //"月累计能耗"
                BigDecimal monthCumulativeEnergyConsumptionTotal = null;
                //差集不为空，则有其他选项
                for (RunAnalysisReportEnergyAnalysisTreeDto treeDatum : treeData) {
                    //日耗
                    if(!ObjectUtil.isEmpty(treeDatum.getDayEnergyConsumption())){
                        if(ObjectUtil.isEmpty(dayEnergyConsumptionTotal)){
                            dayEnergyConsumptionTotal = BigDecimal.ZERO;
                        }
                        dayEnergyConsumptionTotal = dayEnergyConsumptionTotal.add(treeDatum.getDayEnergyConsumption());
                    }
                    //"上周相似日平均能耗"
                    if(!ObjectUtil.isEmpty(treeDatum.getWeekLastSimilarDayAverageEnergyConsumption())){
                        if(ObjectUtil.isEmpty(weekLastSimilarDayAverageEnergyConsumptionTotal)){
                            weekLastSimilarDayAverageEnergyConsumptionTotal = BigDecimal.ZERO;
                        }
                        weekLastSimilarDayAverageEnergyConsumptionTotal = weekLastSimilarDayAverageEnergyConsumptionTotal.add(treeDatum.getWeekLastSimilarDayAverageEnergyConsumption());
                    }
                    //"月累计能耗"
                    if(!ObjectUtil.isEmpty(treeDatum.getMonthCumulativeEnergyConsumption())){
                        if(ObjectUtil.isEmpty(monthCumulativeEnergyConsumptionTotal)){
                            monthCumulativeEnergyConsumptionTotal = BigDecimal.ZERO;
                        }
                        monthCumulativeEnergyConsumptionTotal = monthCumulativeEnergyConsumptionTotal.add(treeDatum.getMonthCumulativeEnergyConsumption());
                    }

                }
                //计算其他项的指标
                RunOverviewEnergyAnalysisDto runOverviewEnergyAnalysisDto = runAnalysisReportEnergyAnalysisDto.getRunOverviewEnergyAnalysisDto();
                BigDecimal projectTotalEnergyConsumption = runOverviewEnergyAnalysisDto.getProjectTotalEnergyConsumption();
                if(ObjectUtil.isNotEmpty(projectTotalEnergyConsumption) && ObjectUtil.isNotEmpty(dayEnergyConsumptionTotal)){
                    otherItem.setDayEnergyConsumption(projectTotalEnergyConsumption.subtract(dayEnergyConsumptionTotal));
                    otherItem.setProjectTotalEnergyConsumption(projectTotalEnergyConsumption);
                    otherItem.setDayEnergyConsumptionProjectRatio(otherItem.dayEnergyConsumptionProjectRatioCalc());
                }
                if(ObjectUtil.isNotEmpty(weekLastSimilarDayAverageEnergyConsumptionTotal) &&ObjectUtil.isNotEmpty(runOverviewEnergyAnalysisDto.getWeekLastSimilarDayAverageEnergyConsumption())){
                    otherItem.setWeekLastSimilarDayAverageEnergyConsumption(runOverviewEnergyAnalysisDto.getWeekLastSimilarDayAverageEnergyConsumption().subtract(weekLastSimilarDayAverageEnergyConsumptionTotal));
                    otherItem.setChainRatio(otherItem.chainRatioCalculate());
                }
                if(ObjectUtil.isNotEmpty(monthCumulativeEnergyConsumptionTotal) && ObjectUtil.isNotEmpty(runOverviewEnergyAnalysisDto.getProjectMonthTotalEnergyConsumption())){
                    otherItem.setMonthCumulativeEnergyConsumption(runOverviewEnergyAnalysisDto.getProjectMonthTotalEnergyConsumption().subtract(monthCumulativeEnergyConsumptionTotal));
                }
                otherItem.setItemName(LocaleMessageUtil.getMessage("key_report_analysis_item_other_name"));
                otherItem.setItemCode("analysisOtherCode");
                otherItem.setId("analysisOtherId");
                return otherItem;
            }
        }
        return null;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/2/3 10:55
    　　 * @description: 运行概述-能耗分析
    */
    public RunOverviewEnergyAnalysisDto runOverviewEnergyAnalysisCalculate(Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData, LocalDate reportDate) {
        if(ObjectUtil.isEmpty(allConfigData.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENERGY_ANALYSIS))){
            return null;
        }
        EnergyAnalysisConfig jsonObject = (EnergyAnalysisConfig) allConfigData.get(RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENERGY_ANALYSIS).get(0);
        TreeContext ctx = extractTreeContext(jsonObject);
        if (ctx == null) {
            return null;
        }

        List<LocalDate> weekDayLocalDateTime = getWeekDayLocalDateTime(reportDate);
        LocalDate beginWeekDay = CollectionUtils.isEmpty(weekDayLocalDateTime)? LocalDate.MAX : weekDayLocalDateTime.get(0);
        LocalDate firstDayOfMonth = DateUtils.getFirstDayOfMonth(reportDate);
        LocalDate startLocalDate = beginWeekDay.isBefore(firstDayOfMonth) ? beginWeekDay : firstDayOfMonth;

        // 查询主分项数据（此方法特有：仅查询mainItemId）
        List<InfoCodeValueVO> infoCodeValueVOS = this.abstractIotDataCacheServer.listInfoCodeValueByCloud(
                DtpRwdPersistenceTimeTypeEnum.DAY,
                Sets.newHashSet(ctx.getItemIdToRwdId().get(ctx.getMainItemId())),
                Sets.newHashSet(InfoCodeEnum.SUBENTRY_ACC_CONSUM.getCode()),
                DateUtils.getDayBoundaryByDate(startLocalDate, 0, Boolean.TRUE),
                DateUtils.getDayBoundaryByDate(reportDate, 1, Boolean.TRUE)
        );
        Map<String, Map<String, Map<LocalDateTime, Object>>> objectIdInfoCodeTimeValueMap =
                InfoCodeValueVO.listToObjectIdInfoCodeTimeValueMap(infoCodeValueVOS);
        if(ObjectUtil.isEmpty(objectIdInfoCodeTimeValueMap) || ObjectUtil.isEmpty(objectIdInfoCodeTimeValueMap.get(ctx.getItemIdToRwdId().get(ctx.getMainItemId())))){
            return null;
        }
        return runEnergyAnalysisCalculate(DateUtils.getDayBoundaryByDate(reportDate, 0, Boolean.TRUE), weekDayLocalDateTime,null,objectIdInfoCodeTimeValueMap,ctx);
    }
    /**
     * 提取 treeData 中所有节点的 id 与 rwdId 映射
     *
     * @param treeData 完整的JSON配置字符串
     * @return Map<节点ID, 对应的rwdId>
     */
    public static Map<String, String> extractIdRwdIdMap(List<EnergyAnalysisTreeConfigTree> treeData) {
        Map<String, String> result = new HashMap<>();
        if (ObjectUtil.isEmpty(treeData)) {
            return result;
        }
        // 递归遍历树节点
        traverseTree(treeData, result);
        return result;
    }


    /**
     * 递归遍历树节点
     */
    private static void traverseTree(List<EnergyAnalysisTreeConfigTree> nodes, Map<String, String> result) {
        if (ObjectUtil.isEmpty(nodes)) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            EnergyAnalysisTreeConfigTree node = nodes.get(i);
            // 提取当前节点的 id 和 rwdId
            String id = node.getId();
            String rwdId = node.getRwdId();
            // 放入Map（跳过空值）
            if (id != null && rwdId != null) {
                result.put(id, rwdId);
            }
            // 递归处理子节点 treeSourceNodeChildren
            List<EnergyAnalysisTreeConfigTree> children = node.getTreeSourceNodeChildren();
            if (ObjectUtil.isNotEmpty(children)) {
                traverseTree(children, result);
            }
        }
    }



    //获取上一周（周一~周日）的公休日标签且与报告日是相同公休日的日期
    public List<LocalDate> getWeekDayLocalDateTime(LocalDate reportDate){
        //查询工作历
        Map<LocalDate, List<MidCalendarDateTagValueEnum>> dateToVoMap = midCalendarClientService.listInstantiationDateTag(reportDate.minusDays(30), reportDate)
                .stream()
                .collect(Collectors.toMap(DemandInstantiationDateTagVO::getDate,
                        e -> CollectionUtils.isEmpty(e.getCalendarTags())?Lists.newArrayList():e.getCalendarTags(),
                        (k1, k2) -> k1)
                );
        //获取上周，周一到周日的日期
        List<LocalDate> lastWeekDates = getLastWeekDates(reportDate);
        //获取当天的公休日
        List<MidCalendarDateTagValueEnum> midCalendarDateTagValueEnums = dateToVoMap.get(reportDate);
        MidCalendarDateTagValueEnum currentCalendarDateTagValueEnum = null;

        List<LocalDate> result = Lists.newArrayList();

        if( midCalendarDateTagValueEnums.contains(MidCalendarDateTagValueEnum.WORKDAY)){
            //工作日
            currentCalendarDateTagValueEnum = MidCalendarDateTagValueEnum.WORKDAY;
        }else if(midCalendarDateTagValueEnums.contains(MidCalendarDateTagValueEnum.DAYOFF)){
            //休息日
            currentCalendarDateTagValueEnum = MidCalendarDateTagValueEnum.DAYOFF;
        }
        for (LocalDate lastWeekDate : lastWeekDates) {
            if(dateToVoMap.getOrDefault(lastWeekDate,Lists.newArrayList()).contains(currentCalendarDateTagValueEnum)){
                //与当天是相同的公休日
                result.add(lastWeekDate);
            }
        }
        return result;
    }

    /**
     * 获取指定日期所在周的上周，周一到周日的日期列表
     * @param date 指定日期
     * @return 上周一到周日的日期列表（共7天）
     */
    public static List<LocalDate> getLastWeekDates(LocalDate date) {
        List<LocalDate> dates = new ArrayList<>();

        // 获取当前日期是星期几（周一=1，周日=7）
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        int dayOfWeek = date.get(weekFields.dayOfWeek());

        // 计算上周一：当前日期 - (dayOfWeek - 1) - 7
        // dayOfWeek - 1 是到本周一的天数，再减7到上周一
        LocalDate lastMonday = date.minusDays(dayOfWeek - 1 + 7);

        // 依次添加上周一到上周日
        for (int i = 1; i < 8; i++) {
            dates.add(lastMonday.plusDays(i));
        }

        return dates;
    }


    /**
     * 根据给定的星期集合和报告日期，获取上周对应星期的日期集合
     *
     * @param dayOfWeeks 星期集合（如 MONDAY, TUESDAY），保持插入顺序
     * @param reportDate 报告日期（如 2026-01-30 周五）
     * @return 上周对应星期的 LocalDate 集合（如 2026-01-19, 2026-01-20）
     */
    public LinkedHashSet<LocalDate> getLastWeekDates(LinkedHashSet<DayOfWeek> dayOfWeeks, LocalDate reportDate) {

        LinkedHashSet<LocalDate> result = new LinkedHashSet<>();

        if (reportDate == null) {
            return result;
        }

        // 如果 dayOfWeeks 为空，则默认使用周一到周日
        // DayOfWeek.values() 返回顺序：MONDAY(1) -> SUNDAY(7)，正好符合需求
        if (dayOfWeeks == null || dayOfWeeks.isEmpty()) {
            dayOfWeeks = new LinkedHashSet<>(Arrays.asList(DayOfWeek.values()));
        }

        // 获取上周同一天作为基准（如 reportDate 是周五，则取上周五）
        LocalDate lastWeekBase = reportDate.minusWeeks(1);
        int baseValue = lastWeekBase.getDayOfWeek().getValue(); // 1=周一, 7=周日

        for (DayOfWeek targetDay : dayOfWeeks) {
            int targetValue = targetDay.getValue();
            // 计算目标星期与基准星期的天数差
            int daysDiff = targetValue - baseValue;
            // 基准日期加上偏移量，得到上周目标星期几的日期
            LocalDate targetDate = lastWeekBase.plusDays(daysDiff);
            result.add(targetDate);
        }

        return result;
    }

    //计算项目总用电量 和 项目月总用电量
    public RunOverviewEnergyAnalysisDto runEnergyAnalysisCalculate(LocalDateTime reportDate, List<LocalDate> weekDayLocalDateTime,List<LocalDate> dateFromMonthStart
            , Map<String, Map<String, Map<LocalDateTime, Object>>> objectIdInfoCodeTimeValueMap, TreeContext ctx){
        if(ObjectUtil.isEmpty(objectIdInfoCodeTimeValueMap) || ObjectUtil.isEmpty(objectIdInfoCodeTimeValueMap.get(ctx.getItemIdToRwdId().get(ctx.getMainItemId())))){
            return null;
        }
        Map<LocalDateTime, Object> timeToValueMap = objectIdInfoCodeTimeValueMap.get(ctx.getItemIdToRwdId().get(ctx.getMainItemId())).get(InfoCodeEnum.SUBENTRY_ACC_CONSUM.getCode());
        RunOverviewEnergyAnalysisDto runOverviewEnergyAnalysisDto = new RunOverviewEnergyAnalysisDto();
        //计算项目总用电量
        runOverviewEnergyAnalysisDto.setProjectTotalEnergyConsumption(ObjectUtil.isEmpty(timeToValueMap.get(reportDate))?null:new BigDecimal(String.valueOf(timeToValueMap.get(reportDate))));
        //上周相似日平均能耗
        runOverviewEnergyAnalysisDto.setWeekLastSimilarDayAverageEnergyConsumption(sumAndAverageByDateRange(timeToValueMap, weekDayLocalDateTime));
        //项目月总用电量
        runOverviewEnergyAnalysisDto.setProjectMonthTotalEnergyConsumption(sumByDateRange(timeToValueMap,dateFromMonthStart));
        //环比
        runOverviewEnergyAnalysisDto.setChainRatio(runOverviewEnergyAnalysisDto.chainRatioCalculate());
        return runOverviewEnergyAnalysisDto;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/2/3 10:23
    　　 * @description: 根据指定日期，获取数据求平均值
    */
    private static BigDecimal sumAndAverageByDateRange(Map<LocalDateTime, Object> timeToValueMap, List<LocalDate> dateRangeTime) {
        if(CollectionUtils.isEmpty(dateRangeTime)){
            return null;
        }
        //获取上一周（周一~周日）的公休日标签且与报告日是相同公休日的日期，获取这些日期的耗电量，去除空值后求平均
        BigDecimal countValue = null;
        int count = 0 ;
        for (LocalDate localDate : dateRangeTime) {
            Object value = timeToValueMap.get(DateUtils.getDayBoundaryByDate(localDate, 0, Boolean.TRUE));
            if(ObjectUtil.isNotEmpty(value)){
                if(ObjectUtil.isEmpty(countValue)){
                    countValue = new BigDecimal(String.valueOf(value));
                }else {
                    countValue = countValue.add(new BigDecimal(String.valueOf(value)));
                }
                count++;
            }
        }
        if(ObjectUtil.isEmpty(countValue)){
            return null;
        }
        return countValue.divide(new BigDecimal(count), NEW_SCALE, RoundingMode.HALF_UP);
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/3/9 17:57
    　　 * @description: 根据指定日期，获取数据求和
    */
    private static BigDecimal sumByDateRange(Map<LocalDateTime, Object> timeToValueMap, List<LocalDate> dateRangeTime) {
        if(CollectionUtils.isEmpty(dateRangeTime)){
            return null;
        }
        //获取上一周（周一~周日）的公休日标签且与报告日是相同公休日的日期，获取这些日期的耗电量，去除空值后求平均
        BigDecimal countValue = null;
        for (LocalDate localDate : dateRangeTime) {
            Object value = timeToValueMap.get(DateUtils.getDayBoundaryByDate(localDate, 0, Boolean.TRUE));
            if(ObjectUtil.isNotEmpty(value)){
                if(ObjectUtil.isEmpty(countValue)){
                    countValue = new BigDecimal(String.valueOf(value));
                }else {
                    countValue = countValue.add(new BigDecimal(String.valueOf(value)));
                }
            }
        }
        return countValue;
    }


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/30 13:55
    　　 * @description: 获取总的分项id   选中分项对应归属能耗模型中的一级分项作为总用电分项
    */
    public String mainItemId(Map<String, Set<String>> topIdToDirectChildrenIdsMap, Map<String, Set<String>> topIdToAllDescendantIdsMap){
        Set<String> topIds = Sets.newHashSet(topIdToAllDescendantIdsMap.keySet());
        topIdToAllDescendantIdsMap.values().forEach(topIds::addAll);
        for (String topId : topIdToDirectChildrenIdsMap.keySet()) {
            Set<String> descendantIds = topIdToDirectChildrenIdsMap.get(topId);
            // 求交集，生成新集合，不修改原集合
            Set<String> intersection = descendantIds.stream()
                    .filter(topIds::contains)
                    .collect(Collectors.toSet());
            if(topIds.contains(topId) || ObjectUtil.isNotEmpty(intersection)){
                return topId;
            }
        }
        return null;
    }


    /**
     * 递归填充
     *
     * @param treeData 树节点数组
     * @param objectIdInfoCodeTimeValueMap 数据源 Map<实例ID,Map<时间,信息点对应的iot数据>>
     */
    public static void fillDataRecursive(List<RunAnalysisReportEnergyAnalysisTreeDto> treeData,
                                         Map<String, Map<String, Map<LocalDateTime, Object>>>  objectIdInfoCodeTimeValueMap,
                                         LocalDateTime reportDate,RunOverviewEnergyAnalysisDto runOverviewEnergyAnalysisDto,
                                         List<LocalDate> weekDayLocalDateTime,List<LocalDate> datesFromMonthStart) {
        if (treeData == null || treeData.isEmpty()) {
            return;
        }

        for (RunAnalysisReportEnergyAnalysisTreeDto treeDatum : treeData) {
            // 填充当前节点数据
            if(objectIdInfoCodeTimeValueMap.containsKey(treeDatum.getRwdId()) && objectIdInfoCodeTimeValueMap.get(treeDatum.getRwdId()).containsKey(InfoCodeEnum.SUBENTRY_ACC_CONSUM.getCode())){
                fillNodeData(treeDatum,objectIdInfoCodeTimeValueMap.get(treeDatum.getRwdId()).get(InfoCodeEnum.SUBENTRY_ACC_CONSUM.getCode()),reportDate,runOverviewEnergyAnalysisDto,weekDayLocalDateTime,datesFromMonthStart);
                // 递归处理子节点
                List<RunAnalysisReportEnergyAnalysisTreeDto> children = treeDatum.getChildren();
                if (children != null && !children.isEmpty()) {
                    fillDataRecursive(children, objectIdInfoCodeTimeValueMap,reportDate,runOverviewEnergyAnalysisDto,weekDayLocalDateTime,datesFromMonthStart);
                }
            }
        }
    }


    /**
     * 填充单个节点的数据字段
     */
    private static void fillNodeData(RunAnalysisReportEnergyAnalysisTreeDto node, Map<LocalDateTime, Object> timeToValueMap, LocalDateTime reportDate,
                                     RunOverviewEnergyAnalysisDto runOverviewEnergyAnalysisDto,  List<LocalDate> weekDayLocalDateTime,List<LocalDate> datesFromMonthStart) {
       if( ObjectUtil.isEmpty(timeToValueMap)){
           return;
       }
        //日能耗
       node.setDayEnergyConsumption(ObjectUtil.isEmpty(timeToValueMap.get(reportDate))?null:new BigDecimal(String.valueOf(timeToValueMap.get(reportDate))));
       //项目总用电量
       node.setProjectTotalEnergyConsumption(runOverviewEnergyAnalysisDto.getProjectTotalEnergyConsumption());
       //日能耗占项目用电比
       node.setDayEnergyConsumptionProjectRatio(node.dayEnergyConsumptionProjectRatioCalc());
       //上周相似日平均能耗
       node.setWeekLastSimilarDayAverageEnergyConsumption(sumAndAverageByDateRange(timeToValueMap,weekDayLocalDateTime));
       //环比
        node.setChainRatio(node.chainRatioCalculate());
       //月累计能耗
        node.setMonthCumulativeEnergyConsumption(sumByDateRange(timeToValueMap,datesFromMonthStart));
    }


    /**
     * 获取顶节点下的所有下级节点ID（递归获取所有后代）
     *
     * @param treeData 完整的JSON字符串
     * @return Map<顶节点ID, List<所有后代节点ID>>
     */
    public static Map<String, Set<String>> extractAllDescendantIds(List<EnergyAnalysisTreeConfigTree> treeData) {
        Map<String, Set<String>> result = new HashMap<>();
        if (ObjectUtil.isEmpty(treeData)) {
            return result;
        }
        // 遍历顶层节点（1396, 1486, 1487）
        for (int i = 0; i < treeData.size(); i++) {
            EnergyAnalysisTreeConfigTree topNode = treeData.get(i);
            String topNodeId = topNode.getId();
            Set<String> allDescendantIds = new HashSet<>();
            List<EnergyAnalysisTreeConfigTree> children = topNode.getTreeSourceNodeChildren();
            // 递归收集所有后代节点ID
            recursivelyCollectIds(children, allDescendantIds);
            result.put(topNodeId, allDescendantIds);
        }

        return result;
    }

    /**
     * 递归收集所有节点ID（深度优先遍历）
     *
     * @param nodes 当前层级的节点数组
     * @param result 收集结果的列表（引用传递）
     */
    private static void recursivelyCollectIds(List<EnergyAnalysisTreeConfigTree> nodes, Set<String> result) {
        if (ObjectUtil.isEmpty(nodes)) {
            return;
        }
        for (int i = 0; i < nodes.size(); i++) {
            EnergyAnalysisTreeConfigTree node = nodes.get(i);
            String nodeId = node.getId();
            if (nodeId != null) {
                result.add(nodeId); // 添加当前节点ID
                // 递归处理子节点（继续深入）
                List<EnergyAnalysisTreeConfigTree> children = node.getTreeSourceNodeChildren();
                recursivelyCollectIds(children, result);
            }
        }
    }



    /**
     * 从 JSON 树中筛选出 checked="checked" 的节点并重组新树
     * 规则：如果父节点未选中，向上追溯至最近的选中祖先节点，挂在其下成为子节点
     *
     * @param treeData FastJSON 的 JSONArray (包含 treeSourceNodeChildren 结构)
     * @return 新的 JSONArray，仅包含选中节点及重组后的层级关系
     */
    public static List<EnergyAnalysisTreeConfigTree> buildCheckedTree(List<EnergyAnalysisTreeConfigTree> treeData) {
        // key: 节点ID, value: 选中的节点对象（已深拷贝，防止修改原数据）
        Map<String, EnergyAnalysisTreeConfigTree> checkedNodeMap = new HashMap<>();
        // key: 节点ID, value: 父节点ID（记录整棵树的父子关系，包含未选中节点，用于向上追溯）
        Map<String, String> parentRelationMap = new HashMap<>();
        // 第一步：遍历原树，收集所有选中节点和完整的父子关系
        traverseAndCollect(treeData, null, checkedNodeMap, parentRelationMap);
        // 第二步：构建新树结构
        List<EnergyAnalysisTreeConfigTree> newTree = Lists.newArrayList();
        for (EnergyAnalysisTreeConfigTree node : checkedNodeMap.values()) {
            String currentId = node.getId();
            String parentId = parentRelationMap.get(currentId);
            boolean mounted = false;

            // 向上追溯，找到最近的已选中的父节点
            while (parentId != null) {
                if (checkedNodeMap.containsKey(parentId)) {
                    // 挂载到找到的父节点下
                    EnergyAnalysisTreeConfigTree parentNode = checkedNodeMap.get(parentId);
                    List<EnergyAnalysisTreeConfigTree> parentChildren = parentNode.getTreeSourceNodeChildren();
                    if (parentChildren == null) {
                        parentChildren = Lists.newArrayList();
                        parentNode.setTreeSourceNodeChildren(parentChildren);
                    }
                    parentChildren.add(node);
                    mounted = true;
                    break;
                }
                // 继续向上追溯（跳过未选中的中间节点）
                parentId = parentRelationMap.get(parentId);
            }
            // 如果没有找到已选中的祖先节点，则该节点作为新树的根节点
            if (!mounted) {
                newTree.add(node);
            }
        }
        return newTree;
    }

    /**
     * 递归遍历：收集选中节点，记录所有节点的父子关系
     */
    private static void traverseAndCollect(List<EnergyAnalysisTreeConfigTree> nodes, String parentId,
                                           Map<String, EnergyAnalysisTreeConfigTree> checkedNodeMap,
                                           Map<String, String> parentRelationMap) {
        if (ObjectUtil.isEmpty(nodes)) {
            return;
        }
        for (int i = 0; i < nodes.size(); i++) {
            EnergyAnalysisTreeConfigTree node = nodes.get(i);
            String id = node.getId();
            String checked = node.getChecked();
            // 记录所有节点的父子关系（无论是否选中）
            parentRelationMap.put(id, parentId);
            // 只处理选中的节点：深拷贝并存储
            if ("checked".equals(checked)) {
                EnergyAnalysisTreeConfigTree newNode = new EnergyAnalysisTreeConfigTree();
                newNode.setId(id);
                // 保留原节点所有业务字段
                newNode.setItemName(node.getItemName());
                newNode.setItemCode(node.getItemCode());
                newNode.setRwdId(node.getRwdId());
                newNode.setChecked(checked);
                // 初始化为空数组，后续挂载子节点时会填充
                newNode.setTreeSourceNodeChildren(Lists.newArrayList());
                checkedNodeMap.put(id, newNode);
            }
            // 递归处理子节点
            List<EnergyAnalysisTreeConfigTree> children = node.getTreeSourceNodeChildren();
            if (ObjectUtil.isNotEmpty(children)) {
                traverseAndCollect(children, id, checkedNodeMap, parentRelationMap);
            }
        }
    }

    /**
     * 获取顶节点的直接下级节点ID集合
     *
     * @param treeData 完整的JSON字符串
     * @return Map<顶节点ID, List<直接下级节点ID>>
     */
    public static Map<String, Set<String>> extractDirectChildrenIds(List<EnergyAnalysisTreeConfigTree> treeData) {
        Map<String, Set<String>> result = new HashMap<>();
        if (ObjectUtil.isEmpty(treeData)) {
            return result;
        }
        // 遍历顶层节点（1396, 1486, 1487）
        for (int i = 0; i < treeData.size(); i++) {
            EnergyAnalysisTreeConfigTree topNode = treeData.get(i);
            String topNodeId = topNode.getId();

            Set<String> childrenIds = new HashSet<>();
            List<EnergyAnalysisTreeConfigTree>  children = topNode.getTreeSourceNodeChildren();

            // 提取直接下级的ID（只取第一层，不递归）
            if (ObjectUtil.isNotEmpty(children)) {
                for (int j = 0; j < children.size(); j++) {
                    EnergyAnalysisTreeConfigTree child = children.get(j);
                    String childId = child.getId();
                    if (childId != null) {
                        childrenIds.add(childId);
                    }
                }
            }
            result.put(topNodeId, childrenIds);
        }

        return result;
    }


    /**
     * 将 FastJSON 的 JSONArray 转换为 RunAnalysisReportEnergyConsumptionTree 列表
     * 支持字段名自动映射（treeSourceNodeChildren -> children）
     */
    public static List<RunAnalysisReportEnergyAnalysisTreeDto> convertToTreeList(List<EnergyAnalysisTreeConfigTree> treeData) {
        if (ObjectUtil.isEmpty(treeData)) {
            return Lists.newArrayList();
        }
        return  BeanListCopyUtil.deepCopyListProperties(
                treeData,
                RunAnalysisReportEnergyAnalysisTreeDto::new,
                EnergyAnalysisTreeConfigTree::getTreeSourceNodeChildren,  // 获取源对象的子节点
                RunAnalysisReportEnergyAnalysisTreeDto::setChildren        // 设置目标对象的子节点
        );
    }

    /**
     * 树配置处理结果上下文（仅供内部使用）
     */
    @Data
    public static class TreeContext {
        /** 裁剪后的树数据 */
        private List<EnergyAnalysisTreeConfigTree> treeData;
        /** ID到rwdId的映射 */
        private Map<String, String> itemIdToRwdId;
        /** 顶节点到所有直接后代ID的映射（基于原始树） */
        private Map<String, Set<String>> topIdToDirectChildrenIdsMap;

        /** 顶节点到所有后代ID的映射（基于裁剪后的树） */
        private Map<String, Set<String>> copTopIdToAllDescendantIdsMap;
        /** 主分项ID */
        private String mainItemId;
        /** 裁剪后的直接子节点映射（第一个方法需要） */
        private Map<String, Set<String>> cropTopIdToDirectChildrenIdsMap;
    }

    /**
     * 抽取公共的树配置解析逻辑
     *
     * @param reportChapterEnergyAnalysis JSON配置字符串
     * @return TreeContext 包含所有中间计算结果，如果配置无效返回null
     */
    private TreeContext extractTreeContext(EnergyAnalysisConfig reportChapterEnergyAnalysis ) {
        if (ObjectUtil.isEmpty(reportChapterEnergyAnalysis)
                || ObjectUtil.isEmpty(reportChapterEnergyAnalysis.getTreeData())) {
            return null;
        }
        // 原始树处理
        List<EnergyAnalysisTreeConfigTree> treeData = reportChapterEnergyAnalysis.getTreeData();
        Map<String, Set<String>> topIdToDirectChildrenIdsMap = extractDirectChildrenIds(treeData);
        Map<String, String> itemIdToRwdId = extractIdRwdIdMap(treeData);
        //顶节点到所有节点的下级id
        Map<String, Set<String>> extractAllDescendantIds = extractAllDescendantIds(treeData);

        // 裁剪树并获取后代映射
        treeData = buildCheckedTree(treeData);
        Map<String, Set<String>> copTopIdToAllDescendantIdsMap = extractAllDescendantIds(treeData);

        // 获取主分项ID
        String mainItemId = mainItemId(topIdToDirectChildrenIdsMap, extractAllDescendantIds);

        // 构建上下文
        TreeContext context = new TreeContext();
        context.setTreeData(treeData);
        context.setItemIdToRwdId(itemIdToRwdId);
        context.setCopTopIdToAllDescendantIdsMap(copTopIdToAllDescendantIdsMap);
        context.setMainItemId(mainItemId);
        context.setCropTopIdToDirectChildrenIdsMap(extractDirectChildrenIds(treeData));
        context.setTopIdToDirectChildrenIdsMap(topIdToDirectChildrenIdsMap);

        return context;
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_ENERGY_ANALYSIS;
    }

    @Override
    public String logTag()  {
        return "运行分析报告-能耗分析";
    }


}
