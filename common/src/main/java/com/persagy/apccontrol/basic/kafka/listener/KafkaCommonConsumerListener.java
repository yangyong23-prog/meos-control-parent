package com.persagy.apccontrol.basic.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.outside.dto.riskevent.InstanceTreeMonitoringSituationChangeMsgDTO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;

/**
 * 异常事件kafka队列
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/10/14 18:32
 * @since v5.241002.0
 */
@Slf4j
@Service("kafkaCommonConsumerListener")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(prefix = "project.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaCommonConsumerListener implements LogComponent {

    @Resource
    SseHandlerContext sshHandlerContext;

    @KafkaListener(topics = "${kafka.topic.yiheng-cloud-business.topic:yiheng-cloud-business-topic}",
            groupId = "#{T(com.persagy.apccontrol.basic.kafka.listener.GroupIdGenerator).generateId()}-danger-instance-tree",
            properties = {AUTO_OFFSET_RESET_CONFIG + "=latest"},
            id = CommonConst.APPLICATION_NAME + "-danger-instance-tree")
    public void instanceTreeMonitoringSituationChange(ConsumerRecord<String, String> output) {
        String message = output.value();

        if (StringUtils.isBlank(message)) {
            log.error("【kafka异常事件消息】消息为空");
            return;
        }

        String projectId = getFiledFromHeaders(output.headers(), "projectId");
        if (StringUtils.isBlank(projectId)) {
            logProjectInstance("all").error("kafka风险事件消息项目id为空",
                    Collections.singletonList(Pair.of("message", message)));
            return;
        }
        String messageType = getFiledFromHeaders(output.headers(), "messageType");
        if (StringUtils.isBlank(messageType)) {
            logProjectInstance("all").error("kafka风险事件消息项目messageType为空",
                    Collections.singletonList(Pair.of("message", message)));
            return;
        }

        try {
            if (messageType.equals("instanceTreeMonitoringSituationChangeMsg")) {
                List<InstanceTreeMonitoringSituationChangeMsgDTO> instanceTreeMonitoringSituationChangeMsgs = JSON.parseArray(message, InstanceTreeMonitoringSituationChangeMsgDTO.class);
                if (CollectionUtils.isEmpty(instanceTreeMonitoringSituationChangeMsgs)) {
                    return;
                }
                InstanceTreeMonitoringSituationChangeMsgDTO pushMsg = new InstanceTreeMonitoringSituationChangeMsgDTO();
                pushMsg.setData(instanceTreeMonitoringSituationChangeMsgs);
                sshHandlerContext.getStrategy(SseEmitterEventEnum.DANGER_EVENT_MONITORING_SITUATION)
                        .doOperation(projectId, pushMsg);
            }
        } catch (
                Throwable e) {
            logProjectInstance(projectId).error("【kafka异常事件消息】处理失败", e,
                    Collections.singletonList(Pair.of("message", message)));
        }
    }


    /**
     * 获取kafka消息头
     *
     * @param headers 消息头
     * @return 字段值
     */
    private String getFiledFromHeaders(Headers headers, String key) {
        for (Header header : headers) {
            if (key.equals(header.key())) {
                return new String(header.value());
            }
        }
        return null;
    }

    @Override
    public String logTag() {
        return "kafka消息";
    }
}
