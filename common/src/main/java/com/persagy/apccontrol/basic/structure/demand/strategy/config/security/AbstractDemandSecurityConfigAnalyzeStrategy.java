package com.persagy.apccontrol.basic.structure.demand.strategy.config.security;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTimeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import com.persagy.apccontrol.basic.structure.demand.strategy.config.AbstractDemandConfigAnalyzeStrategy;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 抽象需求配置策略
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
public abstract class AbstractDemandSecurityConfigAnalyzeStrategy<T extends BaseDemandConfig>
        extends AbstractDemandConfigAnalyzeStrategy<T> {

    List<DemandTimeIntervalValueObject> calculateNotGivenDemandTimeIntervalValue(
            List<DemandTimeIntervalValueObject> sourceTimeIntervalValueList) {
        for (DemandTimeIntervalValueObject demandTimeIntervalValueObject : sourceTimeIntervalValueList) {
            if (BooleanUtils.isTrue(demandTimeIntervalValueObject.getAcrossDay())) {
                //如果有跨天的 并且 结束时间和开始时间都为0，则表示配置中有全天的，不需要计算了
                int compareEnd = demandTimeIntervalValueObject.getEndTime().getAbsoluteTime().compareTo(LocalTime.MIN);
                int compareBegin = demandTimeIntervalValueObject.getBeginTime().getAbsoluteTime().compareTo(LocalTime.MIN);
                if (compareBegin == 0 && compareEnd == 0) {
                    return new ArrayList<>();
                }
            }
        }
        List<DemandTimeIntervalValueObject> notGivenDemandTimeIntervals = new ArrayList<>();
        List<Pair<LocalTime, LocalTime>> sourceTimeIntervals = new ArrayList<>();
        sourceTimeIntervalValueList.forEach(sourceTimeInterval -> {
            if (sourceTimeInterval.getAcrossDay() != null && sourceTimeInterval.getAcrossDay()) {
                int compare = sourceTimeInterval.getEndTime().getAbsoluteTime().compareTo(
                        sourceTimeInterval.getBeginTime().getAbsoluteTime());
                if (compare > 0) {
                    logInstance().warn("跨天配置，结束时间大于开始时间");
                    throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_6298869daf274a77b21afee55f9f41e4"));
                }
                //有跨天的时间段，拆分成两个时间段
                sourceTimeIntervals.add(Pair.of(sourceTimeInterval.getBeginTime().getAbsoluteTime(),
                        LocalTime.MIN));
                sourceTimeIntervals.add(Pair.of(LocalTime.MIN, sourceTimeInterval.getEndTime().getAbsoluteTime()));
            } else {
                sourceTimeIntervals.add(Pair.of(sourceTimeInterval.getBeginTime().getAbsoluteTime(),
                        sourceTimeInterval.getEndTime().getAbsoluteTime()));
            }
        });
        List<Pair<LocalTime, LocalTime>> notGivenIntervals = calculateNotGivenIntervals(sourceTimeIntervals);
        if (CollectionUtils.isNotEmpty(notGivenIntervals)) {
            notGivenIntervals.forEach(notGivenInterval -> {
                DemandTimeIntervalValueObject timeInterval = new DemandTimeIntervalValueObject();
                DemandTimeValueObject beginTime = new DemandTimeValueObject();
                DemandTimeValueObject endTime = new DemandTimeValueObject();
                beginTime.setTimeType(DemandTimeTypeEnum.ABSOLUTE);
                endTime.setTimeType(DemandTimeTypeEnum.ABSOLUTE);
                beginTime.setAbsoluteTime(notGivenInterval.getLeft());
                endTime.setAbsoluteTime(notGivenInterval.getRight());
                timeInterval.setAcrossDay(false);
                timeInterval.setBeginTime(beginTime);
                timeInterval.setEndTime(endTime);
                notGivenDemandTimeIntervals.add(timeInterval);
            });
        }
        return notGivenDemandTimeIntervals;
    }

    /**
     * @param intervals 给定的时间列表 形如01:00:00 - 02:00:00、03:00:00 - 04:00:00
     * @return 返回一天内不在给定的intervals列表中的时间段
     */
    public static List<Pair<LocalTime, LocalTime>> calculateNotGivenIntervals(
            List<Pair<LocalTime, LocalTime>> intervals) {
        intervals.sort(Comparator.comparing(Pair::getLeft));
        List<Pair<LocalTime, LocalTime>> result = new ArrayList<>();
        // 添加初始时间间隔（00:00:00 到 第一个时间段开始前）
        if (!intervals.isEmpty()) {
            if (!intervals.get(0).getLeft().equals(LocalTime.MIDNIGHT)) {
                Pair<LocalTime, LocalTime> firstInterval = intervals.get(0);
                result.add(Pair.of(LocalTime.MIDNIGHT, firstInterval.getLeft()));
            }

        }
        // 添加中间时间间隔（两个相邻时间段之间）
        for (int i = 0; i < intervals.size() - 1; i++) {
            Pair<LocalTime, LocalTime> currentInterval = intervals.get(i);
            Pair<LocalTime, LocalTime> nextInterval = intervals.get(i + 1);
            // 判断两个时间段之间是否有空隙
            if (!currentInterval.getRight().equals(nextInterval.getLeft())) {
                result.add(Pair.of(currentInterval.getRight(), nextInterval.getLeft()));
            }
        }
        if (!intervals.isEmpty()) {
            Pair<LocalTime, LocalTime> lastInterval = intervals.get(intervals.size() - 1);
            //最后一个时间段为00:00:00，则不添加
            if (!lastInterval.getRight().equals(LocalTime.MIN)) {
                result.add(Pair.of(lastInterval.getRight(), LocalTime.MIN));
            }
        }
        return result;
    }
}
