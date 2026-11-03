package com.persagy.apccontrol.basic.opt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * opt 分支 B9（switch.opt.sseCoalesce，默认关）：SSE“全量重算”类事件的窗口合并。
 * <p>
 * 照明等事件每来一条 MQTT 就触发一次全订阅者重算。打开后同一 (事件, 项目, 订阅类型) 在窗口内
 * （{@code opt.sse.coalesce-window-ms}，默认 3000）只执行一次：第一次触发开窗，窗口结束执行，窗口内的其余触发丢弃。
 * 执行前先关窗，执行期间到达的触发会开下一个窗口，不会丢最后一次变化。最大延迟为一个窗口。
 */
@Slf4j
@Component
public class OptSseCoalescer implements DisposableBean {

    private final Set<String> pending = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService scheduler;

    @Value("${opt.sse.coalesce-window-ms:3000}")
    private long windowMs = 3000L;

    private volatile Set<String> events = Collections.emptySet();

    public OptSseCoalescer() {
        this(Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "opt-sse-coalesce");
            t.setDaemon(true);
            return t;
        }));
    }

    OptSseCoalescer(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Value("${opt.sse.coalesce-events:LIGHT_LOOP,LIGHT_SPACE_STATUS,LIGHT_EQUIPMENT_STATUS,LIGHT_DEMAND_CHANGE}")
    public void setEvents(String csv) {
        Set<String> s = new HashSet<>();
        Arrays.stream(csv.split(",")).map(String::trim).filter(x -> !x.isEmpty()).forEach(s::add);
        this.events = s;
    }

    void setWindowMs(long windowMs) {
        this.windowMs = windowMs;
    }

    public boolean applies(String eventType) {
        return OptSwitch.on(OptSwitch.SSE_COALESCE) && events.contains(eventType);
    }

    /**
     * @return true 表示已被合并或已排期，调用方不要再立即执行
     */
    public boolean offer(String key, Runnable task) {
        if (!pending.add(key)) {
            OptMetrics.inc("sse.coalesced");
            return true;
        }
        try {
            scheduler.schedule(() -> {
                pending.remove(key);
                try {
                    task.run();
                } catch (Exception e) {
                    log.warn("[opt] SSE 合并任务执行失败 {}: {}", key, e.toString());
                }
            }, windowMs, TimeUnit.MILLISECONDS);
            return true;
        } catch (RuntimeException e) {
            // 调度失败时退回立即执行
            pending.remove(key);
            return false;
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }
}
