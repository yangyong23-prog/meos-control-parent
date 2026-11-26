package com.persagy.apccontrol.basic.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.config.RedisCacheExpUtil;
import com.persagy.apccontrol.basic.config.cache.redis.RedisConfiguration;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地 Redis（switch.opt.localRedis）：用项目真实的 RedisTemplate 序列化配置和 RedisCacheUtil 跑在本地连接工厂上，
 * 验证业务实际用到的命令与 Redis 行为一致；共享键路由用另一个本地工厂充当“真实 Redis”。
 */
public class OptLocalRedisTest {

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private OptLocalRedisConnectionFactory factory;
    private RedisTemplate<String, Object> original;
    private RedisTemplate<String, Object> application;

    @BeforeEach
    void setUp() {
        factory = new OptLocalRedisConnectionFactory(new OptLocalRedisStore(now::get), null, null, 0);
        RedisConfiguration cfg = new RedisConfiguration(new ObjectMapper());
        original = cfg.redisTemplate(factory);
        original.afterPropertiesSet();
        application = cfg.applicationRedisTemplate(factory);
        application.afterPropertiesSet();
    }

    private static byte[] b(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void valueOperationsBehaveLikeRedis() {
        original.opsForValue().set("apc:a", "x");
        original.opsForValue().set("apc:n", 5);
        Assertions.assertEquals("x", original.opsForValue().get("apc:a"));
        Assertions.assertEquals(7L, original.opsForValue().increment("apc:n", 2));
        Assertions.assertEquals(6L, original.opsForValue().decrement("apc:n"));
        Assertions.assertEquals(Arrays.asList("x", null), original.opsForValue().multiGet(Arrays.asList("apc:a", "apc:none")));
        Assertions.assertTrue(original.opsForValue().setIfAbsent("apc:nx", 1));
        Assertions.assertFalse(original.opsForValue().setIfAbsent("apc:nx", 2));
        Assertions.assertTrue(original.hasKey("apc:a"));
        Assertions.assertTrue(original.delete("apc:a"));
        Assertions.assertFalse(original.hasKey("apc:a"));
    }

    @Test
    void expiryUsesClockAndTtlMatchesRedis() {
        original.opsForValue().set("k", "v", Duration.ofSeconds(10));
        Assertions.assertEquals(10L, original.getExpire("k"));
        original.opsForValue().set("p", "v");
        Assertions.assertEquals(-1L, original.getExpire("p"));
        Assertions.assertEquals(-2L, original.getExpire("missing"));
        now.addAndGet(9_000);
        Assertions.assertEquals("v", original.opsForValue().get("k"));
        now.addAndGet(1_000);
        Assertions.assertNull(original.opsForValue().get("k"));
        Assertions.assertTrue(original.expire("p", 5, TimeUnit.SECONDS));
        now.addAndGet(5_000);
        Assertions.assertFalse(original.hasKey("p"));
        // 过期键在定期清理时释放
        original.opsForValue().set("s", "v", Duration.ofSeconds(1));
        now.addAndGet(2_000);
        Assertions.assertEquals(1, factory.store().sweepExpired());
    }

    @Test
    void hashListAndSetOperations() {
        original.opsForHash().put("apc:h", "f1", "v1");
        original.opsForHash().putAll("apc:h", new HashMap<String, Object>() {{
            put("f2", 2);
        }});
        Assertions.assertEquals("v1", original.opsForHash().get("apc:h", "f1"));
        Assertions.assertEquals(2, original.opsForHash().size("apc:h"));
        Map<Object, Object> all = original.opsForHash().entries("apc:h");
        Assertions.assertEquals(2, all.get("f2"));
        Assertions.assertEquals(1L, original.opsForHash().delete("apc:h", "f1", "nope"));
        Assertions.assertEquals(1L, original.opsForHash().delete("apc:h", "f2"));
        // 最后一个字段删掉后整个键消失（与 Redis 一致）
        Assertions.assertFalse(original.hasKey("apc:h"));

        for (int i = 0; i < 5; i++) {
            original.opsForList().leftPush("apc:l", i);
        }
        original.opsForList().trim("apc:l", 0, 2);
        Assertions.assertEquals(Arrays.asList(4, 3, 2), original.opsForList().range("apc:l", 0, -1));

        original.opsForSet().add("apc:s", "a", "b", "a");
        Assertions.assertEquals(2L, original.opsForSet().size("apc:s"));
        Assertions.assertTrue(original.opsForSet().isMember("apc:s", "b"));
    }

    @Test
    void wrongTypeAndUnsupportedCommandsThrowDataAccessException() {
        original.opsForValue().set("apc:str", "x");
        Assertions.assertThrows(InvalidDataAccessApiUsageException.class,
                () -> original.opsForHash().put("apc:str", "f", "v"));
        Assertions.assertThrows(InvalidDataAccessApiUsageException.class,
                () -> original.opsForZSet().add("apc:z", "m", 1));
    }

    @Test
    void scanAndApplicationPrefixMatchRealKeys() {
        application.opsForValue().set("apc:cache_object_model_status:G:P:X:1", "READY");
        original.opsForValue().set("apc:info_code_config_new:C1:I1", "cfg");
        original.opsForValue().set("apc:info_code_config_new:C1:I2", "cfg");
        original.opsForValue().set("apc:info_code_config_new:C2:I1", "cfg");
        // applicationRedisTemplate 写入的键带应用前缀
        Set<String> appKeys = RedisCacheExpUtil.scanKeys(original, "meos-control*");
        Assertions.assertEquals(1, appKeys.size());
        Assertions.assertTrue(appKeys.iterator().next().endsWith("::apc:cache_object_model_status:G:P:X:1"));
        Assertions.assertEquals(new HashSet<>(Arrays.asList("apc:info_code_config_new:C1:I1", "apc:info_code_config_new:C1:I2")),
                RedisCacheExpUtil.scanKeys(original, "apc:info_code_config_new:C1:*"));
        Assertions.assertEquals(2L, RedisCacheExpUtil.delKeys(original, "apc:info_code_config_new:C1:*"));
        Assertions.assertEquals(1, original.keys("apc:info_code_config_new:*").size());
        Assertions.assertEquals("READY", application.opsForValue().get("apc:cache_object_model_status:G:P:X:1"));
    }

    @Test
    void globSupportsRedisSyntax() {
        OptLocalRedisStore store = new OptLocalRedisStore(now::get);
        Assertions.assertTrue(store.compile("a?c").matcher("abc").matches());
        Assertions.assertTrue(store.compile("a[bx]c").matcher("axc").matches());
        Assertions.assertFalse(store.compile("a[^b]c").matcher("abc").matches());
        Assertions.assertTrue(store.compile("a[a-c]*").matcher("abzz").matches());
        Assertions.assertTrue(store.compile("x.y*").matcher("x.y1").matches());
        Assertions.assertFalse(store.compile("x.y*").matcher("xzy1").matches());
        Assertions.assertTrue(store.compile("a\\*b").matcher("a*b").matches());
        Assertions.assertTrue(store.compile("{id}|(x)+*").matcher("{id}|(x)+tail").matches());
    }

    @Test
    void redisCacheUtilWorksOnLocalFactory() {
        RedisCacheUtil util = new RedisCacheUtil(original);
        util.afterSingletonsInstantiated();
        util.set("apc:automatic:o1:runStatus", 1);
        util.setEx("apc:intervene::o1:i1", 30, Duration.ofSeconds(60));
        util.hSet("apc:class_name", "AHU", "空调机组");
        Assertions.assertEquals(Integer.valueOf(1), util.get("apc:automatic:o1:runStatus"));
        Assertions.assertEquals("空调机组", util.hGet("apc:class_name", "AHU"));
        List<Object> both = util.mGet("apc:automatic:o1:runStatus", "apc:intervene::o1:i1", "apc:none");
        Assertions.assertEquals(Arrays.asList(1, 30, null), both);
        Assertions.assertEquals(2L, util.del(Arrays.asList("apc:automatic:o1:runStatus", "apc:intervene::o1:i1")));
    }

    @Test
    void redisCacheManagerWorksOnLocalFactory() {
        RedisCacheManager manager = RedisCacheManager.builder(factory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)))
                .build();
        manager.afterPropertiesSet();
        manager.getCache("weather").put("k", "v");
        Assertions.assertEquals("v", manager.getCache("weather").get("k").get());
        manager.getCache("weather").clear();
        Assertions.assertNull(manager.getCache("weather").get("k"));
    }

    // ------------------------------------------------------------------------------------------ 共享键路由

    @Test
    void sharedKeysGoToRealRedisAndPrivateKeysStayLocal() {
        OptLocalRedisConnectionFactory realRedis =
                new OptLocalRedisConnectionFactory(new OptLocalRedisStore(now::get), null, null, 0);
        // 真实 Redis 里遗留的旧私有键，路由模式下不能被读回来
        realRedis.store().set("apc:info_code_config_new:OLD:I", b("\"old\""), 0, false, false, false);
        realRedis.store().set("apc:automatic:o0:runStatus", b("1"), 0, false, false, false);

        OptLocalRedisConnectionFactory hybrid = new OptLocalRedisConnectionFactory(new OptLocalRedisStore(now::get),
                new OptLocalRedisRouter(OptLocalRedisRouter.DEFAULT_SHARED_PREFIXES), realRedis, 0);
        RedisTemplate<String, Object> t = new RedisConfiguration(new ObjectMapper()).redisTemplate(hybrid);
        t.afterPropertiesSet();
        RedisTemplate<String, Object> app = new RedisConfiguration(new ObjectMapper()).applicationRedisTemplate(hybrid);
        app.afterPropertiesSet();

        t.opsForValue().set("apc:automatic:o1:runStatus", 1);
        t.opsForValue().set("apc:intervene::o1:i1", 30, Duration.ofSeconds(60));
        t.opsForValue().set("apc:iot_ctrl_data:o1:i1", "on");
        app.opsForValue().set("apc:obj_infocode_forced:o1:i1", 1);
        t.opsForValue().set("apc:info_code_config_new:C1:I1", "cfg");

        Assertions.assertTrue(realRedis.store().exists("apc:automatic:o1:runStatus"));
        Assertions.assertTrue(realRedis.store().exists("apc:intervene::o1:i1"));
        Assertions.assertTrue(realRedis.store().pTtl("apc:intervene::o1:i1") > 0);
        Assertions.assertTrue(realRedis.store().exists("apc:iot_ctrl_data:o1:i1"));
        Assertions.assertTrue(realRedis.store().exists("meos-control::apc:obj_infocode_forced:o1:i1"));
        Assertions.assertFalse(realRedis.store().exists("apc:info_code_config_new:C1:I1"));
        Assertions.assertTrue(hybrid.store().exists("apc:info_code_config_new:C1:I1"));
        Assertions.assertFalse(hybrid.store().exists("apc:automatic:o1:runStatus"));

        // 多键命令拆分合并，顺序保持
        Assertions.assertEquals(Arrays.asList(1, "cfg", null),
                t.opsForValue().multiGet(Arrays.asList("apc:automatic:o1:runStatus", "apc:info_code_config_new:C1:I1", "x")));

        // KEYS/SCAN：合并本地 + 真实 Redis 中的共享键，旧私有键不会回来
        Assertions.assertEquals(new HashSet<>(Arrays.asList("apc:automatic:o0:runStatus", "apc:automatic:o1:runStatus")),
                RedisCacheExpUtil.scanKeys(t, "apc:automatic:*"));
        Assertions.assertEquals(new HashSet<>(Arrays.asList("apc:info_code_config_new:C1:I1")),
                RedisCacheExpUtil.scanKeys(t, "apc:info_code_config_new:*"));
        Set<String> all = RedisCacheExpUtil.scanKeys(t, "*");
        Assertions.assertFalse(all.contains("apc:info_code_config_new:OLD:I"));
        Assertions.assertTrue(all.contains("apc:iot_ctrl_data:o1:i1"));

        Assertions.assertEquals(2L, t.delete(Arrays.asList("apc:automatic:o1:runStatus", "apc:info_code_config_new:C1:I1")));
        Assertions.assertFalse(realRedis.store().exists("apc:automatic:o1:runStatus"));
        Assertions.assertFalse(hybrid.store().exists("apc:info_code_config_new:C1:I1"));
    }

    @Test
    void sharedConnectionIsOpenedLazilyAndClosedWithLocalConnection() {
        AtomicLong opened = new AtomicLong();
        OptLocalRedisConnectionFactory realRedis =
                new OptLocalRedisConnectionFactory(new OptLocalRedisStore(now::get), null, null, 0) {
                    @Override
                    public RedisConnection getConnection() {
                        opened.incrementAndGet();
                        return super.getConnection();
                    }
                };
        OptLocalRedisConnectionFactory hybrid = new OptLocalRedisConnectionFactory(new OptLocalRedisStore(now::get),
                new OptLocalRedisRouter(OptLocalRedisRouter.DEFAULT_SHARED_PREFIXES), realRedis, 0);
        RedisConnection c = hybrid.getConnection();
        c.set(b("apc:info_code_config_new:a"), b("1"));
        c.get(b("apc:info_code_config_new:a"));
        Assertions.assertEquals(0, opened.get());
        c.set(b("apc:automatic:o:i"), b("1"));
        c.get(b("apc:automatic:o:i"));
        Assertions.assertEquals(1, opened.get());
        c.close();
        Assertions.assertTrue(c.isClosed());
    }

    @Test
    void routerStripsApplicationPrefixAndJudgesPatterns() {
        OptLocalRedisRouter r = new OptLocalRedisRouter(OptLocalRedisRouter.DEFAULT_SHARED_PREFIXES);
        Assertions.assertTrue(r.isShared("apc:automatic:o:i"));
        // 干预键本身带 "::"，不能被当成应用前缀剥掉
        Assertions.assertTrue(r.isShared("apc:intervene::o1:i1"));
        Assertions.assertTrue(r.isShared("meos-control::apc:intervene::o1:i1"));
        Assertions.assertTrue(r.mayMatchShared("apc:intervene::o1*"));
        Assertions.assertFalse(r.isShared("cache::apc:info_code_config_new:x"));
        Assertions.assertTrue(r.isShared("meos-control::apc:system_running_model:p:s"));
        Assertions.assertFalse(r.isShared("apc:info_code_config_new:c:i"));
        Assertions.assertFalse(r.isShared("meos-control::apc:cache_object_model_status:x"));
        Assertions.assertTrue(r.mayMatchShared("apc:intervene*i1*"));
        Assertions.assertTrue(r.mayMatchShared("apc:*"));
        Assertions.assertTrue(r.mayMatchShared("*"));
        Assertions.assertTrue(r.mayMatchShared("meos-control::apc:automatic:*"));
        Assertions.assertTrue(r.mayMatchShared("meos-control:*"));
        Assertions.assertFalse(r.mayMatchShared("apc:info_code_config_new:C1:*"));
        Assertions.assertFalse(r.mayMatchShared("meos-control::apc:cache_object_model_status:*"));
        Assertions.assertFalse(r.mayMatchShared("apc:info_code_config_new:C1:I1"));
    }
}
