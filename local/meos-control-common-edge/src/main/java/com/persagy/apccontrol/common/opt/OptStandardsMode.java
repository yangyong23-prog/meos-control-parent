package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.opt.OptMetrics;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

/**
 * 运行标准过渡 B1：判定项目是否已切到新运行标准（ids-mpc-operation-standards-server）。
 *
 * <ul>
 *   <li>{@code switch.opt.standardsMode} 关（默认）：恒为 LEGACY，不查库。</li>
 *   <li>{@code opt.standards.mode-source=db}（默认）：读 dtp-meos 的 demand_custom_config · STANDRAD_CONFIG，
 *       与 dtp-meos 自己的判定口径一致；查询失败（如表不存在、库已拆分）按 LEGACY 处理并在结果里带上 error。</li>
 *   <li>{@code opt.standards.mode-source=list}：只看 {@code opt.standards.switched-projects}（逗号分隔），
 *       用于 meos-control 与 dtp-meos 分库后、或联调时人工指定。</li>
 * </ul>
 * 结果按项目缓存 {@code opt.standards.mode-cache-seconds}（默认 60）秒，查询失败也缓存，避免每次请求打库。
 */
@Slf4j
public class OptStandardsMode {

    public static final String STANDARD = "STANDARD";
    public static final String LEGACY = "LEGACY";

    /** 判定数据源：返回项目是否存在 STANDRAD_CONFIG 记录 */
    public interface Source {
        boolean hasStandardConfig(String projectId);
    }

    private final Source source;
    private final LongSupplier clock;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    public OptStandardsMode(Source source, LongSupplier clock) {
        this.source = source;
        this.clock = clock;
    }

    /** 已切新标准（开关关闭、项目为空或查询失败时为 false） */
    public boolean isStandard(String projectId) {
        return STANDARD.equals(resolve(projectId).get("mode"));
    }

    /**
     * @return projectId、mode（STANDARD / LEGACY）、readOnly、source、error（可能为 null）
     */
    public Map<String, Object> resolve(String projectId) {
        if (!OptSwitch.on(OptSwitch.STANDARDS_MODE)) {
            return result(projectId, false, "switch-off", null);
        }
        if (projectId == null || projectId.trim().isEmpty()) {
            return result(projectId, false, "no-project", null);
        }
        String sourceName = OptSwitch.value("standards.mode-source", "db");
        if ("list".equalsIgnoreCase(sourceName)) {
            return result(projectId, switchedProjects().contains(projectId), "list", null);
        }
        long now = clock.getAsLong();
        long ttlMillis = Math.max(OptSwitch.number("standards.mode-cache-seconds", 60L), 0L) * 1000L;
        Cached c = cache.get(projectId);
        if (c == null || now - c.at >= ttlMillis) {
            c = load(projectId, now);
            cache.put(projectId, c);
        }
        return result(projectId, c.standard, "db", c.error);
    }

    /** 清空缓存（切换项目后可立即生效） */
    public void evict() {
        cache.clear();
    }

    private Cached load(String projectId, long now) {
        try {
            boolean standard = source.hasStandardConfig(projectId);
            OptMetrics.inc("standards.mode.load");
            return new Cached(standard, null, now);
        } catch (RuntimeException e) {
            OptMetrics.incAlways("standards.mode.error");
            log.warn("[opt-standards] 读取 demand_custom_config STANDRAD_CONFIG 失败，项目 {} 按 LEGACY 处理：{}", projectId, e.toString());
            return new Cached(false, e.getClass().getSimpleName() + ": " + e.getMessage(), now);
        }
    }

    private static Set<String> switchedProjects() {
        String v = OptSwitch.value("standards.switched-projects", "");
        if (v.isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.stream(v.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private static Map<String, Object> result(String projectId, boolean standard, String source, String error) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("projectId", projectId);
        r.put("mode", standard ? STANDARD : LEGACY);
        r.put("readOnly", standard && OptSwitch.on(OptSwitch.STANDARDS_READONLY));
        r.put("source", source);
        r.put("error", error);
        return r;
    }

    private static final class Cached {
        final boolean standard;
        final String error;
        final long at;

        Cached(boolean standard, String error, long at) {
            this.standard = standard;
            this.error = error;
            this.at = at;
        }
    }
}
