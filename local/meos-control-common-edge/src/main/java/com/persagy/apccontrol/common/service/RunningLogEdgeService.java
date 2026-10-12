package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.param.log.PageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.log.TodayLogSummaryVo;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogInsertParam;

import java.util.List;

public interface RunningLogEdgeService {


    /**
     * 播报查询
     *
     * @param param
     * @return
     */
    public List<FeedbackInfoMsgMqttVO.PageBroadcastContent> listPageBroadcastMsg(PageBroadcastQueryParam param);

    /**
     * 值班首页查询
     *
     * @return
     */
    public TodayLogSummaryVo todayLogSummary();

    /**
     * 查询报警中的日志
     * @param objectId
     * @param firstLevelCategory
     * @param secondLevelCategory
     * @param thirdLevelCategory
     * @return
     */
    RcRunningLog getNoEndRunningLogByAlarm(String objectId, RunningLogCategoryEnum firstLevelCategory, RunningLogCategoryEnum secondLevelCategory, RunningLogCategoryEnum thirdLevelCategory);


    /**
     * 本地端保存日志调用此方法，实时同步日志到云端
     * @param groupCode
     * @param param
     */
    void saveLog(String groupCode, RunningLogInsertParam param);

    void saveLog(RcRunningLog param);
}

