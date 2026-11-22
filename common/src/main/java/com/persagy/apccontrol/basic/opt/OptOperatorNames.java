package com.persagy.apccontrol.basic.opt;

import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * opt 分支 B15（switch.opt.operatorNameLocal，默认关）：操作人姓名不再向 mid-permission 反查。
 * <p>
 * 边缘前端在请求头 {@value #HEADER} 里带上主框架登录信息中的姓名（encodeURIComponent 编码），
 * {@link OptOperatorNameFilter} 收到后按 userId 记下：进程内存一份，Redis hash {@value #REDIS_KEY} 一份
 * （compute-edge 等其它进程处理 Twin 回执时也能读到）。姓名不变时不重复写 Redis。
 * <p>
 * 手动指令回执写运行日志时（RunningLogHandle → MidPermissionClientService#getUserNameByUserIds）按 userId 取姓名；
 * 取不到时不返回该 userId，调用方保持原样写 userId。
 * <p>
 * 姓名只用于显示，不参与任何权限判断。
 */
@Slf4j
@Component
public class OptOperatorNames {

    public static final String HEADER = "opt-user-name";
    static final String REDIS_KEY = "apc:opt:operator_name";
    static final int MAX_NAME_LENGTH = 64;
    static final int MAX_LOCAL_ENTRIES = 10_000;

    private final ConcurrentMap<String, String> local = new ConcurrentHashMap<>();

    @Lazy
    @Resource
    private RedisCacheUtil redisCacheUtil;

    /**
     * 记下 userId 对应的姓名。
     *
     * @param userId     请求头 userId
     * @param headerName 请求头里的姓名（URL 编码）
     */
    public void remember(String userId, String headerName) {
        String name = decode(headerName);
        if (StringUtils.isBlank(userId) || name == null || name.equals(userId)) {
            return;
        }
        if (name.equals(local.get(userId))) {
            return;
        }
        if (local.size() >= MAX_LOCAL_ENTRIES) {
            local.clear();
        }
        local.put(userId, name);
        try {
            redisCacheUtil.getRedisTemplate().opsForHash().put(REDIS_KEY, userId, name);
            OptMetrics.inc("operatorName.write");
        } catch (RuntimeException e) {
            // 写失败下次请求再试
            local.remove(userId, name);
            log.warn("[opt-operator] 写操作人姓名失败 userId={}：{}", userId, e.toString());
        }
    }

    /**
     * 按 userId 取姓名，只返回取到的。
     */
    public Map<String, String> lookupAll(Collection<String> userIds) {
        Map<String, String> result = new HashMap<>();
        if (userIds == null) {
            return result;
        }
        for (String userId : userIds) {
            String name = lookup(userId);
            if (name != null) {
                result.put(userId, name);
            }
        }
        return result;
    }

    public String lookup(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        String name = local.get(userId);
        if (name != null) {
            OptMetrics.inc("operatorName.hit");
            return name;
        }
        try {
            Object v = redisCacheUtil.getRedisTemplate().opsForHash().get(REDIS_KEY, userId);
            if (v != null && StringUtils.isNotBlank(v.toString())) {
                name = v.toString();
                if (local.size() < MAX_LOCAL_ENTRIES) {
                    local.put(userId, name);
                }
                OptMetrics.inc("operatorName.hit");
                return name;
            }
        } catch (RuntimeException e) {
            log.warn("[opt-operator] 读操作人姓名失败 userId={}：{}", userId, e.toString());
        }
        OptMetrics.inc("operatorName.miss");
        return null;
    }

    /** URL 解码、去空白、截断；空串或解码失败的非法输入返回 null */
    static String decode(String raw) {
        if (raw == null) {
            return null;
        }
        String s;
        try {
            s = URLDecoder.decode(raw.replace("+", "%2B"), "UTF-8");
        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
            s = raw;
        }
        s = s.trim();
        if (s.isEmpty()) {
            return null;
        }
        return s.length() > MAX_NAME_LENGTH ? s.substring(0, MAX_NAME_LENGTH) : s;
    }

    void putLocalForTest(String userId, String name) {
        local.put(userId, Objects.requireNonNull(name));
    }
}
