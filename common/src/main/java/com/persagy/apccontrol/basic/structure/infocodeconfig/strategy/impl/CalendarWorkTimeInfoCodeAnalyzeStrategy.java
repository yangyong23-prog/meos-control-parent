package com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.impl;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarWorkTimeDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarWorkTimeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.InfoCodeAnalyzeStrategy;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 工作历-营业时间信息点解析策略
 *
 * @author JiYinchuan
 * @since 5.240901.0
 */
@Slf4j
@Component(CalendarWorkTimeInfoCodeAnalyzeStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CalendarWorkTimeInfoCodeAnalyzeStrategy
        extends BaseInfoCodeAnalyzeStrategy
        implements InfoCodeAnalyzeStrategy {

    public static final String BEAN_NAME = "calendarWorkTimeInfoCodeAnalyzeStrategy";

    private final MidCalendarClientService midCalendarClientService;

    @Override
    public InfoCodeTypeEnum infoCodeType() {
        return InfoCodeTypeEnum.CALENDAR_WORK_TIME;
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
        Map<String, List<MidCalendarWorkTimeDTO>> objectIdAndWorkTimes = new ConcurrentHashMap<>();

        LocalDate today = DateUtils.now().toLocalDate();
        LocalDate tomorrow = today.plusDays(1);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String objectId : objectIds) {
            futures.add(CompletableFuture.runAsync(() -> {
                MidCalendarWorkTimeQueryParam queryParam = new MidCalendarWorkTimeQueryParam();
                queryParam.setObjId(objectId);
                queryParam.setWorkDateStart(today);
                queryParam.setWorkDateEnd(tomorrow);
                List<MidCalendarWorkTimeDTO> workTimes = midCalendarClientService.listCalendarWorkTime(queryParam);
                if (CollectionUtils.isNotEmpty(workTimes)) {
                    objectIdAndWorkTimes.put(objectId, workTimes);
                }
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (InfoCodeAggregate aggregate : aggregates) {
            String objectId = aggregate.getIdentifier().getObject().getId();
            String infoCode = aggregate.getConfigModel().getInfoCode();

            List<MidCalendarWorkTimeDTO> workTimes = objectIdAndWorkTimes.get(objectId);

            if (CollectionUtils.isNotEmpty(workTimes)) {
                List<String> workTimesByString = workTimes.stream().map(item -> {
                    String beginTimeString = MidCalendarClientService.OPENING_TIME_FORMATTER.format(item.getBeginWorkTime());
                    String endTimeString = MidCalendarClientService.OPENING_TIME_FORMATTER.format(item.getEndWorkTime());
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
