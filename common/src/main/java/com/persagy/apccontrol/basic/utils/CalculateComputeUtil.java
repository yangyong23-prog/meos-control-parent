package com.persagy.apccontrol.basic.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import com.persagy.apccontrol.basic.model.enumtype.EnumSpaceBusinessModel;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningBaseConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObjectSpaceDTO;
import com.persagy.validate.utility.enumeration.TimeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/14 11:39
 */
@Slf4j
public class CalculateComputeUtil {

    /**
     * 转换时间格式
     *
     * @param responseRegulationTimeIntervals
     */
    public static List<LocalTime> convertTimeIntervals(List<DemandTimeIntervalValueObject> responseRegulationTimeIntervals) {
        List<LocalTime> realResponseTime = new ArrayList<>();
        if (CollectionUtil.isEmpty(responseRegulationTimeIntervals)) {
            return realResponseTime;
        }
        List<Range<LocalTime>> responseDemandTimeRangeList = new ArrayList<>();
        for (DemandTimeIntervalValueObject responseRegulationTimeInterval : responseRegulationTimeIntervals) {
            List<Range<LocalTime>> tempRangeList = responseRegulationTimeInterval.analyzeTimeInterval(TimeTypeEnum.MINUTE, null);
            responseDemandTimeRangeList.addAll(tempRangeList);
        }

        mergeBusinessTime(responseDemandTimeRangeList, realResponseTime, true);
        return realResponseTime;
    }

    /**
     * 解析当前空间配置
     *
     * <p>
     *     1-有营业时间、无空调需求且非24h营业的空间
     * <p>
     *     2-有空调需求时间设置
     * <p>
     *     3-24h营业空间
     * @param businessTime
     * @param objectSpaceDTO
     * @return
     */
    public static EnumSpaceBusinessModel parseCurrentSpaceConfig(List<LocalTime> businessTime, DemandConfigAirconditioningValueObjectSpaceDTO objectSpaceDTO) {
        EnumSpaceBusinessModel businessModel = null;
        if (objectSpaceDTO == null || objectSpaceDTO.getGlobalConfig() == null) {
            return null;
        }

        // 1.优先判断是否存在空调需求时间
        List<DemandTimeIntervalValueObject> airConditioningDemandTimeIntervals = null;
        DemandConfigAirconditioningBaseConfigValueObject baseConfig = objectSpaceDTO.getGlobalConfig().getBaseConfig();
        if (baseConfig != null && !BooleanUtil.isTrue(baseConfig.getIsAlgorithmCompute()) && CollectionUtil.isNotEmpty(baseConfig.getAirConditioningDemandTimeIntervals())) {
            airConditioningDemandTimeIntervals = baseConfig.getAirConditioningDemandTimeIntervals();
        }

        // 1.判断当前空间是否存在空调需求时间的设置
        boolean airCondition = CollectionUtil.isNotEmpty(airConditioningDemandTimeIntervals);
        if (airCondition) {
            List<Range<LocalTime>> airConditioningDemandTimeRangeList = new ArrayList<>();
            for (DemandTimeIntervalValueObject airConditioningDemandTimeInterval : airConditioningDemandTimeIntervals) {
                List<Range<LocalTime>> tempRangeList = airConditioningDemandTimeInterval.analyzeTimeInterval(TimeTypeEnum.MINUTE, null);
                airConditioningDemandTimeRangeList.addAll(tempRangeList);
            }
            // 当存在空调需求时间的时候
            boolean allTheTime = mergeBusinessTime(airConditioningDemandTimeRangeList, businessTime, true);
            businessModel = allTheTime ? EnumSpaceBusinessModel.ALL_THE_BUSINESS_MODEL : EnumSpaceBusinessModel.AIR_CONDITION_BUSINESS_MODEL;
        } else if (CollectionUtil.isNotEmpty(objectSpaceDTO.getBusinessTimeRangeList())) {
            // 2.判断当前空间所属日的类型，并进行解析判断是否是24h营业时间
            boolean allTheTime = mergeBusinessTime(objectSpaceDTO.getBusinessTimeRangeList(), businessTime, false);
            businessModel = allTheTime ? EnumSpaceBusinessModel.ALL_THE_BUSINESS_MODEL : EnumSpaceBusinessModel.INTERMITTENT_BUSINESS_MODEL;
        }
        // 初始的时候，开关机时间为设置的开关机时间--即营业时间
        return businessModel;
    }

    /**
     * 将无序且实际连续、显示不连续的营业时间，进行合并优化
     *
     * @param businessTimeRangeList 前端配置的营业时间数据
     * @param realBusinessTime 解析后填充
     * @return true-24h营业时间，false-非24营业时间
     */
    public static boolean mergeBusinessTime(List<Range<LocalTime>> businessTimeRangeList, List<LocalTime> realBusinessTime, boolean isAirCondition) {
        if (CollectionUtil.isEmpty(businessTimeRangeList)) {
            return false;
        }
        // 按照开始时间进行排序
        Collections.sort(businessTimeRangeList, new Comparator<Range<LocalTime>>() {
                @Override
                public int compare(Range<LocalTime> o1, Range<LocalTime> o2) {
                    return o1.getMinimum().compareTo(o2.getMinimum());
                }
            }
        );

        // 营业时间合并，同时判断是否属于连续时间，如果是，再次进行合并
        LocalTime early = LocalTime.parse("00:00");
        LocalTime latest = LocalTime.parse("23:59");
        LocalTime latestAir = LocalTime.parse("23:59");
        List<Range<LocalTime>> mergeRangeList = new ArrayList<>();
        Range<LocalTime> temp = businessTimeRangeList.get(0);
        for (int i = 1; i < businessTimeRangeList.size(); ++i) {
            Range<LocalTime> businessTime = businessTimeRangeList.get(i);
            LocalTime offsetTime = businessTime.getMinimum().equals(early) ? early : businessTime.getMinimum().plusMinutes(-5);
            if (businessTime.getMinimum().compareTo(temp.getMaximum()) <= 0 || offsetTime.compareTo(temp.getMaximum()) <= 0) {
                temp = Range.between(temp.getMinimum(), (temp.getMaximum().compareTo(businessTime.getMaximum()) < 0) ? businessTime.getMaximum() : temp.getMaximum());
            } else {
                mergeRangeList.add(temp);
                temp = businessTime;
            }
        }
        mergeRangeList.add(temp);

        mergeRangeList.stream().forEach(item -> {
            realBusinessTime.add(item.getMinimum());
            realBusinessTime.add(item.getMaximum());
        });
        if (isAirCondition) {
            return realBusinessTime.size() == 2 && realBusinessTime.get(0).equals(early) && realBusinessTime.get(1).equals(latestAir);
        }
        return realBusinessTime.size() == 2 && realBusinessTime.get(0).equals(early) && realBusinessTime.get(1).equals(latest);
    }

    /**
     * 将判断当前营业时间是否是24h营业
     *
     * @param businessTime
     * @return true-24h营业时间，false-非24营业时间
     */
    public static boolean checkBusinessModel(List<LocalTime> businessTime) {
        if (businessTime == null || businessTime.size() != 2) {
            return false;
        }
        LocalTime early = LocalTime.parse("00:00");
        LocalTime latest = LocalTime.parse("23:55");
        return businessTime.get(0).equals(early) && businessTime.get(1).equals(latest);
    }

    /**
     * 检查当前时间点是否处于营业时间范围内
     *
     * @param workTime
     * @param businessTimes
     * @param containEnd 是否包含结束时间点
     * @return
     */
    public static boolean checkCurrentTimeBelongBusinessTime(LocalTime workTime, List<LocalTime> businessTimes, boolean containEnd) {
        if (workTime == null || CollectionUtil.isEmpty(businessTimes)) {
            return false;
        }
        for (int i = 0; i < businessTimes.size(); i = i + 2) {
            LocalTime start = businessTimes.get(i);
            LocalTime end = businessTimes.get(i + 1);
            if (!workTime.isBefore(start) && ((!workTime.isAfter(end) && containEnd) || (workTime.isBefore(end) && !containEnd))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按 5 分钟的时间跨度，为此段时间赋值上对应的数据
     *
     * @param startTime 秒数置空
     * @param endTime 秒数置空
     * @param interval 最低 5 分钟的间隔
     */
    public static List<LocalTime> spiltLocalTimeByMinute(LocalTime startTime, LocalTime endTime, int interval) {
        List<LocalTime> timeList = new ArrayList<>();

        LocalTime offsetTime = startTime;
        LocalTime end = LocalTime.MAX.withSecond(0).withNano(0).plusMinutes(-interval + 1);
        do {
            timeList.add(LocalTime.parse(offsetTime.toString()));
            if (offsetTime.equals(end)) {
                break;  // 当天结束了
            }
            offsetTime = offsetTime.plusMinutes(interval);
        } while (offsetTime.isBefore(endTime));
        return timeList;
    }

    /**
     * 进行时间分割
     *
     * @param startDateTime 包含当前时间点
     * @param endDateTime 不包含结束时间点
     * @param interval 时间间隔
     * @return
     */
    public static List<LocalDateTime> spiltDateTimeByMinute(LocalDateTime startDateTime, LocalDateTime endDateTime, int interval, boolean containEnd) {
        List<LocalDateTime> spiltDateTimes = new ArrayList<>();
        LocalDateTime tempDateTime = startDateTime;
        while (tempDateTime.isBefore(endDateTime) || (containEnd && tempDateTime.equals(endDateTime))) {
            spiltDateTimes.add(tempDateTime);
            tempDateTime = tempDateTime.plusMinutes(interval);
        }

        return spiltDateTimes;
    }
}
