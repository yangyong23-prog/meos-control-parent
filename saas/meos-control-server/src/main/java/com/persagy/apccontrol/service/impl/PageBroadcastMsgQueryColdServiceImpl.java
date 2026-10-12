package com.persagy.apccontrol.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.apccontrol.basic.model.param.log.PageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.log.TodayLogSummaryVo;
import com.persagy.apccontrol.basic.resource.RcRunningLogService;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogPageQueryParam;
import com.persagy.apccontrol.basic.service.common.PageBroadcastMsgQueryService;
import com.persagy.apccontrol.basic.service.runninglog.NowRunningLogService;
import com.persagy.apccontrol.basic.service.runninglog.RunningLogService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 页面播报服务
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Service
public class PageBroadcastMsgQueryColdServiceImpl implements PageBroadcastMsgQueryService {

    @Resource
    private RcRunningLogService rcRunningLogService;
    
    @Resource
    private NowRunningLogService nowRunningLogService;

    @Override
    public List<FeedbackInfoMsgMqttVO.PageBroadcastContent> listPageBroadcastMsg(PageBroadcastQueryParam param) {

        //只播报“日志分类 = 控制需求调整、系统控制参数修改、设备手动/强制操作、需求干预”的日志
        List<RunningLogCategoryEnum> secondLevels = new ArrayList<>();
        secondLevels.add(RunningLogCategoryEnum.STRATEGY_ADJUST);
        secondLevels.add(RunningLogCategoryEnum.HOT_EQUIPMENT_MENUAL_OPERATION);
        secondLevels.add(RunningLogCategoryEnum.LIGHT_MONITORING_INTERVENTION);


        //查询最新的五条页面播报数据
        List<RcRunningLog> rcRunningLogs = rcRunningLogService.lambdaQuery()
                                                              .eq(RcRunningLog::getProjectId,
                                                                      ContextUtil.getProjectId())
                .ge(RcRunningLog::getCreateTime, LocalDate.now().atStartOfDay())
                .eq(RcRunningLog::getSystemId, param.getSystemId())
                .in(RcRunningLog::getSecondLevelCategory, secondLevels)
                .orderByDesc(RcRunningLog::getCreateTime)
                .last(CommonConst.LIMIT_FIVE)
                .list();
        if (CollectionUtil.isEmpty(rcRunningLogs)) {
            return new ArrayList<>();
        }

        List<FeedbackInfoMsgMqttVO.PageBroadcastContent> contents = new ArrayList<>();
        for (RcRunningLog runningLog : rcRunningLogs) {
            FeedbackInfoMsgMqttVO.PageBroadcastContent broadcastContent = new FeedbackInfoMsgMqttVO.PageBroadcastContent();
            broadcastContent.setContent(runningLog.getPageBroadcastContent(runningLog.getCreateUser()));
            broadcastContent.setOperateType(RunningLogTypeEnum.AUTO.value().equals(runningLog.getType()) ? OperateTypeEnum.PAGE_BROADCAST_AUTO.value()
                    : RunningLogTypeEnum.MANUAL.value().equals(runningLog.getType()) ? OperateTypeEnum.PAGE_BROADCAST_MANUAL.value()
                    : null);
            contents.add(broadcastContent);
        }
        return contents;
    }

    @Override
    public TodayLogSummaryVo todayLogSummary() {
        TodayLogSummaryVo todayLogSummaryVo = new TodayLogSummaryVo(0L, 0L, null);
        Map<RunningLogTypeEnum, Long> countWithType = rcRunningLogService.countWithType(ContextUtil.getProjectId());
        if (!countWithType.isEmpty()) {
            // 日志统计汇总  总数只记录手动和自动的
            AtomicReference<Long> total = new AtomicReference<>(0L);
            countWithType.forEach((k, v) -> {
                if (k.equals(RunningLogTypeEnum.AUTO) || k.equals(RunningLogTypeEnum.MANUAL)) {
                    total.updateAndGet(v1 -> v1 + v);
                }
            });
            todayLogSummaryVo.setAutoCount(countWithType.get(RunningLogTypeEnum.AUTO));
            todayLogSummaryVo.setTotal(total.get());

            // 查询项目今日最新三条自动日志
            RunningLogPageQueryParam queryParam = new RunningLogPageQueryParam();
            queryParam.setProjectId(ContextUtil.getProjectId());
            queryParam.setStartTime(DateUtils.now().withHour(0).withMinute(0).withSecond(0));
            queryParam.setType(RunningLogTypeEnum.AUTO);
            queryParam.setPageNumber(1);
            queryParam.setPageSize(3);
            IPage<RcRunningLog> collectionR = rcRunningLogService.queryRunningLogList(queryParam);
            todayLogSummaryVo.setLatestContent(RunningLogService.getRunningLogHomePageVO(collectionR.getRecords()));
        }
        
        //新版统计
        TodayLogSummaryVo logSummaryVo = nowRunningLogService.todayLogSummary();
        //合并结果
        todayLogSummaryVo.combinedResult(logSummaryVo);


        return todayLogSummaryVo;
    }

}
