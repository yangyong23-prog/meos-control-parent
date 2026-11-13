
package com.persagy.apccontrol.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.twin.BusinessExternalPushDTO;
import com.persagy.apccontrol.basic.model.enumtype.EnumIotDataType;
import com.persagy.apccontrol.basic.model.enumtype.websocket.WebSocketTypeEnum;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.opt.OptMqttPayloads;
import com.persagy.apccontrol.opt.OptMqttStatus;
import com.persagy.apccontrol.basic.outside.dto.dangerevent.DangerEventMsgDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventMsgDTO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.basic.utils.DailyStringCounter;
import com.persagy.apccontrol.common.opt.OptRealtimeMqttWriter;
import com.persagy.apccontrol.common.service.MqttService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.mqtt.service.MqttReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


/***
 * 接收mqtt消息
 *
 * @author lgy
 * @date 2022/8/16 15:49
 */

@Slf4j
@Service
public class MqttReceiveServiceImpl implements MqttReceiveService {

    @Resource
    MqttService mqttService;

    @Resource
    ProjectCfg projectCfg;

    @Resource
    SseHandlerContext sshHandlerContext;

    /** opt 分支 B2（switch.opt.realtimeCacheWrite）：反馈值第一步写共享缓存 */
    @Lazy
    @Resource
    OptRealtimeMqttWriter optRealtimeMqttWriter;

    //反馈值队列
    @Value("${mqtt.topic.meos-compute.name}")
    private String compute;

    // 异常事件
    @Value("${mqtt.topic.event-change.name}")
    private String eventChange;

    // 风险事件
    @Value("${mqtt.topic.danger-event.name}")
    private String dangerEvent;

    // BDTP 控制服务
    @Value("${mqtt.topic.dtp-meos.name}")
    private String dtpMeosServerEdge;

    /** opt 分支 B3：Twin 按点订阅专用主题（twin/compute_meos），默认未配置 */
    @Value("${mqtt.topic.meos-compute-point.name:}")
    private String computePoint;


    /**
     * 务必实现，单点逻辑
     *
     * @param message Message
     */

    @Override
    public void receiveValue(Message<String> message) {
        handlerMessage(message);
    }

    /**
     * 务必实现，批量逻辑
     *
     * @param messageList String Array List
     */
    @Override
    public void receiveValues(List<Message<String>> messageList) {
        for (Message<String> message : messageList) {
            handlerMessage(message);
        }
    }

    /**
     * 接受消息处理 消息异步消费
     *
     * @param message 消息
     */
    public void handlerMessage(Message<String> message) {
        if (!OptSwitch.on(OptSwitch.MQTT_ISOLATION)) {
            doHandlerMessage(message);
            return;
        }
        // opt B1：记录收包、处理完在 finally 清理线程上下文（16.18）
        OptMqttStatus.markReceived();
        OptMetrics.inc("mqtt.recv");
        try {
            doHandlerMessage(message);
        } finally {
            ContextUtil.remove();
        }
    }

    private void doHandlerMessage(Message<String> message) {

        if (StringUtils.isEmpty(message.getPayload())) {
            return;
        }
        ContextUtil.setProjectId(projectCfg.getProjectId());
        ContextUtil.setGroupCode(projectCfg.getGroupCode());
        ContextUtil.setTenant(projectCfg.getGroupCode());

        //判断是反馈值 还是控制值数据 或者是异常事件
        String receivedTopic = message.getHeaders().get("mqtt_receivedTopic", String.class);

        DailyStringCounter.incrementCount("mqtt接收总数");

        // 异常事件
        if (StringUtils.equals(eventChange, receivedTopic)) {
            eventChange(message);
            return;
        }

        // 风险事件
        if (StringUtils.equals(dangerEvent, receivedTopic)) {
            dangerEvent(message);
            return;
        }

        //控制服务上报的日志信息
        if (StringUtils.equals(dtpMeosServerEdge, receivedTopic)) {
            dtpMeosServerEdge(message);
            return;
        }

        // opt B3：按点订阅专用主题
        if (StringUtils.isNotEmpty(computePoint) && StringUtils.equals(computePoint, receivedTopic)) {
            computePointMqtt(message);
            return;
        }

        twinMqtt(message, receivedTopic);
    }

    /**
     * dtpMeosServerEdge 上报的数据处理
     *
     * @param message
     */
    private void dtpMeosServerEdge(Message<String> message) {
        //数据转化为JSON格式
        BusinessExternalPushDTO businessExternalPush = JSONObject.parseObject(message.getPayload(), BusinessExternalPushDTO.class);
        if (businessExternalPush == null) {
            return;
        }
        DailyStringCounter.incrementCount("mqtt_dtp_meos接收总数");
        mqttService.dtpMeosServerEdge(businessExternalPush);
    }

    /**
     * 异常事件上报的数据处理
     *
     * @param message
     */
    private void eventChange(Message<String> message) {
        //数据转化为JSON格式
        List<ExceptionEventMsgDTO> exceptionEventMsgs = JSONObject.parseArray(message.getPayload(), ExceptionEventMsgDTO.class);
        if (exceptionEventMsgs == null) {
            return;
        }
        DailyStringCounter.incrementCount("mqtt_异常事件接收总数");
        sshHandlerContext.getStrategy(SseEmitterEventEnum.ABNORMAL_EVENT)
                .doOperation(projectCfg.getProjectId(), exceptionEventMsgs);
    }

    /**
     * 异常事件上报的数据处理
     *
     * @param message
     */
    private void dangerEvent(Message<String> message) {
        //数据转化为通用对象统一处理
        String payload = message.getPayload().replace("@type", "type");
        DangerEventMsgDTO dangerEventMsgDTO = JSON.parseObject(payload, DangerEventMsgDTO.class);
        if (Objects.isNull(dangerEventMsgDTO)) {
            return;
        }
        sshHandlerContext.getStrategy(SseEmitterEventEnum.DANGER_EVENT)
                .doOperation(projectCfg.getProjectId(), dangerEventMsgDTO);
    }

    /**
     * twin 上报的 MQTT 数据处理
     *
     * @param message
     * @param receivedTopic
     */
    private void twinMqtt(Message<String> message, String receivedTopic) {
        if (OptSwitch.on(OptSwitch.MQTT_ISOLATION)) {
            // opt B1：payload 只解析一次；"~" 日志降为 debug 并限频
            FeedbackInfoMsgMqttVO parsed = OptMqttPayloads.parseTwin(message.getPayload());
            if (parsed == null) {
                return;
            }
            parsed.setProjectId(projectCfg.getProjectId());
            parsed.setGroupCode(projectCfg.getGroupCode());
            writeRealtimeCache(parsed, receivedTopic);
            if (null == parsed.getDataValue() || "~".equals(parsed.getDataValue())) {
                OptMqttPayloads.logInvalidValue(message.getPayload());
                return;
            }
            twinMqttExpense(parsed, message, receivedTopic);
            return;
        }
        //数据转化为JSON格式
        JSONObject obj = JSONObject.parseObject(message.getPayload());
        if (obj == null) {
            return;
        }

        //数据转化为JSON格式
        JSONObject jsonValue = JSONObject.parseObject(obj.toString());
        FeedbackInfoMsgMqttVO vo = null;
        try {
            vo = CommonConst.JSON_MAPPER.readValue(jsonValue.toJSONString(), FeedbackInfoMsgMqttVO.class);
            vo.setProjectId(projectCfg.getProjectId());
            vo.setGroupCode(projectCfg.getGroupCode());
        } catch (JsonProcessingException e) {
            ControlLogUtil.error("MQTT推送数据，数据格式错误:{}", jsonValue.toJSONString(), e);
            return;
        }
        writeRealtimeCache(vo, receivedTopic);
        if (null == vo.getDataValue() || "~".equals(vo.getDataValue())) {
            //无效消息值
            ControlLogUtil.warn("MQTT推送数据，无效消息值-:{}", jsonValue.toJSONString());
            return;
        }
        twinMqttExpense(vo, message, receivedTopic);
    }

    /**
     * opt B3：专用主题报文总是写共享缓存；只有 switch.opt.meosTopicFullPipeline 打开时才按反馈值走完整处理
     * （切换期两个主题并存，默认不重复处理）。
     */
    private void computePointMqtt(Message<String> message) {
        FeedbackInfoMsgMqttVO parsed = OptMqttPayloads.parseTwin(message.getPayload());
        if (parsed == null) {
            return;
        }
        parsed.setProjectId(projectCfg.getProjectId());
        parsed.setGroupCode(projectCfg.getGroupCode());
        optRealtimeMqttWriter.onTwinFeedback(parsed);
        OptMetrics.inc("mqtt.recv.point");
        if (!OptSwitch.on(OptSwitch.MEOS_TOPIC_FULL_PIPELINE)) {
            return;
        }
        if (null == parsed.getDataValue() || "~".equals(parsed.getDataValue())) {
            OptMqttPayloads.logInvalidValue(message.getPayload());
            return;
        }
        twinMqttExpense(parsed, message, compute);
    }

    /**
     * opt B2：反馈值主题的报文在任何换算之前写共享缓存（含 "~" 离线标记）。
     */
    private void writeRealtimeCache(FeedbackInfoMsgMqttVO raw, String receivedTopic) {
        if (StringUtils.equals(compute, receivedTopic)) {
            optRealtimeMqttWriter.onTwinFeedback(raw);
        }
    }

    private void twinMqttExpense(FeedbackInfoMsgMqttVO vo, Message<String> message, String receivedTopic) {
        if (StringUtils.equals(compute, receivedTopic)) {
            //反馈值
            vo.setType(EnumIotDataType.FEEDBACK.getCode());
            DailyStringCounter.incrementCount("mqtt反馈值");
        } else {
            vo.setType(EnumIotDataType.CTRL.getCode());
            ControlLogUtil.info("收到mqtt数据-控制值-:{}", message);
            DailyStringCounter.incrementCount("mqtt控制值总数");
        }

        vo.setWebType(WebSocketTypeEnum.PARAM_SET);
        //调用消费逻辑
        mqttService.expense(vo);
    }

}
