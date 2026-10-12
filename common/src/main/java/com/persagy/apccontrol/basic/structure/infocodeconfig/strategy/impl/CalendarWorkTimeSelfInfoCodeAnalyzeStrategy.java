package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarWorkTimeDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarOpenTimeProjectDTO;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作历-自身营业时间信息点解析策略
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Slf4j
@Component(CalendarWorkTimeSelfInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CalendarWorkTimeSelfInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "calendarWorkTimeSelfInfoCodeAnalyzeStrategy";

    private final MidCalendarClientService midCalendarClientService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.CALENDAR_WORK_TIME_SELF;
    }

    @Override
    public void fillInfoCodeDefineModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        fillAggregateDefineOfEmpty(aggregates, String.class.getSimpleName());
    }

    @Override
    public void fillInfoCodeDataModel(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable Map<String, Object> additionalValues) {
        Set<String> objectIds = InfoCodeAggregate.listAllObjectIds(aggregates);
        if (CollectionUtils.isEmpty(objectIds)) {
            return;
        }
        List<MidCalendarOpenTimeProjectDTO> openTimeProjects = midCalendarClientService.listOpenTimeProject(DateUtils.now().toLocalDate());
        Map<String, List<Range<LocalTime>>> objectIdAndWorkTimeRanges = openTimeProjects.stream().collect(
                Collectors.toMap(MidCalendarOpenTimeProjectDTO::getObjectId,
                        MidCalendarOpenTimeProjectDTO::getWorkTimeRanges, (o1, o2) -> o1));

        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();

            List<Range<LocalTime>> workTimeRanges = objectIdAndWorkTimeRanges.get(objectId);
            if (CollectionUtils.isNotEmpty(workTimeRanges)) {
                List<String> workTimesByString = workTimeRanges.stream().map(item -> {
                    String beginTimeString = MidCalendarClientService.OPENING_TIME_FORMATTER.format(item.getMinimum());
                    String endTimeString = MidCalendarClientService.OPENING_TIME_FORMATTER.format(item.getMaximum());
                    return String.join(MidCalendarWorkTimeDTO.DEFAULT_WORK_TIME_SPLIT, beginTimeString, endTimeString);
                }).collect(Collectors.toList());
                aggregate.setInfoCodeAndDataModel(new InfoCodeDataModel(
                        infoCode, workTimesByString, null, null));
            }
        }
    }

    @Override
    public String logTag() {
        return "信息点配置-信息点解析策略-工作历营业时间";
    }
}
