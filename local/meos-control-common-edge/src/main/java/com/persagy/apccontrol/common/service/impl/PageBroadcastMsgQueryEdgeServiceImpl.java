package com.persagy.apccontrol.common.service.impl;

import com.persagy.apccontrol.basic.model.param.log.PageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.log.TodayLogSummaryVo;
import com.persagy.apccontrol.basic.service.common.PageBroadcastMsgQueryService;
import com.persagy.apccontrol.basic.service.runninglog.NowRunningLogService;
import com.persagy.apccontrol.common.service.RunningLogEdgeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 页面播报本地服务
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Service
public class PageBroadcastMsgQueryEdgeServiceImpl implements PageBroadcastMsgQueryService {

    @Resource
    private RunningLogEdgeService runningLogEdgeService;
    @Resource
    private NowRunningLogService nowRunningLogService;
    
    @Override
    public List<FeedbackInfoMsgMqttVO.PageBroadcastContent> listPageBroadcastMsg(PageBroadcastQueryParam param) {
        return runningLogEdgeService.listPageBroadcastMsg(param);
    }

    @Override
    public TodayLogSummaryVo todayLogSummary() {
        TodayLogSummaryVo todayLogSummaryVo = runningLogEdgeService.todayLogSummary();
        //新版统计
        TodayLogSummaryVo logSummaryVo = nowRunningLogService.todayLogSummary();
        //合并结果
        todayLogSummaryVo.combinedResult(logSummaryVo);
        return todayLogSummaryVo;
    }
}
