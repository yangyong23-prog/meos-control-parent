package com.persagy.apccontrol.basic.opt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 本地 Redis（switch.opt.localRedis）的共享键路由：判断一个键是否属于与 dtp-meos 共享、必须留在真实 Redis 的数据。
 *
 * <p>键在 Redis 里可能带应用前缀（applicationRedisTemplate 写成 {@code meos-control::apc:...}，Spring Cache 写成
 * {@code cacheName::key}）。判断时原键或去掉应用前缀后的键以共享前缀开头即视为共享。
 *
 * <p>默认共享前缀（{@code opt.local-redis.shared-prefixes} 可覆盖）：
 * <ul>
 *   <li>{@code apc:automatic:} 手自动状态</li>
 *   <li>{@code apc:intervene:} 系统干预（带过期，实际键形如 {@code apc:intervene::obj:info}）</li>
 *   <li>{@code apc:system_running_model:} 系统运行模式</li>
 *   <li>{@code apc:obj_infocode_forced:} 对象信息点强制（dtp-meos 会删除）</li>
 *   <li>{@code apc:iot_ctrl_data:} 控制值（dtp-meos StrategyServiceImpl 读取）</li>
 *   <li>{@code apc:iot_real_time_data:} 实时值共享缓存（契约 docs/opt/contract-iot-real-time-data.md 写明 dtp-meos 是读方；
 *       确认 dtp-meos 未读取后可从前缀里去掉，改为本地）</li>
 * </ul>
 */
public final class OptLocalRedisRouter {

    public static final List<String> DEFAULT_SHARED_PREFIXES = Collections.unmodifiableList(java.util.Arrays.asList(
            "apc:automatic:",
            "apc:intervene:",
            "apc:system_running_model:",
            "apc:obj_infocode_forced:",
            "apc:iot_ctrl_data:",
            "apc:iot_real_time_data:"
    ));

    private static final String APP_SEPARATOR = "::";

    private final List<String> prefixes;

    public OptLocalRedisRouter(List<String> sharedPrefixes) {
        List<String> p = new ArrayList<>();
        for (String s : sharedPrefixes) {
            if (s != null && !s.trim().isEmpty()) {
                p.add(s.trim());
            }
        }
        this.prefixes = Collections.unmodifiableList(p);
    }

    public List<String> prefixes() {
        return prefixes;
    }

    /** 去掉应用前缀（第一个 "::" 之前且不含 ':' 的部分）后的逻辑键；没有应用前缀时返回 null */
    private static String stripApplicationPrefix(String key) {
        int i = key.indexOf(APP_SEPARATOR);
        if (i <= 0 || key.lastIndexOf(':', i - 1) >= 0) {
            return null;
        }
        return key.substring(i + APP_SEPARATOR.length());
    }

    private boolean startsWithShared(String k) {
        for (String p : prefixes) {
            if (k.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 键是否属于共享数据（ISO-8859-1 映射后的键）。注意干预键本身就含 "::"（{@code apc:intervene::obj:info}），
     * 所以先按原键判断，再按去掉应用前缀后的键判断。
     */
    public boolean isShared(String key) {
        if (startsWithShared(key)) {
            return true;
        }
        String stripped = stripApplicationPrefix(key);
        return stripped != null && startsWithShared(stripped);
    }

    /**
     * KEYS/SCAN 的 glob 是否可能匹配到共享键。可能误判为“可能”（只多查一次真实 Redis，结果仍会按 {@link #isShared} 过滤），
     * 但不会漏判。
     */
    public boolean mayMatchShared(String glob) {
        int wildcard = firstWildcard(glob);
        if (wildcard < 0) {
            return isShared(glob);
        }
        String literal = glob.substring(0, wildcard);
        if (compatible(literal)) {
            return true;
        }
        String stripped = stripApplicationPrefix(literal);
        if (stripped != null) {
            return compatible(stripped);
        }
        // 字面量里还没出现 "::"，它可能只是应用前缀的一部分（如 "meos-control:"、"meos-*"）
        int colon = literal.indexOf(':');
        return colon < 0 || colon == literal.length() - 1;
    }

    private boolean compatible(String literal) {
        for (String p : prefixes) {
            if (literal.startsWith(p) || p.startsWith(literal)) {
                return true;
            }
        }
        return false;
    }

    private static int firstWildcard(String glob) {
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*' || c == '?' || c == '[' || c == '\\') {
                return i;
            }
        }
        return -1;
    }
}
