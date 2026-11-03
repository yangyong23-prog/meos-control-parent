package com.persagy.apccontrol.basic.sse;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.persagy.apccontrol.basic.opt.OptSseKeepalive;
import com.persagy.apccontrol.basic.sse.annotation.SsePollingParam;
import com.persagy.apccontrol.basic.utils.DailyStringCounter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE，例：https://github.com/aliakh/demo-spring-sse
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/25 17:40
 */
@Slf4j
@Component
@Scope(value = "singleton")
@RefreshScope
@Getter
public class SseEmitters {


    @Autowired
    @Qualifier("asyncSSEExecutor")
    private ThreadPoolTaskExecutor asyncSSEExecutor;

    /**
     * key:   eventId
     * value: 客户端集合
     */
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> multiEmitters = new ConcurrentHashMap<>();

    /**
     * eventId和客户端参数映射
     *
     * key:   eventId
     * value: 客户端参数
     */
    private final Map<String, SseSubscribeParam> sseParamMap = new ConcurrentHashMap<>();

    /**
     * 新增一个客户端
     *
     * @param eventKey
     * @return
     */
    public SseEmitter add(String eventKey, SseSubscribeParam param) {
        sseParamMap.put(eventKey, param);
        // opt B9：switch.opt.sseKeepalive 打开时带超时，关闭时为 0L（永不超时）
        return add(eventKey, new CustomSseEmitter(OptSseKeepalive.emitterTimeout()));
    }

    /**
     * 绑定一个客户端
     *
     * @param eventKey
     * @return
     */
    public SseEmitter bind(String eventKey, SseSubscribeParam param, SseEmitter emitter) {
        sseParamMap.put(eventKey, param);
        return add(eventKey, emitter);
    }

    /**
     * 移除客户端
     *
     * @param eventKey
     * @param emitter
     */
    public void removeSseEmitter(String eventKey, SseEmitter emitter) {
        if (emitter == null) {
            return;
        }
        // 16.17：onCompletion / onError / onTimeout 可能先后触发，key 已被清理时不再 NPE
        CopyOnWriteArrayList<SseEmitter> emitters = this.multiEmitters.get(eventKey);
        if (emitters != null) {
            emitters.remove(emitter);
        }
        if (CollUtil.isEmpty(this.multiEmitters.get(eventKey))) {
            sseParamMap.remove(eventKey);
            multiEmitters.remove(eventKey);
        }
    }

    /**
     * 断开连接处理
     *
     * @param eventKey
     * @param emitter
     * @return
     */
    private SseEmitter add(String eventKey, SseEmitter emitter) {
        this.multiEmitters.computeIfAbsent(eventKey, v -> new CopyOnWriteArrayList<>()).add(emitter);
        log.info("eventKey: {}, Emitter count: {}, key count: {}", eventKey, multiEmitters.get(eventKey).size(), multiEmitters.keySet().size());
        emitter.onCompletion(() -> {
            log.info("eventKey: {}, Emitter completed: {}", eventKey, emitter);
            removeSseEmitter(eventKey, emitter);
        });

        emitter.onError((e) -> {
            if (!(e instanceof IOException)) {
                log.error("eventKey: {}, connect exception", eventKey, e);
            }
            removeSseEmitter(eventKey, emitter);
        });

        emitter.onTimeout(() -> {
            log.info("eventKey: {}, Emitter timed out: {}", eventKey, emitter);
            removeSseEmitter(eventKey, emitter);
        });

        return emitter;
    }


    /**
     * 消息发送
     *
     * @param eventKey
     * @param message
     */
    public void send(String eventKey, SseEmitterObject message) {
        timeConsumingStatistics(eventKey, message);
        
        SseEmitter.SseEventBuilder builder =
                SseEmitter.event().id(IdWorker.getIdStr()).name(message.getEventType().value()).data(message);
        send(eventKey, emitter -> emitter.send(builder));
    }
    
    /**
     * 耗时统计
     *
     * @param eventKey
     * @param message
     */
    private void timeConsumingStatistics(String eventKey, SseEmitterObject message) {
        try {
            DailyStringCounter.incrementCount(message.getEventType() + "事件sse发送请求总数");
            //记录请求处理时间
            if (!SseEmitterEventEnum.IOT_DATA_CHANGE.equals(message.getEventType())) {
                Long time2 = DailyStringCounter.sseExecuteElapsedTimeEnd(eventKey + message.getEventType());
                if (time2 != null) {
                    log.error("sse事件推送耗时: eventKey: {},EventType: {},耗时: {} 毫秒", eventKey, message.getEventType(),
                            time2);
                }
            }
        } catch (Exception e) {
            log.error("sse推送超时记录异常", e);
        }
    }
    
    /**
     * 消息发送
     *
     * @param consumer
     */
    private void send(String eventKey, SseEmitterConsumer<SseEmitter> consumer) {
        CopyOnWriteArrayList<SseEmitter> failedEmitters = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<SseEmitter> emitters = multiEmitters.getOrDefault(eventKey, new CopyOnWriteArrayList<>());
        emitters.forEach(emitter -> {
            try {
                consumer.accept(emitter);
                DailyStringCounter.incrementCount("sse实际发送总数" + eventKey);
                DailyStringCounter.incrementCount("sse实际发送总数");
            } catch (Exception e) {
                emitter.completeWithError(e);
                failedEmitters.add(emitter);
                log.info("Project: {}, Emitter failed: {}", eventKey, emitter);
                DailyStringCounter.incrementCount("sse发送异常重试总数" + eventKey);
            }
        });
        emitters.removeAll(failedEmitters);
    }

    public List<SseEmitter> getSseEmittersByEventKey(String eventKey) {
        return multiEmitters.get(eventKey);
    }



    public Map<String, SseSubscribeParam> getEventKeys(String projectId, SubscribeTypeEnum subscribeType) {
        if (sseParamMap.isEmpty()){
            return Collections.emptyMap();
        }
        return getStringSseSubscribeParamMap(projectId, subscribeType);
    }
    
    /**
     * 获取订阅参数
     * 如果订阅不存在了就删除订阅
     *
     * @param projectId
     * @param subscribeType
     * @return
     */
    private Map<String, SseSubscribeParam> getStringSseSubscribeParamMap(String projectId,
                                                                         SubscribeTypeEnum subscribeType) {
        Map<String, SseSubscribeParam> result = new HashMap<>();
        sseParamMap.forEach((key, value) -> {
            //先检查 客户端是否已被移除 移除就把这个事件移除
            if (CollectionUtils.isEmpty(multiEmitters.get(key))) {
                sseParamMap.remove(key);
                multiEmitters.remove(key);
                return;
            }
            boolean projectIdFlag = false;
            boolean type = false;
            if (projectId == null || projectId.equals(value.getProjectId())) {
                projectIdFlag = true;
            }
            
            if (subscribeType == null || subscribeType.equals(value.getSubscribeType())) {
                type = true;
            }
            
            if (projectIdFlag && type) {
                result.put(key, value);
            }
        });
        return result;
    }
    
    public Map<String, SseSubscribeParam> getEventKeys(SubscribeTypeEnum subscribeType) {
        if (sseParamMap.isEmpty()){
            return Collections.emptyMap();
        }
        return getStringSseSubscribeParamMap(null, subscribeType);
    }

    public Map<String, SseSubscribeParam> getEventKeysByProjectId(String projectId) {
        if (sseParamMap.isEmpty()){
            return Collections.emptyMap();
        }
        return getStringSseSubscribeParamMap(projectId, null);
    }

    public void addPollingParam(SsePollingParam param) {
        if (sseParamMap.containsKey(param.getEventId())){
            sseParamMap.get(param.getEventId()).addPollingParam(param.getEventType(), param.getParam());
        }
    }

    @FunctionalInterface
    private interface SseEmitterConsumer<T> {
        void accept(T t) throws IOException;
    }

}
