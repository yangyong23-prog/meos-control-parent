package com.persagy.apccontrol.basic.service.midcalendar;

import com.persagy.apccontrol.basic.model.dto.calendar.MidCalendarWorkTimeDTO;
import com.persagy.apccontrol.basic.model.param.midcalendar.MidCalendarWorkTimeBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.midcalendar.MidCalendarWorkTimeVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarWorkTimeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作历服务实现
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MidCalendarServiceImpl implements MidCalendarService {

    private final MidCalendarClientService midCalendarClientService;

    @Override
    public List<MidCalendarWorkTimeVO> listWorkTimes(@NonNull MidCalendarWorkTimeBatchQueryParam param) {
        Set<String> objectIds = param.getObjectIds();
        LocalDate beginDate = param.getBeginDate();
        LocalDate endDate = param.getEndDate();
        Map<LocalDate, MidCalendarWorkTimeVO> workTimeAndData = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String objectId : objectIds) {
            futures.add(CompletableFuture.runAsync(() -> {
                MidCalendarWorkTimeQueryParam workTimeQueryParam = new MidCalendarWorkTimeQueryParam();
                workTimeQueryParam.setObjId(objectId);
                workTimeQueryParam.setWorkDateStart(beginDate);
                workTimeQueryParam.setWorkDateEnd(endDate);
                List<MidCalendarWorkTimeDTO> workTimes = midCalendarClientService.listCalendarWorkTime(workTimeQueryParam);
                if (CollectionUtils.isEmpty(workTimes)) {
                    return;
                }
                for (MidCalendarWorkTimeDTO workTime : workTimes) {
                    LocalDate date = workTime.getDate();
                    Boolean workday = workTime.getWorkday();
                    Boolean anniversary = workTime.getAnniversary();
                    String workId = workTime.getWorkId();
                    LocalTime beginWorkTime = workTime.getBeginWorkTime();
                    LocalTime endWorkTime = workTime.getEndWorkTime();
                    MidCalendarWorkTimeVO dateWorkTime = Optional.ofNullable(workTimeAndData.get(date)).orElseGet(() -> {
                        MidCalendarWorkTimeVO tempDateWorkTime = new MidCalendarWorkTimeVO();
                        tempDateWorkTime.setDate(date);
                        tempDateWorkTime.setWorkday(workday);
                        tempDateWorkTime.setAnniversary(anniversary);
                        return tempDateWorkTime;
                    });
                    List<MidCalendarWorkTimeVO.MidCalendarWorkTimeDetailVO> details = Optional.ofNullable(
                            dateWorkTime.getDetails()).orElseGet(() -> Collections.synchronizedList(new ArrayList<>()));
                    MidCalendarWorkTimeVO.MidCalendarWorkTimeDetailVO tempDetail =
                            new MidCalendarWorkTimeVO.MidCalendarWorkTimeDetailVO();
                    tempDetail.setObjectId(objectId);
                    tempDetail.setWorkId(workId);
                    tempDetail.setBeginWorkTime(beginWorkTime);
                    tempDetail.setEndWorkTime(endWorkTime);
                    details.add(tempDetail);
                    dateWorkTime.setDetails(details);
                    workTimeAndData.put(date, dateWorkTime);
                }
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return new ArrayList<>(workTimeAndData.values());
    }
}
