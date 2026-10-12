package com.persagy.apccontrol.mq;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CacheDynamicKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.message.dtx.DtxPublishMessageEvent;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphEditorService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.apccontrol.basic.utils.context.ContextUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.rabbitmq.handler.AbstractRabbitHandlerTemplate;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author MAF
 * 交换机： dtx.dataPublish.exchange
 * 路由：dtx.dataPublish.routeKey
 * 消息格式：{
 * "groupCode":"集团编码",
 * "projectId":"项目ID",
 * "publishTime":"发布时间"
 * }
 */

@RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = CommonConst.APPLICATION_NAME + ".dtx.dataPublish.queue", durable = "true"),
        exchange = @Exchange(value = "dtx.dataPublish.exchange"),
        key = "dtx.dataPublish.routeKey"))
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DtxDataPublishRabbitHandler extends AbstractRabbitHandlerTemplate implements LogComponent {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SystemGraphEditorService systemGraphEditorService;
    private final CommonConfigService commonConfigService;
    private final SystemGraphService systemGraphService;
    private final DtpRelationalGraphMessageHandler dtpRelationalGraphMessageHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RedisCacheTemplate redisCacheTemplate;

    @SneakyThrows
    @Override
    protected void handler(String msg, Channel channel, Message message) {

        if (StringUtils.isBlank(msg)) {
            return;
        }
        // 先只处理2次，用while可能会出现死循环漏洞，当前情况转2次就够了
        Object object = objectMapper.readValue(msg, Object.class);
        if (object instanceof String) {
            object = objectMapper.readValue((String) object, Object.class);
        }
        if (!(object instanceof Map<?, ?>)) {
            log.info("msg={}格式错误，最终无法转换为map", msg);
            return;
        }
        Map<?, ?> messageObject = (Map<?, ?>) object;

        String groupCode = String.valueOf(messageObject.get("groupCode"));
        String projectId = String.valueOf(messageObject.get("projectId"));
        if (StringUtils.isBlank(groupCode) || StringUtils.isBlank(projectId)) {
            return;
        }

        ContextUtil.setTenant(groupCode);
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setProjectId(projectId);

        // 删除所有RWD缓存
        redisCacheTemplate.fuzzyMatchingDel(CommonConst.APPLICATION_NAME
                + CacheKeyPrefix.SEPARATOR
                + CacheDynamicKey.DYNAMIC_KEY_PREFIX
                + "rwd");
        redisCacheTemplate.fuzzyMatchingDel(RedisCacheTemplate.BASE_CACHE_KEY_PREFIX);

        // 发布事件
        publishEvent(messageObject);

    }

    /**
     * 发布事件
     *
     * @param message 消息
     * @since 7.250601.0
     */
    private void publishEvent(@NonNull Map<?, ?> message) {
        if (MapUtils.isEmpty(message)) {
            logProjectInstance().error("消息为空");
            return;
        }
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        try {
            DtxPublishMessageEvent.Message eventMessage = objectMapper
                    .convertValue(message, DtxPublishMessageEvent.Message.class);
            ContextUtils.initGroupCodeProjectId(eventMessage);
            logProjectInstance().info("开始发布事件", Pair.of("eventMessage", eventMessage));
            applicationEventPublisher.publishEvent(new DtxPublishMessageEvent(eventMessage));
            logProjectInstance().info("结束发布事件", Pair.of("eventMessage", eventMessage));
        } catch (Exception e) {
            logProjectInstance().error("消息解析失败", e, Pair.of("message", message));
        }
    }

    @Override
    public String logTag() {
        return "DTX数据发布消息";
    }
}
