package com.persagy.apccontrol.mq;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.properties.ProjectProperties;
import com.persagy.apccontrol.basic.config.properties.RabbitProperties;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.message.BaseMessage;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.utils.ValidateUtils;
import com.persagy.framework.common.context.ContextConstants;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.validate.DefaultGroup;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.amqp.core.Message;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 基础监听处理器
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
public abstract class BaseListenerHandler<T extends BaseMessage, C> implements BizComponent<C> {

    /**
     * 处理消息数据
     *
     * @param message 消息数据
     * @throws Exception 消息处理异常
     * @since 5.240402.0
     */
    public abstract void handler(T message) throws Exception;

    /**
     * 监听器名称
     *
     * @return 监听器名称
     * @since 5.240402.0
     */
    public abstract String listenerName();

    /**
     * 消息基础处理
     *
     * @param channel 通道对象
     * @param message 消息对象
     * @since 5.240402.0
     */
    protected void execute(T data, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Boolean redelivered = message.getMessageProperties().isRedelivered();
        List<Pair<String, Object>> logDetails = Lists.newArrayList(
                Pair.of("data", data),
                Pair.of("properties", message.getMessageProperties()));
        try {
            setContext(data);
            ProjectProperties projectProperties = SpringUtil.getBean(ProjectProperties.class);
            MDC.put(ContextConstants.LOG_TRACE_ID, listenerName() + "-" + deliveryTag);
            Level messageLogLevel = RabbitProperties.Listener.DEFAULT_MESSAGE_LOG_LEVEL;
            RabbitProperties.Listener listener = projectProperties.getRabbit().getListeners().get(listenerName());
            if (Objects.nonNull(listener)) {
                messageLogLevel = listener.getMessageLogLevel();
                if (!listener.isEnabled()) {
                    if (BooleanUtils.isTrue(redelivered)) {
                        LogExecutor.instance().info(log(), logTag(), "消息恢复拒绝", logDetails);
                        this.nack(channel, deliveryTag, false, logDetails);
                    } else {
                        LogExecutor.instance().info(log(), logTag(), "消息恢复", logDetails);
                        this.recover(channel);
                    }
                    return;
                }
            }
            LogExecutor.instance().log(log(), messageLogLevel, logTag(), "收到消息", logDetails);
            StopWatch stopWatch = StopWatch.createStarted();
            ValidateUtils.validate(data, DefaultGroup.class);
            this.handler(data);
            stopWatch.stop();
            logDetails.add(Pair.of("executeTime", stopWatch.getTime() + "ms"));
            LogExecutor.instance().log(log(), messageLogLevel, logTag(), "消息处理成功", logDetails);
            this.ack(channel, deliveryTag, logDetails);
        } catch (ApplicationException e) {
            LogExecutor.instance().warn(log(), logTag(), "消息处理异常", e, logDetails);
            this.nack(channel, deliveryTag, false, logDetails);
        } catch (Exception e) {
            if (BooleanUtils.isTrue(redelivered)) {
                LogExecutor.instance().error(log(), logTag(), "消息重新处理失败", e, logDetails);
                this.nack(channel, deliveryTag, false, logDetails);
            } else {
                LogExecutor.instance().error(log(), logTag(), "消息处理失败, 即将重新投递", e, logDetails);
                this.nack(channel, deliveryTag, true, logDetails);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * 重新投递消息
     *
     * @param channel 通道对象
     * @since 5.240402.0
     */
    protected void recover(Channel channel) {
        try {
            channel.basicRecover(true);
        } catch (IOException e) {
            LogExecutor.instance().error(log(), logTag(), "消息恢复失败", e);
        }
    }

    /**
     * 确认消息
     *
     * @param channel     通道对象
     * @param deliveryTag 消息标记
     * @param logDetails  日志详细信息
     * @since 5.240402.0
     */
    protected void ack(Channel channel, long deliveryTag, List<Pair<String, Object>> logDetails) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            LogExecutor.instance().error(log(), logTag(), "消息确认失败", e, logDetails);
        }
    }

    /**
     * 拒绝消息
     *
     * @param channel     通道对象
     * @param deliveryTag 消息标记
     * @param requeue     是否重新投递
     * @param logDetails  日志详细信息
     * @since 5.240402.0
     */
    protected void nack(Channel channel, long deliveryTag, boolean requeue, List<Pair<String, Object>> logDetails) {
        try {
            channel.basicNack(deliveryTag, false, requeue);
        } catch (IOException e) {
            LogExecutor.instance().error(log(), logTag(), "消息拒绝失败", e, logDetails);
        }
    }

    /**
     * 设置上下文
     *
     * @param message 消息对象
     * @since 5.240402.0
     */
    private void setContext(BaseMessage message) {
        ContextUtil.setTenant(message.getGroupCode());
        ContextUtil.setGroupCode(message.getGroupCode());
        ContextUtil.setProjectId(message.getProjectId());
    }
}
