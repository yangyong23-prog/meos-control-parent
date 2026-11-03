package com.persagy.apccontrol.opt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinR;
import com.persagy.apccontrol.common.opt.OptRealtimeMissListener;
import com.persagy.apccontrol.common.outside.feign.OptTwinSubscribeClient;
import com.persagy.apccontrol.common.outside.model.dto.MqttSubscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * opt 分支 B3（switch.opt.pointSubscribe，默认关）：在 Twin 专用主题上按点订阅。
 * <ul>
 *     <li>应用就绪后（本地缓存已加载）首次全量同步，之后每 {@code opt.subscribe.sync-interval-minutes} 分钟一次：
 *     查询 topic={@code opt.subscribe.topic} 的现有规则，与 {@link OptSubscribePointRegistry#expected()} 求差量，先删后加；</li>
 *     <li>读路径发现未订阅的点连续 {@code opt.subscribe.auto-threshold} 次需要回源 Twin 且有有效值时，记入
 *     Redis 并在下一个 tick（默认 60 秒）增量补订阅。</li>
 * </ul>
 * 原按类订阅（topic=external，{@code MqttSubscribeInitServiceImpl}）完全不动。开关关闭时 tick 直接返回、不调 Twin。
 */
@Slf4j
@Component
public class OptPointSubscribeService implements OptRealtimeMissListener {

    @Resource
    private OptTwinSubscribeClient client;
    @Resource
    private OptSubscribePointRegistry registry;
    @Resource
    private ProjectCfg projectCfg;

    @Value("${paas.mqtt.enabled:true}")
    private Boolean mqttEnabled;
    @Value("${opt.subscribe.topic:meos}")
    private String topic;
    @Value("${opt.subscribe.sync-interval-minutes:30}")
    private long syncIntervalMinutes;
    @Value("${opt.subscribe.auto-threshold:3}")
    private int autoThreshold;

    private final Cache<String, AtomicInteger> missCounts = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS).maximumSize(100_000).build();
    private final Set<String> pending = ConcurrentHashMap.newKeySet();
    private volatile Map<String, Set<String>> current = Collections.emptyMap();
    private volatile long lastFullSync;
    private volatile boolean ready;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        ready = true;
    }

    @Override
    public void onTwinFill(String classCode, String infoCode) {
        if (!OptSwitch.on(OptSwitch.POINT_SUBSCRIBE) || StringUtils.isAnyBlank(classCode, infoCode)
                || covered(current, classCode, infoCode)) {
            return;
        }
        String point = classCode + ":" + infoCode;
        AtomicInteger n = missCounts.get(point, k -> new AtomicInteger());
        if (n != null && n.incrementAndGet() >= autoThreshold) {
            missCounts.invalidate(point);
            if (pending.add(point)) {
                OptMetrics.incAlways("sub.autoPending");
            }
        }
    }

    @Scheduled(initialDelayString = "${opt.subscribe.tick-initial-delay-ms:30000}", fixedDelayString = "${opt.subscribe.tick-ms:60000}")
    public void tick() {
        if (!ready || !OptSwitch.on(OptSwitch.POINT_SUBSCRIBE) || !Boolean.TRUE.equals(mqttEnabled)) {
            return;
        }
        try {
            if (System.currentTimeMillis() - lastFullSync >= TimeUnit.MINUTES.toMillis(syncIntervalMinutes)) {
                fullSync();
            } else {
                flushPending();
            }
        } catch (Exception e) {
            OptMetrics.incAlways("sub.error");
            log.warn("[opt] 按点订阅同步失败，下个周期重试: {}", e.toString());
        }
    }

    /** 全量差量同步 */
    void fullSync() {
        List<String> drained = drainPending();
        registry.persistAuto(drained);
        Map<String, Set<String>> expected = registry.expected();
        Set<String> classes = new TreeSet<>(expected.keySet());
        classes.addAll(current.keySet());
        List<OptSubscribePlan.Rule> existing = query(new ArrayList<>(classes));
        OptSubscribePlan plan = OptSubscribePlan.diff(expected, existing);
        if (!plan.getToDelete().isEmpty()) {
            check(client.del(projectCfg.getProjectId(), projectCfg.getGroupCode(), topic, toRequest(plan.getToDelete())), "del");
        }
        if (!plan.getToAdd().isEmpty()) {
            check(client.add(projectCfg.getProjectId(), projectCfg.getGroupCode(), topic, toRequest(plan.getToAdd())), "add");
        }
        current = expected;
        lastFullSync = System.currentTimeMillis();
        OptMetrics.incAlways("sub.sync");
        OptMetrics.addAlways("sub.addPoints", plan.addedPointCount());
        log.info("[opt] 按点订阅同步完成 topic={} 类={} 删除={} 新增={}", topic, expected.size(),
                plan.getToDelete().keySet(), plan.getToAdd().keySet());
    }

    /** 增量补订阅 */
    void flushPending() {
        List<String> drained = drainPending();
        if (drained.isEmpty()) {
            return;
        }
        Map<String, Set<String>> add = new TreeMap<>();
        for (String point : drained) {
            String[] parts = point.split(":", 2);
            if (!covered(current, parts[0], parts[1])) {
                add.computeIfAbsent(parts[0], k -> new TreeSet<>()).add(parts[1]);
            }
        }
        registry.persistAuto(drained);
        if (add.isEmpty()) {
            return;
        }
        try {
            check(client.add(projectCfg.getProjectId(), projectCfg.getGroupCode(), topic, toRequest(add)), "add");
        } catch (RuntimeException e) {
            // 已持久化，下次全量同步会补上
            lastFullSync = 0;
            throw e;
        }
        Map<String, Set<String>> next = new HashMap<>(current);
        add.forEach((c, points) -> {
            Set<String> merged = new TreeSet<>(next.getOrDefault(c, Collections.emptySet()));
            merged.addAll(points);
            next.put(c, merged);
        });
        current = next;
        OptMetrics.addAlways("sub.autoAdded", drained.size());
        log.info("[opt] 自动补订阅 {}", add);
    }

    private List<OptSubscribePlan.Rule> query(List<String> classes) {
        List<OptSubscribePlan.Rule> rules = new ArrayList<>();
        if (classes.isEmpty()) {
            return rules;
        }
        DtpTwinR<Map<String, Object>> r = client.query(projectCfg.getProjectId(), projectCfg.getGroupCode(), topic, classes);
        check(r, "query");
        if (r.getContent() != null) {
            for (Map<String, Object> item : r.getContent()) {
                Object c = item.get("classCode");
                Object i = item.get("infoCode");
                String info = i == null ? null : String.valueOf(i);
                if (StringUtils.isBlank(info) || "@".equals(info)) {
                    info = null;
                }
                if (c != null) {
                    rules.add(new OptSubscribePlan.Rule(String.valueOf(c), info));
                }
            }
        }
        return rules;
    }

    static List<MqttSubscribe> toRequest(Map<String, Set<String>> rules) {
        List<MqttSubscribe> list = new ArrayList<>(rules.size());
        rules.forEach((classCode, points) -> {
            MqttSubscribe s = new MqttSubscribe();
            s.setClassCode(classCode);
            s.setInfoCode(points.isEmpty() ? null : new ArrayList<>(points));
            list.add(s);
        });
        return list;
    }

    static boolean covered(Map<String, Set<String>> rules, String classCode, String infoCode) {
        Set<String> points = rules.get(classCode);
        return points != null && (points.isEmpty() || points.contains(infoCode));
    }

    private List<String> drainPending() {
        List<String> drained = new ArrayList<>(pending);
        pending.removeAll(drained);
        return drained;
    }

    private static void check(DtpTwinR<?> r, String action) {
        if (r == null || !r.getIsSuccess()) {
            throw new IllegalStateException("Twin 订阅规则 " + action + " 失败: " + (r == null ? "null" : r.getRespMsg()));
        }
    }
}
