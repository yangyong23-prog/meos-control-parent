package com.persagy.apccontrol.basic.structure.bizcheck.strategy.impl;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.param.bizcheck.NotifyPeriodConfigParam;
import com.persagy.apccontrol.basic.model.param.midcalendar.MidCalendarWorkTimeBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.midcalendar.MidCalendarWorkTimeVO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.service.midcalendar.MidCalendarService;
import com.persagy.apccontrol.basic.structure.bizcheck.context.BizCheckContext;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStrategyTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.BizCheckItemDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.model.dto.WorkCalendarCheckDTO;
import com.persagy.apccontrol.basic.structure.bizcheck.strategy.BizCheckStrategy;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工作日历检查策略实现
 * 包含3个检查项：
 * 1. 系统服务空间营业时间（返回系统名称和营业时间数组）
 * 2. 当日所属的空调季（返回枚举code）
 * 3. 当日空调工况（返回枚举code）
 *
 * @author JiYinchuan
 * @since 7.260301.0
 */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WorkCalendarCheckStrategy implements BizCheckStrategy<WorkCalendarCheckDTO> {

    private final MidCalendarService midCalendarService;
    private final MidCalendarClientService midCalendarClientService;

    @Override
    public BizCheckStrategyTypeEnum getStrategyType() {
        return BizCheckStrategyTypeEnum.WORK_CALENDAR;
    }

    @Override
    public BizCheckItemDTO<WorkCalendarCheckDTO> execute(BizCheckContext context) {
        LocalDate today = context.getCheckDate();
        LocalDate tomorrow = today.plusDays(1);
        List<LocalDate> checkDates = new ArrayList<>();
        checkDates.add(today);
        checkDates.add(tomorrow);

        List<NotifyPeriodConfigParam> notifyConfigs = context.getNotifyPeriodConfigs();

        WorkCalendarCheckDTO checkResult = new WorkCalendarCheckDTO();

        // 1. 系统服务空间营业时间检查
        this.buildSystemBusinessCheck(checkResult, notifyConfigs, checkDates);

        // 2. 空调季检查
        this.buildAirSeasonCheck(checkResult, checkDates);

        // 3. 空调工况检查
        this.buildAcSeasonCheck(checkResult, checkDates);

        BizCheckItemDTO<WorkCalendarCheckDTO> checkItemDTO = new BizCheckItemDTO<>();
        checkItemDTO.setCheckResult(checkResult);
        checkItemDTO.setStatus(checkResult.anyException() ? BizCheckStatusEnum.REJECTED : BizCheckStatusEnum.NORMAL);
        return checkItemDTO;
    }

    /**
     * 构建系统服务空间营业时间检查
     *
     * @param checkResult   检查结果对象
     * @param notifyConfigs 通知周期配置
     * @param checkDates    检查日期
     * @since 7.260301.0
     */
    private void buildSystemBusinessCheck(
            @NonNull WorkCalendarCheckDTO checkResult,
            @NonNull List<NotifyPeriodConfigParam> notifyConfigs,
            @NonNull List<LocalDate> checkDates) {
        if (CollectionUtils.isEmpty(notifyConfigs)) {
            checkResult.setDate2SystemBusiness(Collections.emptyMap());
            checkResult.setSystemBusinessStatus(BizCheckStatusEnum.TO_BE_CHECKED);
            return;
        }

        // 收集所有系统ID，批量查询营业时间
        Set<String> allSystemIds = notifyConfigs.stream()
                .map(NotifyPeriodConfigParam::getId)
                .collect(Collectors.toSet());

        // 查询当日+次日的营业时间
        LocalDate minDate = checkDates.get(0);
        LocalDate maxDate = checkDates.get(checkDates.size() - 1);
        MidCalendarWorkTimeBatchQueryParam queryParam = new MidCalendarWorkTimeBatchQueryParam();
        queryParam.setObjectIds(allSystemIds);
        queryParam.setBeginDate(minDate);
        queryParam.setEndDate(maxDate.plusDays(1));
        List<MidCalendarWorkTimeVO> workTimes = midCalendarService.listWorkTimes(queryParam);

        Map<LocalDate, MidCalendarWorkTimeVO> workTimeByDate = workTimes.stream().collect(Collectors.toMap(
                MidCalendarWorkTimeVO::getDate, Function.identity(),
                (v1, v2) -> v2));

        Map<LocalDate, List<WorkCalendarCheckDTO.SystemBusinessCheckDTO>> date2SystemBusiness = new HashMap<>();
        boolean isAnyException = false;

        for (LocalDate date : checkDates) {
            int dayOfWeek = date.getDayOfWeek().getValue();

            List<NotifyPeriodConfigParam> systemsForDay = notifyConfigs.stream()
                    .filter(c -> CollectionUtils.isNotEmpty(c.getDays()))
                    .filter(c -> c.getDays().contains(dayOfWeek))
                    .sorted(Comparator.comparing(NotifyPeriodConfigParam::getLocalName,
                            Comparator.nullsLast(Collator.getInstance(Locale.CHINA))))
                    .collect(Collectors.toList());

            if (systemsForDay.isEmpty()) {
                date2SystemBusiness.put(date, Collections.emptyList());
                continue;
            }

            // 当日工作历营业时间数据
            MidCalendarWorkTimeVO dayWorkTime = workTimeByDate.get(date);
            // systemId -> List<detail>
            Map<String, List<MidCalendarWorkTimeVO.MidCalendarWorkTimeDetailVO>> detailsBySystem = Optional.ofNullable(dayWorkTime)
                    .map(MidCalendarWorkTimeVO::getDetails)
                    .filter(CollectionUtils::isNotEmpty)
                    .map(details -> details.stream().collect(Collectors.groupingBy(
                            MidCalendarWorkTimeVO.MidCalendarWorkTimeDetailVO::getObjectId)))
                    .orElseGet(Collections::emptyMap);

            List<WorkCalendarCheckDTO.SystemBusinessCheckDTO> systemCheckList = new ArrayList<>();
            boolean anyEmpty = false;

            for (NotifyPeriodConfigParam config : systemsForDay) {
                WorkCalendarCheckDTO.SystemBusinessCheckDTO systemCheck = new WorkCalendarCheckDTO.SystemBusinessCheckDTO();

                DtpRwdObjectDTO objectDTO = new DtpRwdObjectDTO();
                objectDTO.setId(config.getId());
                objectDTO.setLocalName(config.getLocalName());
                systemCheck.setObject(objectDTO);

                List<MidCalendarWorkTimeVO.MidCalendarWorkTimeDetailVO> details = detailsBySystem.get(config.getId());
                if (CollectionUtils.isEmpty(details)) {
                    systemCheck.setSpaceBusinessHours(Collections.emptyList());
                    anyEmpty = true;
                } else {
                    List<Range<LocalTime>> timeRanges = details.stream()
                            .filter(d -> d.getBeginWorkTime() != null && d.getEndWorkTime() != null)
                            .map(d -> Range.between(d.getBeginWorkTime(), d.getEndWorkTime()))
                            .collect(Collectors.toList());
                    if (timeRanges.isEmpty()) {
                        anyEmpty = true;
                    }
                    systemCheck.setSpaceBusinessHours(timeRanges);
                }
                systemCheckList.add(systemCheck);
            }

            date2SystemBusiness.put(date, systemCheckList);
            if (anyEmpty) {
                isAnyException = true;
            }
        }

        checkResult.setDate2SystemBusiness(date2SystemBusiness);
        checkResult.setSystemBusinessStatus(isAnyException ? BizCheckStatusEnum.EXCEPTION : BizCheckStatusEnum.NORMAL);
    }

    /**
     * 构建空调季检查
     *
     * @param checkResult 检查结果对象
     * @param checkDates  检查日期
     * @since 7.260301.0
     */
    private void buildAirSeasonCheck(
            @NonNull WorkCalendarCheckDTO checkResult,
            @NonNull List<LocalDate> checkDates) {
        String projectId = ContextUtil.getProjectId();
        Map<LocalDate, MidCalendarAirSeasonTypeEnum> date2AirSeasonType = new HashMap<>();
        boolean isAnyException = false;

        for (LocalDate date : checkDates) {
            MidCalendarAirSeasonTypeEnum airSeason = midCalendarClientService.getAirSeasonType(projectId, date);
            date2AirSeasonType.put(date, airSeason);
            if (airSeason == null) {
                isAnyException = true;
            }
        }

        checkResult.setDate2AirSeasonType(date2AirSeasonType);
        checkResult.setAirSeasonStatus(isAnyException ? BizCheckStatusEnum.EXCEPTION : BizCheckStatusEnum.NORMAL);
    }

    /**
     * 构建空调工况检查
     *
     * @param checkResult 检查结果对象
     * @param checkDates  检查日期
     * @since 7.260301.0
     */
    private void buildAcSeasonCheck(
            @NonNull WorkCalendarCheckDTO checkResult,
            @NonNull List<LocalDate> checkDates) {
        String projectId = ContextUtil.getProjectId();
        Map<LocalDate, MidCalendarAcSeasonTypeEnum> date2AcSeasonType = new HashMap<>();
        boolean isAnyException = false;

        for (LocalDate date : checkDates) {
            MidCalendarAcSeasonTypeEnum acSeason = midCalendarClientService.getAcSeasonType(projectId, projectId, date);
            date2AcSeasonType.put(date, acSeason);
            boolean isCooling = MidCalendarAcSeasonTypeEnum.SEASON_COOLING.equals(acSeason);
            if (!isCooling) {
                isAnyException = true;
            }
        }

        checkResult.setDate2AcSeasonType(date2AcSeasonType);
        checkResult.setAcSeasonStatus(isAnyException ? BizCheckStatusEnum.EXCEPTION : BizCheckStatusEnum.NORMAL);
    }

    @Override
    public String logTag() {
        return "工作日历检查";
    }
}
