package com.persagy.apccontrol.basic.config;

import cn.hutool.core.collection.CollUtil;
import com.persagy.apccontrol.basic.config.cache.redis.RedisConfiguration;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Redis key 扫描工具
 *
 * <p>线上禁用 {@code KEYS} 命令后，使用 {@code SCAN} 命令遍历匹配 {@code pattern} 的 key，避免阻塞 Redis。
 * 核心逻辑为静态方法 {@link #scanKeys(RedisTemplate, String)}、{@link #scanKeys(RedisTemplate, String, long)}、
 * {@link #delKeys(RedisTemplate, String)}、{@link #delKeys(RedisTemplate, String, long)}，支持传入任意 RedisTemplate；
 * 同时提供实例方法 {@link #scanKeys(String)}、{@link #scanKeys(String, long)}、{@link #delKeys(String)}、
 * {@link #delKeys(String, long)}，基于原始 redisTemplate（key 不带应用名前缀）操作。
 *
 * <p>注意：{@code SCAN} 是增量迭代，返回结果可能重复（本工具使用 Set 去重），迭代期间的 key 增减不保证可见，语义与 {@code KEYS} 略有差异。
 *
 * @author JiYinchuan
 */
@Component
public class RedisCacheExpUtil {

    /**
     * 每次 SCAN 迭代的默认提示数量
     */
    private static final long DEFAULT_COUNT = 1000L;

    /**
     * 原始 redisTemplate（key 不带应用名前缀）
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造器
     *
     * <p>依赖 {@code @Primary} 注入原始 redisTemplate（key 不带应用名前缀），与 {@link RedisConfiguration} 的默认模板保持一致。
     *
     * @param redisTemplate 原始 redisTemplate
     */
    public RedisCacheExpUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 使用 SCAN 命令遍历匹配 pattern 的 key
     *
     * @param pattern 匹配模式，如 "user:*"
     * @return 匹配的 key 集合
     */
    public Set<String> scanKeys(String pattern) {
        return scanKeys(redisTemplate, pattern);
    }

    /**
     * 使用 SCAN 命令遍历匹配 pattern 的 key
     *
     * @param pattern 匹配模式，如 "user:*"
     * @param cnt     每次迭代返回的大致数量
     * @return 匹配的 key 集合
     */
    public Set<String> scanKeys(String pattern, long cnt) {
        return scanKeys(redisTemplate, pattern, cnt);
    }

    /**
     * 删除匹配 pattern 的 key
     *
     * @param pattern 匹配模式，如 "user:*"
     * @return 实际删除的 key 数量
     */
    public long delKeys(String pattern) {
        return delKeys(redisTemplate, pattern);
    }

    /**
     * 删除匹配 pattern 的 key
     *
     * @param pattern 匹配模式，如 "user:*"
     * @param cnt     每次迭代返回的大致数量
     * @return 实际删除的 key 数量
     */
    public long delKeys(String pattern, long cnt) {
        return delKeys(redisTemplate, pattern, cnt);
    }

    /**
     * 使用 SCAN 命令遍历匹配 pattern 的 key
     *
     * @param redisTemplate 操作使用的模板，key 序列化语义与存储端保持一致
     * @param pattern       匹配模式，如 "user:*"
     * @return 匹配的 key 集合
     */
    public static Set<String> scanKeys(RedisTemplate<String, Object> redisTemplate, String pattern) {
        return scanKeys(redisTemplate, pattern, DEFAULT_COUNT);
    }

    /**
     * 使用 SCAN 命令遍历匹配 pattern 的 key
     *
     * <p>pattern 会通过 {@code redisTemplate} 的 keySerializer 序列化后再匹配，
     * 确保与存储 key 的命名空间一致（例如 {@link RedisConfiguration.KeyWithApplicationSerializer} 自动加应用名前缀）。
     *
     * @param redisTemplate 操作使用的模板，key 序列化语义与存储端保持一致
     * @param pattern       匹配模式，如 "user:*"
     * @param cnt           每次迭代返回的大致数量
     * @return 匹配的 key 集合
     */
    public static Set<String> scanKeys(RedisTemplate<String, Object> redisTemplate, String pattern, long cnt) {
        Set<String> keys = CollUtil.newHashSet();
        // 通过 keySerializer 序列化 pattern，确保与 KEYS 命令的语义一致
        // （无前缀模板序列化后不变，带前缀模板如 KeyWithApplicationSerializer 会自动加应用名前缀）
        @SuppressWarnings("unchecked")
        RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        byte[] rawPattern = keySerializer.serialize(pattern);
        String actualPattern = rawPattern != null ? new String(rawPattern, StandardCharsets.UTF_8) : pattern;
        ScanOptions options = ScanOptions.scanOptions().match(actualPattern).count(cnt).build();
        redisTemplate.execute((RedisCallback<Object>) conn -> {
            if (conn instanceof RedisClusterConnection) {
                // 集群模式下遍历所有主节点
                RedisClusterConnection clusterConn = (RedisClusterConnection) conn;
                for (RedisClusterNode node : clusterConn.clusterGetNodes()) {
                    if (node.isMaster()) {
                        collectKeys(clusterConn.scan(node, options), keys);
                    }
                }
            } else {
                collectKeys(conn.scan(options), keys);
            }
            return null;
        });
        return keys;
    }

    /**
     * 删除匹配 pattern 的 key
     *
     * @param redisTemplate 操作使用的模板，key 序列化语义与存储端保持一致
     * @param pattern       匹配模式，如 "user:*"
     * @return 实际删除的 key 数量
     */
    public static long delKeys(RedisTemplate<String, Object> redisTemplate, String pattern) {
        return delKeys(redisTemplate, pattern, DEFAULT_COUNT);
    }

    /**
     * 删除匹配 pattern 的 key
     *
     * @param redisTemplate 操作使用的模板，key 序列化语义与存储端保持一致
     * @param pattern       匹配模式，如 "user:*"
     * @param cnt           每次迭代返回的大致数量
     * @return 实际删除的 key 数量
     */
    public static long delKeys(RedisTemplate<String, Object> redisTemplate, String pattern, long cnt) {
        Set<String> keys = scanKeys(redisTemplate, pattern, cnt);
        // 集群模式下跨 slot 的 key 无法批量 DEL，逐 key 删除兼容单机与集群
        keys.forEach(redisTemplate::delete);
        return keys.size();
    }

    /**
     * 收集游标中的 key，使用 Set 去重
     *
     * @param cursor 扫描游标，使用后关闭
     * @param keys   key 集合
     */
    private static void collectKeys(Cursor<byte[]> cursor, Set<String> keys) {
        try (Cursor<byte[]> c = cursor) {
            while (c.hasNext()) {
                keys.add(new String(c.next(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            // 游标关闭异常不影响已收集的 key，忽略
        }
    }
}
