package com.persagy.apccontrol.service;

import com.persagy.apccontrol.basic.model.param.runningstandardboard.RunningStandardBoardQueryObjectParam;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.RunningStandardBoardQueryParam;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.SpecialBusinessTimeQuoteCountQueryParam;
import com.persagy.apccontrol.basic.model.vo.runningstandardboard.*;
import com.persagy.apccontrol.basic.structure.demand.enumeration.ParticipateInSchedulingEnum;

import java.time.LocalDate;
import java.util.List;

public interface RunningStandardService {
    BusinessTimeObjectVo previewProjectBusinessTime(RunningStandardBoardQueryParam param);

    List<AirconditioningRunningStandardObjectVo> previewProjectGlobalConfig(RunningStandardBoardQueryParam param);

    ParticipateInSchedulingEnum isParticipateInScheduling(LocalDate localDate);

    /**
     * 查询-某日价值选择方案
     *
     * @param runningStandardBoardQueryParam
     * @return
     */
    ValueSelectObjectVo previewProjectValueSelect(RunningStandardBoardQueryParam runningStandardBoardQueryParam);

    /**
     * 查询-某日特殊需求配置项
     *
     * @param runningStandardBoardQueryParam
     * @return
     */
    SpecialDemandConfigObjectVo previewSpecialDemandConfig(RunningStandardBoardQueryParam runningStandardBoardQueryParam);

    /**
     * 查询-引用对象列表
     *
     * @param param
     * @return
     */
    List<ReferenceSpaceDetailVo> previewReferenceSpaceDetail(RunningStandardBoardQueryObjectParam param);

    List<SpecialBusinessTimeQuoteCountVo> specialBusinessTimeQuoteCount(SpecialBusinessTimeQuoteCountQueryParam param);

}
