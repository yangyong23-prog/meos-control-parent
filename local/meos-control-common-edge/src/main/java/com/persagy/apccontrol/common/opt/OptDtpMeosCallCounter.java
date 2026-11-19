package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.LongSupplier;

/**
 * 运行标准过渡 B3：dtp-meos 反向调用我方的 5 个接口按（接口, 项目, 调用方）计数，作为这些接口“可以下线”的证据。
 *
 * <p>{@code switch.opt.dtpMeosCallCounter} 关（默认）时不做任何事。开启后每次命中：
 * <ul>
 *   <li>Redis hash {@code apc:opt:dtpMeosCall:{yyyyMMdd}} 字段 {@code {接口}|{项目}|{调用方}} 自增，key 保留 40 天；</li>
 *   <li>hash {@code apc:opt:dtpMeosCall:last} 同字段记最近一次调用时间（毫秒）；</li>
 *   <li>进程内计数 {@code dtpMeosCall.{接口}}（随 [opt-metrics] 每分钟日志输出）。</li>
 * </ul>
 * 调用方按 User-Agent 区分 browser（页面）与 service（dtp-meos 等服务间调用），因为其中两个接口页面也会调用。
 * 计数失败只计 {@code dtpMeosCall.error}，不影响业务请求。接口本身不做任何改动。
 */
@Slf4j
public class OptDtpMeosCallCounter implements HandlerInterceptor {

    /** dtp-meos MeosControlServerClient 调用的 5 个接口 */
    public static final List<String> PATHS = Collections.unmodifiableList(Arrays.asList(
            "/demand/config/listConfigsByInstance",
            "/demand/listInstanceConfig",
            "/outside/intervention/taskConfigList",
            "/external/demand/config/listConfigResultsByInstance",
            "/valueSelection/currentApplyConfig"));

    static final String KEY_PREFIX = "apc:opt:dtpMeosCall:";
    static final String LAST_KEY = KEY_PREFIX + "last";
    private static final long KEEP_SECONDS = 40L * 24 * 3600;
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 计数存储，便于单测替换 */
    public interface Store {
        void increment(String key, String field, long keepSeconds);

        void put(String key, String field, String value);

        Map<String, String> entries(String key);
    }

    private final Store store;
    private final LongSupplier clock;
    private final ZoneId zone;

    public OptDtpMeosCallCounter(Store store, LongSupplier clock, ZoneId zone) {
        this.store = store;
        this.clock = clock;
        this.zone = zone;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!OptSwitch.on(OptSwitch.DTP_MEOS_CALL_COUNTER)) {
            return true;
        }
        String path = OptStandardsRequests.path(request);
        if (!PATHS.contains(path)) {
            return true;
        }
        String projectId = OptStandardsRequests.projectId(request);
        record(path, projectId == null ? "unknown" : projectId, OptStandardsRequests.caller(request));
        return true;
    }

    void record(String path, String projectId, String caller) {
        String api = path.substring(path.lastIndexOf('/') + 1);
        OptMetrics.incAlways("dtpMeosCall." + api);
        try {
            long now = clock.getAsLong();
            String field = path + "|" + projectId + "|" + caller;
            store.increment(dayKey(now), field, KEEP_SECONDS);
            store.put(LAST_KEY, field, String.valueOf(now));
        } catch (RuntimeException e) {
            OptMetrics.incAlways("dtpMeosCall.error");
            log.debug("[opt-standards] dtp-meos 调用计数失败：{}", e.toString());
        }
    }

    /**
     * 最近 days 天的计数（含今天），按 {日期: {字段: 次数}}，外加 last: {字段: 最近调用时间}。
     */
    public Map<String, Object> stats(int days) {
        int n = Math.min(Math.max(days, 1), 40);
        long now = clock.getAsLong();
        Map<String, Object> byDay = new LinkedHashMap<>();
        LocalDate today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate();
        for (int i = 0; i < n; i++) {
            String day = today.minusDays(i).format(DAY);
            Map<String, String> entries = store.entries(KEY_PREFIX + day);
            if (entries != null && !entries.isEmpty()) {
                byDay.put(day, new TreeMap<>(entries));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", OptSwitch.on(OptSwitch.DTP_MEOS_CALL_COUNTER));
        result.put("days", byDay);
        Map<String, String> last = store.entries(LAST_KEY);
        Map<String, String> lastReadable = new TreeMap<>();
        if (last != null) {
            last.forEach((k, v) -> lastReadable.put(k, readable(v)));
        }
        result.put("last", lastReadable);
        return result;
    }

    private String dayKey(long now) {
        return KEY_PREFIX + Instant.ofEpochMilli(now).atZone(zone).toLocalDate().format(DAY);
    }

    private String readable(String millis) {
        try {
            return Instant.ofEpochMilli(Long.parseLong(millis)).atZone(zone).toLocalDateTime().toString();
        } catch (NumberFormatException e) {
            return millis;
        }
    }
}
