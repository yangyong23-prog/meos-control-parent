package com.persagy.apccontrol.mq;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.message.dtx.DtxRulePublishMessageEvent;
import com.persagy.apccontrol.basic.model.message.mq.DtxRulePublishMessage;
import com.persagy.apccontrol.basic.utils.context.ContextUtils;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * DTX规则发布消息监听处理器
 * <p>
 * 交换机：dtx.control.rule.inst.exchange
 * 路由Key：dtx.control.rule.inst.routeKey
 * 消息格式：{
 * "groupCode":"集团编码",
 * "projectId":"项目ID",
 * "operatorName":"操作人"
 * }
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DtxRulePublishRabbitHandler extends BaseListenerHandler<DtxRulePublishMessage, DtxRulePublishRabbitHandler> {

    /**
     * 监听器名称
     */
    private static final String LISTENER_NAME = "dtx-rule-publish";

    private final ApplicationEventPublisher applicationEventPublisher;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = CommonConst.APPLICATION_NAME + ".dtx.control.rule.inst.queue", durable = "true"),
            exchange = @Exchange(value = "dtx.control.rule.inst.exchange"),
            key = "dtx.control.rule.inst.routeKey"))
    public void onMessage(DtxRulePublishMessage data, Channel channel, Message message) {
        this.execute(data, channel, message);
    }

    /**
     * 处理规则发布消息
     *
     * @param message 消息数据
     * @throws Exception 消息处理异常
     * @since 7.260302.0
     */
    @Override
    public void handler(DtxRulePublishMessage message) throws Exception {
        logProjectInstance().info("收到DTX规则发布消息", Pair.of("message", message));
        this.publishEvent(message);
    }

    /**
     * 发布事件
     *
     * @param message 消息
     * @since 7.260302.0
     */
    private void publishEvent(@NonNull DtxRulePublishMessage message) {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        try {
            DtxRulePublishMessageEvent.Message eventMessage = objectMapper
                    .convertValue(message, DtxRulePublishMessageEvent.Message.class);
            ContextUtils.initGroupCodeProjectId(eventMessage.getGroupCode(), eventMessage.getProjectId());
            logProjectInstance().info("开始发布事件", Pair.of("eventMessage", eventMessage));
            applicationEventPublisher.publishEvent(new DtxRulePublishMessageEvent(eventMessage));
            logProjectInstance().info("结束发布事件", Pair.of("eventMessage", eventMessage));
        } catch (Exception e) {
            logProjectInstance().error("消息解析失败", e, Pair.of("message", message));
        }
    }

    /**
     * 监听器名称
     *
     * @return 监听器名称
     * @since 7.260302.0
     */
    @Override
    public String listenerName() {
        return LISTENER_NAME;
    }

    /**
     * 日志标签
     *
     * @return 日志标签
     * @since 7.260302.0
     */
    @Override
    public String logTag() {
        return "DTX规则发布消息";
    }
}
