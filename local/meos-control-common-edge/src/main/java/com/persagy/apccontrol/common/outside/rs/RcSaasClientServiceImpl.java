package com.persagy.apccontrol.common.outside.rs;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.calendar.MidCalendarDayDateTagQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.calendar.MidCalendarDayDateTagResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidCalendarClientService;
import com.persagy.apccontrol.basic.service.demand.DemandExternalService;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObjectSpaceDTO;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.common.model.param.strategy.chiller.QuerySeasonOrder;
import com.persagy.validate.utility.validate.range.DataRangeValidator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 云端客户端服务实现
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Service
@Slf4j
public class RcSaasClientServiceImpl implements RcSaasClientService {

    private static final String LOG_TAG = "云端客户端服务";

    @Autowired
    private DemandExternalService demandExternalService;

    @Autowired
    private MidCalendarClientService midCalendarClientService;

    @Override
    public List<String> getDemandConfigSpace(String groupCode, String projectId) {
        // 获取空间配置数据
        List<DemandConfigAirconditioningValueObjectSpaceDTO> objectInfoDTOS = this.demandExternalService.getDemandConfigAirconditioningValueObjectSpaceDTOs(DateUtils.now().toLocalDate(),
                null, null, null, false, true, true, false);
        List<String> lstSpaceId = new ArrayList<>();
        if (CollectionUtil.isEmpty(objectInfoDTOS)) {
            ControlLogUtil.warn(projectId + " airTerminalHandler.queryControlObjectList 获取数据为空");
            return lstSpaceId;
        }
        for (DemandConfigAirconditioningValueObjectSpaceDTO objectInfoDTO : objectInfoDTOS) {
            lstSpaceId.add(objectInfoDTO.getInstanceId());
        }
        if (lstSpaceId.size() == 0) {
            ControlLogUtil.warn(projectId + " getDemandConfigSpace 未获取到空间需求");
        }
        return lstSpaceId;
    }

    @Override
    public TreeMap<LocalDateTime, String> querySeasonByTime(QuerySeasonOrder param) {
        TreeMap<LocalDateTime, String> midCalendarAcSeasonTypeList = new TreeMap<>();

        LocalDate beginDate = param.getBeginDateTime();
        LocalDate endDate = param.getEndDateTime().minusDays(1);
        DataRangeValidator.date().allowFutureTime().validate(beginDate, endDate);

        MidCalendarDayDateTagQueryParam queryParam = new MidCalendarDayDateTagQueryParam();
        queryParam.setObjId(param.getProjectId());
        queryParam.setBeginDate(beginDate);
        queryParam.setEndDate(endDate);
        List<MidCalendarDayDateTagResponse> dayPlans = midCalendarClientService.listCalendarDayDateTag(param.getProjectId(), queryParam);
        if (CollectionUtil.isEmpty(dayPlans)) {
            return midCalendarAcSeasonTypeList;
        }
        dayPlans.forEach(i -> {
            if (CollectionUtil.isNotEmpty(i.getTagList())) {
                i.getTagList().forEach(b -> {
                    MidCalendarAcSeasonTypeEnum season = MidCalendarAcSeasonTypeEnum.getEnumByCode(b);
                    if (season != null) {
                        midCalendarAcSeasonTypeList.put(i.getCurrentDay(), season.getReasonPhrase());
                    }
                });
            }
        });
        return midCalendarAcSeasonTypeList;
    }

    @Override
    public Logger log() {
        return log;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
