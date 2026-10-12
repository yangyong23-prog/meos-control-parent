package com.persagy.apccontrol.util.mqtt;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.extra.spring.SpringUtil;
import com.lmax.disruptor.dsl.Disruptor;
import com.persagy.framework.annotation.mqtt.ConditionalOnMqttEnabled;
import com.persagy.framework.mqtt.disruptor.MessageDisruptor;
import com.persagy.framework.mqtt.disruptor.MessageEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@ConditionalOnMqttEnabled
public class MqttCounter {
    
    
    @Resource
    @Qualifier("asyncSSEExecutor")
    private ThreadPoolTaskExecutor asyncSSEExecutor;
    @Resource
    @Qualifier("highPriorityMqttExecutor")
    private ThreadPoolExecutor highPriorityMqttExecutor;
    @Resource
    @Qualifier("normalPriorityMqttExecutor")
    private ThreadPoolExecutor normalPriorityMqttExecutor;
    
    /**
     * 统计 mqtt/sse 线程池队列大小
     *
     * @return 统计数量
     */
    public Map<String, Object> getExecutorQueueSize() {
        Map<String, Object> map = new HashMap<>();
        map.put(LocaleMessageUtil.getMessage("key_ac3267353cff4475aaf08ee98026a8a3"), asyncSSEExecutor.getThreadPoolExecutor().getQueue().size());
        // 暂时没用先注释 map.put("mqtt高优先级线程池队列", highPriorityMqttExecutor.getQueue().size());
        map.put(LocaleMessageUtil.getMessage("key_53c5863fe78c48b7a994edc12c9b5746"), normalPriorityMqttExecutor.getQueue().size());
        
        MessageDisruptor mqttMessageDisruptor = SpringUtil.getBean(MessageDisruptor.class);
        if (mqttMessageDisruptor != null) {
            Disruptor<MessageEvent> disruptor = mqttMessageDisruptor.getDisruptor();
            // 计算Disruptor队列当前元素数量
            long cursor = disruptor.getRingBuffer().getCursor();
            long minSequence = disruptor.getRingBuffer().getMinimumGatingSequence();
            long currentCapacity = cursor - minSequence;
            map.put(LocaleMessageUtil.getMessage("key_1ae504c25a9342de822ae198e228dab9"), currentCapacity);
        }
        
        return map;
    }
    
}
