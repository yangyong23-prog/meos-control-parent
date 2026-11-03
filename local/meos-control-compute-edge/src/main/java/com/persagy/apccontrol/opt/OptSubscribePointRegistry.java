package com.persagy.apccontrol.opt;

import cn.hutool.core.collection.CollUtil;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * opt 分支 B3：计算“应订阅的点”。
 * <ul>
 *     <li>类集合与原按类订阅一致：Redis {@link RedisKey#CLASS_NAME} + {@link InfoCodeConfigService#getClassCodesForTwinSubscribe()}；</li>
 *     <li>有信息点配置的类：订阅配置里的 infoCode，以及 extendInfo / forceInfo 中引用的 infoCode / triggerInfoCode / infoCodes（带 classCode 的归到对应类）；</li>
 *     <li>没有信息点配置的类：订阅该类全部点（与原行为一致，空集合表示全部）；</li>
 *     <li>补充：Nacos {@code opt.subscribe.extra-points}（"classCode:infoCode,..."）和自动补订阅记录（Redis set {@value #AUTO_KEY}）。</li>
 * </ul>
 * 漏订的点不会出错：读路径缓存未命中时回源 Twin，并经 {@link OptPointSubscribeService} 自动补订阅。
 */
@Component
public class OptSubscribePointRegistry {

    public static final String AUTO_KEY = "apc:opt:subscribe:auto";

    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private InfoCodeConfigService infoCodeConfigService;
    @Resource
    private LocalRedisCacheHandler localRedisCacheHandler;

    @Value("${opt.subscribe.extra-points:}")
    private String extraPoints;

    /**
     * @return classCode -> infoCodes，空集合表示该类全部点
     */
    public Map<String, Set<String>> expected() {
        Set<String> classes = subscribedClasses();
        Map<String, Set<String>> own = new HashMap<>();
        Map<String, Set<String>> refs = new HashMap<>();
        for (RcInfoCodeCfg cfg : localRedisCacheHandler.listInfoCodeConfigs(classes)) {
            String classCode = OptSubscribePoints.normalizeClass(cfg.getClassCode());
            if (StringUtils.isBlank(classCode)) {
                continue;
            }
            if (StringUtils.isNotBlank(cfg.getInfoCode())) {
                own.computeIfAbsent(classCode, k -> new TreeSet<>()).add(cfg.getInfoCode());
            }
            OptSubscribePoints.collectRefs(cfg.getExtendInfo(), classCode, refs);
            OptSubscribePoints.collectRefs(cfg.getForceInfo(), classCode, refs);
        }
        OptSubscribePoints.mergeInto(refs, OptSubscribePoints.parsePoints(extraPoints));
        OptSubscribePoints.mergeInto(refs, OptSubscribePoints.parsePoints(String.join(",", autoPoints())));
        return OptSubscribePoints.merge(classes, own, refs);
    }

    public Set<String> autoPoints() {
        Set<byte[]> raw = template()
                .execute((RedisCallback<Set<byte[]>>) c -> c.sMembers(bytes(AUTO_KEY)));
        Set<String> result = new TreeSet<>();
        if (raw != null) {
            for (byte[] b : raw) {
                result.add(new String(b, StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    public void persistAuto(Collection<String> points) {
        if (CollUtil.isEmpty(points)) {
            return;
        }
        byte[][] values = points.stream().map(OptSubscribePointRegistry::bytes).toArray(byte[][]::new);
        template().execute((RedisCallback<Long>) c -> c.sAdd(bytes(AUTO_KEY), values));
    }

    /**
     * RedisCacheUtil#getRedisTemplate 返回原始类型，直接 execute 会丢泛型；这里只用连接级回调。
     */
    @SuppressWarnings("unchecked")
    private RedisOperations<Object, Object> template() {
        return (RedisOperations<Object, Object>) redisCacheUtil.getRedisTemplate();
    }

    private Set<String> subscribedClasses() {
        Set<String> classes = new TreeSet<>();
        Map<String, Object> classMap = redisCacheUtil.hGetAll(RedisKey.CLASS_NAME);
        if (classMap != null) {
            classMap.keySet().forEach(c -> classes.add(OptSubscribePoints.normalizeClass(c)));
        }
        Set<String> configured = infoCodeConfigService.getClassCodesForTwinSubscribe();
        if (configured != null) {
            configured.forEach(c -> classes.add(OptSubscribePoints.normalizeClass(c)));
        }
        classes.remove("");
        return classes;
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
