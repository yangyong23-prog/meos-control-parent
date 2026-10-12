package com.persagy.apccontrol.basic.service.common;

import com.persagy.apccontrol.basic.model.param.log.PageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.log.TodayLogSummaryVo;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 页面播报服务
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
public interface PageBroadcastMsgQueryService {

    /**
     * 页面播报
     *
     * @param param
     */
    List<FeedbackInfoMsgMqttVO.PageBroadcastContent> listPageBroadcastMsg(@NotNull PageBroadcastQueryParam param);


    /**
     * 查询当日自控日志统计
     *
     * @param
     * @return {@link TodayLogSummaryVo}
     * @author yuecaipu
     * @date 2024/11/5 14:00
     * @since v5.241101.0
     */
    TodayLogSummaryVo todayLogSummary();

}