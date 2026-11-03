package com.persagy.apccontrol.basic.sse;

import cn.hutool.core.collection.CollUtil;
import com.persagy.apccontrol.basic.opt.OptSseKeepalive;
import com.persagy.apccontrol.basic.sse.annotation.SsePollingParam;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * SSE 数据推送
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/26 13:39
 */
@Slf4j
@Component
public class SseEmitterServer {

    @Autowired
    private SseEmitters sseEmitters;

    public synchronized SseEmitter add(SseSubscribeParam param) {
        String eventKey = genEventKey(param);
        SseEmitter sseEmitter = this.sseEmitters.add(eventKey, param);
        try {
            SseEmitter.SseEventBuilder connect = SseEmitter.event().name("EVENT_ID").comment(eventKey);
            sseEmitter.send(connect);
        } catch (Exception e) {
            log.error("连接创建失败", e);
            this.sseEmitters.removeSseEmitter(eventKey, sseEmitter);
        }
        return sseEmitter;
    }

    public synchronized SseEmitter addMulti(List<SseSubscribeParam> params) {
        if(CollUtil.isEmpty(params))
            return null;

        SseEmitter sseEmitter = new CustomSseEmitter(OptSseKeepalive.emitterTimeout());
        params.forEach(param->{
            param.setGroupCode(ContextUtil.getGroupCode());
            String eventKey = genEventKey(param);
            this.sseEmitters.bind(eventKey,param,sseEmitter);
            try {
                SseEmitter.SseEventBuilder connect = SseEmitter.event().name("EVENT_ID").comment(eventKey);
                sseEmitter.send(connect);
            } catch (Exception e) {
                log.error("连接创建失败", e);
                this.sseEmitters.removeSseEmitter(eventKey, sseEmitter);
            }
        });

        return sseEmitter;
    }

    /**
     * 事件ID
     *
     * @param param
     * @return {@link String}
     * @author yuecaipu
     * @date 2024/4/29 14:21
     * @since v5.240402.00
     */
    public String genEventKey(SseSubscribeParam param) {
        return Objects.hash(param.toString()) + "";
    }
    
    public void publishEvent(SseEmitterObject content) {
        this.publishEvent(content.getEventId(), content);
    }
    
    public void publishEvent(String eventId, SseEmitterObject object) {
        if (isDataEmpty(object)) {
            return;
        }
        this.sseEmitters.send(eventId, object);
    }

    /**
     * 判断数据是否为空
     *
     * @param object SseEmitterObject
     * @return {@link boolean}
     * @author yuecaipu
     * @date 2024/6/20 17:03
     * @since v5.240602
     */
    private boolean isDataEmpty(SseEmitterObject object) {
        if (object == null || object.getData() == null) {
            return true;
        }

        // 对于集合类型的检查
        if (object.getData() instanceof Collection<?>) {
            return ((Collection<?>) object.getData()).isEmpty();
        }

        return false;
    }

    public Map<String,SseSubscribeParam> getEventKeys(String projectId, SubscribeTypeEnum subscribeType) {
        return sseEmitters.getEventKeys(projectId, subscribeType);
    }

    public Map<String,SseSubscribeParam> getEventKeysByProjectId(String projectId) {
        return sseEmitters.getEventKeysByProjectId(projectId);
    }


    public void addPollingParam(SsePollingParam param) {
        sseEmitters.addPollingParam(param);
    }
}
