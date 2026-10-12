package com.persagy.apccontrol.kafka.listener;

import com.alibaba.fastjson.JSON;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.outside.dto.dangerevent.DangerEventMsgDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventMsgDTO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
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
import java.util.Objects;

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
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(prefix = "project.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumerListener implements LogComponent {

    @Resource
    SseHandlerContext sshHandlerContext;

    @KafkaListener(topics = "${kafka.topic.ee-event-publish.topic:ee-event-publish}",
            groupId = "#{T(com.persagy.apccontrol.kafka.listener.GroupIdGenerator).generateId()}-exception-event",
            properties = {AUTO_OFFSET_RESET_CONFIG + "=latest"},
            id = CommonConst.APPLICATION_NAME + "-abnormal-event")
    public void abnormalEventMessageHandle(ConsumerRecord<String, String> output) {
        String message = output.value();

        if (StringUtils.isBlank(message)) {
            log.error("【kafka异常事件消息】消息为空");
            return;
        }

        String projectId = getFiledFromHeaders(output.headers(), "project-id");
        if (StringUtils.isBlank(projectId)) {
            return;
        }

        try {
            List<ExceptionEventMsgDTO> exceptionEventMsgs = JSON.parseArray(message,
                    ExceptionEventMsgDTO.class, DateUtils.getParserConfigWithZone());
            if (CollectionUtils.isEmpty(exceptionEventMsgs)) {
                return;
            }

            sshHandlerContext.getStrategy(SseEmitterEventEnum.ABNORMAL_EVENT)
                    .doOperation(projectId, exceptionEventMsgs);

        } catch (Throwable e) {
            logProjectInstance(projectId).error("【kafka异常事件消息】处理失败", e,
                    Collections.singletonList(Pair.of("message", message)));
        }
    }

    @KafkaListener(topics = "${kafka.topic.yiheng-risk-status-calculate.topic:risk-tree-status-calculate-topic}",
            groupId = "#{T(com.persagy.apccontrol.kafka.listener.GroupIdGenerator).generateId()}-danger-event",
            properties = {AUTO_OFFSET_RESET_CONFIG + "=latest"},
            id = CommonConst.APPLICATION_NAME + "-danger-event")
    public void dangerEventMessageHandle(ConsumerRecord<String, String> output) {
        String message = output.value();

        if (StringUtils.isBlank(message)) {
            log.error("【kafka风险事件消息】消息为空");
            return;
        }

        String projectId = getFiledFromHeaders(output.headers(), "project-id");
        String groupCode = getFiledFromHeaders(output.headers(), "group-code");
        ContextUtil.setProjectId(projectId);
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        if (StringUtils.isBlank(projectId)) {
            logProjectInstance("all").error("kafka风险事件消息项目id为空",
                    Collections.singletonList(Pair.of("message", message)));
            return;
        }

        try {

            DangerEventMsgDTO dangerEventMsgDTO = JSON.parseObject(message,
                    DangerEventMsgDTO.class, DateUtils.getParserConfigWithZone());
            if (Objects.isNull(dangerEventMsgDTO)) {
                return;
            }

            sshHandlerContext.getStrategy(SseEmitterEventEnum.DANGER_EVENT)
                    .doOperation(projectId, dangerEventMsgDTO);

        } catch (Throwable e) {
            logProjectInstance(projectId).error("【kafka风险事件消息】处理失败", e,
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
