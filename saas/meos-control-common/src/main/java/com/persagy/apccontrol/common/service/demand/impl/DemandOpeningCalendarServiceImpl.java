package com.persagy.apccontrol.common.service.demand.impl;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CachePersistenceKey;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarDTO;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarGlobalRuleDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarDateTagValueEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarWorkDayTypeEnum;
import com.persagy.apccontrol.basic.model.param.demand.openingcalendar.*;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleLastUpdateTimeVO;
import com.persagy.apccontrol.basic.model.vo.demand.openingcalendar.DemandGlobalRuleVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarOpenTimeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.outside.feign.service.param.calendar.*;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.apccontrol.basic.structure.calendar.converter.MidCalendarConvert;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import com.persagy.apccontrol.common.service.demand.DemandCalendarService;
import com.persagy.apccontrol.common.service.demand.DemandOpeningCalendarService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.pattern.DatePattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.utils.range.RangeUtils.DEFAULT_RANGE_SPLIT_REGEX;

/**
 * 需求管理-营业日历服务实现
 *
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>getGlobalRule</b> - 查询全局规则</li>
 *     <li><b>getGlobalRuleLastUpdateTime</b> - 查询全局规则最后更新时间</li>
 *     <li><b>listInstantiationDateTag</b> - 查询实例化日期标签</li>
 *     <li><b>editGlobalRule</b> - 编辑全局规则</li>
 * </ul>
 * <p>------------------------- 外部方法 -------------------------
 * <ul>
 *     <li><b>asyncResetTemplateCalendar</b> - 异步重置需求日历</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Service
@Slf4j
public class DemandOpeningCalendarServiceImpl implements DemandOpeningCalendarService {

    @Autowired
    private Executor executor;
    @Autowired
    private RedisCacheTemplate redisCacheTemplate;
    @Autowired
    private MidCalendarClientService midCalendarClientService;
    @Autowired
    private DemandCalendarQueryService demandCalendarQueryService;
    @Autowired
    private DemandCalendarService demandCalendarService;
    @Autowired
    private MidCalendarConvert midCalendarConvert;

    @Override
    public DemandGlobalRuleVO getGlobalRule(@NonNull DemandGlobalRuleQueryParam param) {
        String projectId = ContextUtil.getProjectId();
        //先查询工作历,有个特殊逻辑：有个向上继承的逻辑（空间--》楼层--》建筑--》项目），没有工作历的话，会一层一层的往上去找，有就返回，没有就再继续往找，直到项目为止
        MidCalendarDTO objCalendar = midCalendarClientService.getCalendar(projectId, new MidCalendarQueryParam().setObjId(param.getInstanceId()));
        if (objCalendar == null || StringUtils.isEmpty(objCalendar.getCalendarId())) {
            return null;
        }
        //查询工作历规则
        MidCalendarGlobalRuleDTO calendarRule = midCalendarClientService.getGlobalCalendarRule(projectId, objCalendar.getCalendarId(), null, null);

        DemandGlobalRuleVO result = new DemandGlobalRuleVO();
        result.setWeekdays(calendarRule.getGlobalWeekDays());
        result.setCoolingSeasonDateRangeList(MapUtils.getObject(
                calendarRule.getGlobalSeasonDateRangeMap(),
                MidCalendarAcSeasonTypeEnum.SEASON_COOLING));
        result.setHeatingSeasonDateRangeList(MapUtils.getObject(
                calendarRule.getGlobalSeasonDateRangeMap(),
                MidCalendarAcSeasonTypeEnum.SEASON_HEATING));
        result.setNotCoolingAndHeatingSeasonDateRangeList(MapUtils.getObject(
                calendarRule.getGlobalSeasonDateRangeMap(),
                MidCalendarAcSeasonTypeEnum.SEASON_TRANSITION));
        result.setAnniversary(calendarRule.getGlobalAnniversary());

        //新增节假日-通用配置
        result.setHolidayGeneralConfiguration(calendarRule.getHolidayGeneralConfiguration());
        //新增节假日-特殊配置
        result.setHolidaySpecialConfiguration(calendarRule.getHolidaySpecialConfiguration());

        //空调季
        SeasonRangeParam seasonRangeParam = new SeasonRangeParam();
        seasonRangeParam.setAirSummerRangeList(MapUtils.getObject(
                calendarRule.getGlobalAirSeasonRangeDateRangeMap(),
                MidCalendarDateTagValueEnum.AIR_SUMMER));
        seasonRangeParam.setAirSummerPeakRangeList(MapUtils.getObject(
                calendarRule.getGlobalAirSeasonRangeDateRangeMap(),
                MidCalendarDateTagValueEnum.AIR_SUMMER_PEAK));
        seasonRangeParam.setAirWinterRangeList(MapUtils.getObject(
                calendarRule.getGlobalAirSeasonRangeDateRangeMap(),
                MidCalendarDateTagValueEnum.AIR_WINTER));
        seasonRangeParam.setAirSeasonSpringRangeList(MapUtils.getObject(
                calendarRule.getGlobalAirSeasonRangeDateRangeMap(),
                MidCalendarDateTagValueEnum.AIR_SEASON_SPRING));
        seasonRangeParam.setAirSeasonAutumnRangeList(MapUtils.getObject(
                calendarRule.getGlobalAirSeasonRangeDateRangeMap(),
                MidCalendarDateTagValueEnum.AIR_SEASON_AUTUMN));
        result.setSeasonRangeParam(seasonRangeParam);

        //营业时间
        BusinessTimeRangeParam businessTimeRange = new BusinessTimeRangeParam();
        businessTimeRange.setInstanceId(param.getInstanceId());
        businessTimeRange.setDateTagType(calendarRule.getDateTagType());
        businessTimeRange.setBusinessTimeRangeList(calendarRule.getDefaultBusinessTimeRangeList());
        businessTimeRange.setWorkdayBusinessTimeRangeList(calendarRule.getGlobalBusinessTimeRangeListOnWeekDays());
        businessTimeRange.setDayOffBusinessTimeRangeList(calendarRule.getGlobalBusinessTimeRangeListOnNonWeekDays());
        //新增星期号
        businessTimeRange.setWeekNumberParamList(calendarRule.getWeekNumberParamList());
        //特殊配置内容
        businessTimeRange.setSpecialConfigurationContentParamList(calendarRule.getSpecialConfigurationContentParamList());

        result.setBusinessTimeRange(businessTimeRange);
        return result;
    }

    @Override
    public DemandGlobalRuleLastUpdateTimeVO getGlobalRuleLastUpdateTime(
            @NonNull DemandGlobalRuleLastUpdateTimeQueryParam param) {
        String projectId = param.getProjectId();

        CachePersistenceKey.Key key = CacheKey.Persistence.Demand.globalRuleLastUpdateTime(projectId);
        return redisCacheTemplate.object().get(key.getKey());
    }

    @Override
    public boolean editGlobalRule(@NonNull DemandGlobalRuleUpdateParam param) {
        String projectId = ContextUtil.getProjectId();
        LinkedHashSet<DayOfWeek> weekdays = param.getWeekdays();
        LinkedHashSet<Range<MonthDay>> anniversaries = param.getAnniversaries();
        MidCalendarDTO projectCalendar = midCalendarClientService.getOrInitObjectRealCalendar(
                projectId, DemandInstanceTypeEnum.PROJECT, projectId);
        String calendarId = projectCalendar.getCalendarId();

        //空调工况 编辑
        LinkedHashSet<Range<MonthDay>> coolingSeasonDateRangeList = param.getCoolingSeasonDateRangeList();
        LinkedHashSet<Range<MonthDay>> heatingSeasonDateRangeList = param.getHeatingSeasonDateRangeList();
        LinkedHashSet<Range<MonthDay>> notCoolingAndHeatingSeasonDateRangeList = param.getNotCoolingAndHeatingSeasonDateRangeList();
        List<MidCalendarGlobalDateAcSeasonTagRuleUpdateParam> acSeasonTagRules = new ArrayList<>();
        addAcSeasonTagRules(coolingSeasonDateRangeList, acSeasonTagRules, calendarId, MidCalendarDateTagValueEnum.SEASON_COOLING);
        addAcSeasonTagRules(heatingSeasonDateRangeList, acSeasonTagRules, calendarId, MidCalendarDateTagValueEnum.SEASON_HEATING);
        addAcSeasonTagRules(notCoolingAndHeatingSeasonDateRangeList, acSeasonTagRules, calendarId, MidCalendarDateTagValueEnum.SEASON_TRANSITION);
        // 编辑空调季
        SeasonRangeParam seasonRange = param.getSeasonRangeParam();
        List<MidCalendarGlobalDateAirSeasonTagRuleUpdateParam> airSeasonTagRules = new ArrayList<>();
        addAirSeasonTagRules(seasonRange.getAirSeasonAutumnRangeList(), airSeasonTagRules, calendarId, MidCalendarDateTagValueEnum.AIR_SEASON_AUTUMN);
        addAirSeasonTagRules(seasonRange.getAirSummerRangeList(), airSeasonTagRules, calendarId, MidCalendarDateTagValueEnum.AIR_SUMMER);
        addAirSeasonTagRules(seasonRange.getAirSummerPeakRangeList(), airSeasonTagRules, calendarId, MidCalendarDateTagValueEnum.AIR_SUMMER_PEAK);
        addAirSeasonTagRules(seasonRange.getAirWinterRangeList(), airSeasonTagRules, calendarId, MidCalendarDateTagValueEnum.AIR_WINTER);
        addAirSeasonTagRules(seasonRange.getAirSeasonSpringRangeList(), airSeasonTagRules, calendarId, MidCalendarDateTagValueEnum.AIR_SEASON_SPRING);

        MidCalendarDateTagGlobalRuleBatchUpdateParam updateParam = new MidCalendarDateTagGlobalRuleBatchUpdateParam();
        updateParam.setCalendarId(calendarId);
        updateParam.setWorkDayTagRule(new MidCalendarGlobalDateWorkDateTagRuleUpdateParam(
                calendarId, MidCalendarWorkDayTypeEnum.WORKDAY, weekdays));
        updateParam.setAcSeasonTagRules(acSeasonTagRules);
        updateParam.setAirSeasonTagRules(airSeasonTagRules);
        updateParam.setAnniversaryRule(new MidCalendarGlobalAnniversaryRuleUpdateParam(calendarId, anniversaries));
        //新增节假日
        updateParam.setHolidayRule(new MidCalendarGlobalHolidayRuleUpdateParam(calendarId,param.getHolidayGeneralConfiguration(),param.getHolidaySpecialConfiguration()));
        midCalendarClientService.batchEditDateTagGlobalRule(projectId, updateParam);

        //编辑营业时间
        editBusinessTimeRange(projectId, param.getBusinessTimeRange());

        // 异步重置需求日历
        asyncResetTemplateCalendar(projectId);

        CachePersistenceKey.Key key = CacheKey.Persistence.Demand.globalRuleLastUpdateTime(projectId);
        redisCacheTemplate.object().set(key.getKey(), new DemandGlobalRuleLastUpdateTimeVO(DateUtils.now()));
        return true;
    }

    @Override
    public boolean editBusinessTime(@NonNull BusinessTimeRangeParam businessTimeRange) {
        editBusinessTimeRange(ContextUtil.getProjectId(), businessTimeRange);
        return true;
    }

    @Override
    public LinkedHashSet<String> listBusinessTimeByObjId(CalendarDayPlanByObjIdParam objIdParam) {
        return RangeUtils.toString(getBusinessTimeByObjId(objIdParam),
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }


    public LinkedHashSet<Range<LocalTime>> getBusinessTimeByObjId(CalendarDayPlanByObjIdParam objIdParam) {
        MidCalendarQueryParam param = new MidCalendarQueryParam();
        param.setObjId(objIdParam.getObjId());
        MidCalendarOpenTimeResponse openTime = midCalendarClientService.getMidCalendarOpenTimeByDay(param, objIdParam.getDate());
        if (openTime == null) {
            return null;
        }
        LinkedHashSet<Range<LocalTime>> localTimeList = new LinkedHashSet<>();
        openTime.getTimeTable().forEach(item -> {
            if (StringUtils.isEmpty(item.getWorkTime())) {
                return;
            }
            String[] times = item.getWorkTime().split("-");
            if (times.length == 2) {
                localTimeList.add(Range.between(LocalTime.parse(times[0]), LocalTime.parse(times[1])));
            }
        });


        return localTimeList;
    }

    @Override
    public boolean isInBusinessTime(LinkedHashSet<Range<LocalTime>> date, LocalTime time) {
        if (CollectionUtils.isNotEmpty(date)) {
            for (Range<LocalTime> range : date) {
                if (range.contains(time)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/7 14:34
    　　 * @description: 特殊对象配置-批量删除配置内容
    */
    @Override
    public boolean batchDeleteSpecialObjectConfiguration(BatchDeleteSpecialObjectConfigurationParam batchDelete) {
        //先查询所有实例的工作里集合
        List<MidCalendarDTO> midCalendarDTOS = midCalendarClientService.listCalendar(batchDelete.getProjectId(), new MidCalendarQueryParam());
        //获取工作历id集合
        List<String> calendarIds = midCalendarDTOS.stream().filter(it -> {
           if(batchDelete.getInstanceIds().contains(it.getObjectId())){
               return Boolean.TRUE;
           }
           //不在删除列表里面的就过滤掉
           return Boolean.FALSE;
        }).map(MidCalendarDTO::getCalendarId).distinct().collect(Collectors.toList());
        midCalendarClientService.delBatchCalendar(batchDelete.getProjectId(), calendarIds);
        return Boolean.TRUE;
    }

    /**
     * 编辑营业时间
     *
     * @param projectId
     * @param businessTimeRange
     */
    private void editBusinessTimeRange(String projectId, BusinessTimeRangeParam businessTimeRange) {
        if (StringUtils.isEmpty(businessTimeRange.getInstanceId())) {
            return;
        }
        MidCalendarDTO objCalendar = midCalendarClientService.getCalendar(projectId, new MidCalendarQueryParam().setObjId(businessTimeRange.getInstanceId()));
        //清除配置 清除对应工作历 只能清除非项目级别的工作历
        if (businessTimeRange.getIsDelete() != null && businessTimeRange.getIsDelete()) {
            if (objCalendar != null && StringUtils.isNotEmpty(objCalendar.getCalendarId())
                    && StringUtils.equals(businessTimeRange.getInstanceId(), objCalendar.getObjectId())) {
                midCalendarClientService.delCalendar(projectId, objCalendar.getCalendarId());
                return;
            }
            return;
        }
        String calendarId;
        //如果没有工作历 先进行创建
        if (objCalendar == null || !StringUtils.equals(businessTimeRange.getInstanceId(), objCalendar.getObjectId())) {
            calendarId = midCalendarClientService.addCalendar(projectId, businessTimeRange.getInstanceId());
        } else {
            calendarId = objCalendar.getCalendarId();
        }
        //编辑营业时间
        List<MidCalendarGlobalOpeningRuleUpdateParam> editOpeningParams = new ArrayList<>();
        if (MidCalendarDateTagTypeEnum.WORKDAY.equals(businessTimeRange.getDateTagType())) {
            editOpeningParams.add(new MidCalendarGlobalOpeningRuleUpdateParam(
                    MidCalendarDateTagTypeEnum.WORKDAY, MidCalendarWorkDayTypeEnum.WORKDAY,
                    businessTimeRange.getWorkdayBusinessTimeRangeList(),null,null));
            editOpeningParams.add(new MidCalendarGlobalOpeningRuleUpdateParam(
                    MidCalendarDateTagTypeEnum.WORKDAY, MidCalendarWorkDayTypeEnum.DAYOFF,
                    businessTimeRange.getDayOffBusinessTimeRangeList(),null,null));
        } else if(MidCalendarDateTagTypeEnum.WEEK.equals(businessTimeRange.getDateTagType())){
            //新增星期号
            LinkedHashSet<BusinessTimeRangeParam.WeekNumberParam> weekNumberParamList = businessTimeRange.getWeekNumberParamList();
            weekNumberParamList.forEach(it -> {
                LinkedHashSet<DayOfWeek> weekdays = it.getWeekdays();
                String periodDate = null;
                if(CollectionUtils.isNotEmpty(weekdays)){
                    periodDate = weekdays
                            .stream()
                            .map(midCalendarConvert::toCalendarDayOfWeekString)
                            .collect(Collectors.joining(","));
                }
                editOpeningParams.add(new MidCalendarGlobalOpeningRuleUpdateParam(
                        MidCalendarDateTagTypeEnum.WEEK, null,
                        it.getWeekNumberBusinessTimeRangeList(),MidCalendarDateTagValueEnum.WEEK,periodDate));
            });
        }else {
            editOpeningParams.add(new MidCalendarGlobalOpeningRuleUpdateParam(
                    MidCalendarDateTagTypeEnum.DEFAULT, null,
                    businessTimeRange.getBusinessTimeRangeList(),null,null));
        }

        //特殊对象配置-特殊配置内容
        LinkedHashSet<BusinessTimeRangeParam.SpecialConfigurationContentParam> specialConfigurationContentParamList = businessTimeRange.getSpecialConfigurationContentParamList();
        if(CollectionUtils.isNotEmpty(specialConfigurationContentParamList)){
            List<MidCalendarGlobalOpeningRuleUpdateParam> midCalendarGlobalOpeningRuleUpdateParamList = Lists.newArrayList();
            for (BusinessTimeRangeParam.SpecialConfigurationContentParam specialConfigurationContentParam : specialConfigurationContentParamList) {
                Range<LocalDate> timePeriod = specialConfigurationContentParam.getTimePeriod();
                LinkedHashSet<Range<LocalTime>> specialConfigurationContentBusinessTimeRangeList = specialConfigurationContentParam.getSpecialConfigurationContentBusinessTimeRangeList();
                midCalendarGlobalOpeningRuleUpdateParamList.add(new MidCalendarGlobalOpeningRuleUpdateParam(
                        MidCalendarDateTagTypeEnum.DEFAULT, null,
                        specialConfigurationContentBusinessTimeRangeList,null,String.join(DEFAULT_RANGE_SPLIT_REGEX, timePeriod.getMinimum().format(DatePattern.NORM_DATE_FORMATTER),timePeriod.getMaximum().format(DatePattern.NORM_DATE_FORMATTER))));
            }
            if(CollectionUtils.isNotEmpty(editOpeningParams)){
                //将特殊配置放在最前面
                editOpeningParams.addAll(0,midCalendarGlobalOpeningRuleUpdateParamList);
            }
        }
        midCalendarClientService.editGlobalOpeningRule(projectId, calendarId, true, editOpeningParams);
    }




    private void addAcSeasonTagRules(LinkedHashSet<Range<MonthDay>> notCoolingAndHeatingSeasonDateRangeList,
                                     List<MidCalendarGlobalDateAcSeasonTagRuleUpdateParam> acSeasonTagRules,
                                     String calendarId, MidCalendarDateTagValueEnum seasonTransition) {
        if (Objects.nonNull(notCoolingAndHeatingSeasonDateRangeList)) {
            acSeasonTagRules.add(new MidCalendarGlobalDateAcSeasonTagRuleUpdateParam(
                    calendarId, seasonTransition, notCoolingAndHeatingSeasonDateRangeList));
        }
    }

    private void addAirSeasonTagRules(LinkedHashSet<Range<MonthDay>> notCoolingAndHeatingSeasonDateRangeList,
                                      List<MidCalendarGlobalDateAirSeasonTagRuleUpdateParam> acSeasonTagRules,
                                      String calendarId, MidCalendarDateTagValueEnum seasonTransition) {
        if (Objects.nonNull(notCoolingAndHeatingSeasonDateRangeList)) {
            acSeasonTagRules.add(new MidCalendarGlobalDateAirSeasonTagRuleUpdateParam(
                    calendarId, seasonTransition, notCoolingAndHeatingSeasonDateRangeList));
        }
    }

    /**
     * 异步重置需求日历
     *
     * @param projectId 项目ID
     * @since v6.0.0
     */
    private void asyncResetTemplateCalendar(@NonNull String projectId) {
        List<Long> templateIds = new ArrayList<>();
        for (DemandTemplateTypeEnum templateType : DemandTemplateTypeEnum.values()) {
            DemandTemplateBatchQueryParam templateBatchQueryParam = new DemandTemplateBatchQueryParam();
            templateBatchQueryParam.setTemplateType(templateType);
            templateBatchQueryParam.setEnabled(true);
            List<DemandTemplateVO> templates = demandCalendarQueryService.listTemplate(templateBatchQueryParam);
            if (CollectionUtils.isNotEmpty(templates)) {
                templateIds.addAll(templates.stream().map(DemandTemplateVO::getTemplateId).collect(Collectors.toList()));
            }
        }
        if (CollectionUtils.isEmpty(templateIds)) {
            return;
        }
        TtlExecutors.getTtlExecutor(executor).execute(() -> demandCalendarService.resetTemplateCalendarByTemplate(
                projectId, templateIds, null, false));
    }
}
