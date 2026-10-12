package com.persagy.apccontrol.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.dto.ReleaseInterventionDTO;
import com.persagy.apccontrol.basic.model.dto.twin.BusinessExternalPushDTO;
import com.persagy.apccontrol.basic.model.dto.twin.BusinessTypeEnum;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.common.outside.dtpmeos.BusinessRunningLogPushDTO;
import com.persagy.apccontrol.common.server.SseEmitterEdgeServer;
import com.persagy.apccontrol.common.service.RunningLogEdgeNowService;
import com.persagy.apccontrol.common.service.RunningLogEdgeService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * dtpMeosServerEdge服务 mqtt消息处理
 */
@Component
public class DtpMeosServerEdgeService {


    @Autowired
    SseHandlerContext sseHandlerContext;
    @Resource
    private ObjectMapper objectMapper;
    @Autowired
    private RunningLogEdgeService runningLogEdgeService;
    @Autowired
    private SseEmitterEdgeServer sseEmitterEdgeServer;
    @Autowired
    private RunningLogEdgeNowService runningLogEdgeNowService;


    /**
     * 执行事件逻辑
     *
     * @param vo
     */
    public void dtpMeosServerEdge(BusinessExternalPushDTO vo) {
        // ControlLogUtil.info("收到dtpMeosServerEdge的mqtt数据-:{}", vo);
        BusinessTypeEnum type = vo.getBusinessType();
        if (type == null) {
            return;
        }
        try {
            switch (type) {
                case RUNNING_LOG:
                    if (vo.getContent() == null) {
                        return;
                    }
                    runningLogEdgeService.saveLog(objectMapper.readValue(vo.getContent().toString(), RcRunningLog.class));
                    break;
                case LIGHT_DEMAND_CHANGE:
                    sseHandlerContext.getStrategy(SseEmitterEventEnum.LIGHT_DEMAND_CHANGE)
                            .doOperation(ContextUtil.getProjectId(), SubscribeTypeEnum.LIGHT_MONITORING);
                    sseHandlerContext.getStrategy(SseEmitterEventEnum.LIGHT_LOOP)
                            .doOperation(ContextUtil.getProjectId(), SubscribeTypeEnum.LIGHT_MONITORING);
                    break;
                case RELEASE_INTERVENTION:
                    if (vo.getContent() == null) {
                        return;
                    }
                    sseEmitterEdgeServer.releaseIntervention(objectMapper.readValue(vo.getContent().toString(), ReleaseInterventionDTO.class));
                    break;
                case RUN_CONSOLE_DEMAND_CHANGE:
                case RUN_CONSOLE_DEMAND_CHANGE_SWITCH_SET:
                    if (vo.getContent() == null) {
                        return;
                    }

                    JSONArray msgList = JSONArray.parseArray(vo.getContent().toString());
                    msgList.forEach(i -> chillersControlConsoleMsg(i, type));

                    break;
                case BUSINESS_RUNNING_LOG:
                    if (vo.getContent() == null) {
                        return;
                    }
                    BusinessRunningLogPushDTO log = objectMapper.readValue(vo.getContent().toString(),
                            BusinessRunningLogPushDTO.class);
                    runningLogEdgeNowService.saveByBdtpMsg(log);
                    
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            ControlLogUtil.error("收到dtpMeosServerEdge的mqtt数据-执行错误-:{}", vo, e);
        }
    }

    /**
     * 冷源控制台消息推送
     *
     * @param msg
     */
    @SneakyThrows
    public void chillersControlConsoleMsg(Object msg, BusinessTypeEnum type) {
        if (msg == null) {
            return;
        }
        //obj 转成 MAP
        JSONObject json = JSONObject.parseObject(msg.toString());

        // 推送扩展配置消息 刷新页面
        FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = new FeedbackInfoMsgMqttVO();
        feedbackInfoMsgMqttVO.setClassCode(json.getString("classCode"));
        feedbackInfoMsgMqttVO.setSystemId(json.getString("systemId"));
        feedbackInfoMsgMqttVO.setObjectId(json.getString("objectId"));
        feedbackInfoMsgMqttVO.setModelType(type.getValue());
        feedbackInfoMsgMqttVO.setProjectId(ContextUtil.getProjectId());
        feedbackInfoMsgMqttVO.setExtendSetting(msg);
        //sse推送
        sseHandlerContext.getStrategy(SseEmitterEventEnum.INFO_EXTEND_SET)
                         .doOperation(ContextUtil.getProjectId(), feedbackInfoMsgMqttVO);
    }

}
