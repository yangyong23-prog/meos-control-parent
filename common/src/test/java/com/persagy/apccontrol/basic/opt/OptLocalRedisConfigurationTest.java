package com.persagy.apccontrol.basic.opt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import com.persagy.framework.redis.lock.RedisLockUtil;
import com.persagy.framework.redis.lock.RedissonLockAutoConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * 本地 Redis 的 Bean 装配：用与线上相同的自动配置（Spring Boot Redis、Redisson starter、框架 starter-redis 与
 * paas.lock）在<b>没有 Redis</b>的环境里启动上下文，确认开关打开后不连 Redis、Bean 都换成本地实现；开关关闭时不注册任何本地 Bean。
 */
public class OptLocalRedisConfigurationTest {

    /** 框架的自动配置与 Spring Boot 的同名（简单类名都是 RedisAutoConfiguration），测试里换个名字注册 */
    @org.springframework.context.annotation.Configuration
    static class PaasRedisAutoConfiguration extends com.persagy.framework.redis.config.RedisAutoConfiguration {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withUserConfiguration(OptLocalRedisConfiguration.class)
            .withConfiguration(AutoConfigurations.of(
                    RedissonAutoConfiguration.class,
                    RedisAutoConfiguration.class,
                    PaasRedisAutoConfiguration.class,
                    RedissonLockAutoConfiguration.class))
            .withPropertyValues(
                    "spring.application.name=meos-control-server-edge",
                    // 指向不存在的端口：只要有任何组件在启动时连接 Redis，上下文就会失败
                    "spring.redis.host=127.0.0.1",
                    "spring.redis.port=1",
                    "spring.redis.timeout=1s",
                    "paas.lock.enabled=true",
                    "paas.lock.address=redis://127.0.0.1:1");

    @Test
    void switchOnStartsWithoutRedisAndUsesLocalBeans() {
        runner.withPropertyValues("switch.opt.localRedis=true", "opt.local-redis.shared=false").run(ctx -> {
            Assertions.assertNull(ctx.getStartupFailure());
            Assertions.assertTrue(ctx.getBean(RedisConnectionFactory.class) instanceof OptLocalRedisConnectionFactory);
            Assertions.assertTrue(ctx.getBeansOfType(LettuceConnectionFactory.class).isEmpty());
            Assertions.assertEquals("OptLocalRedissonClient", ctx.getBean(RedissonClient.class).toString());
            Assertions.assertEquals(ctx.getBean(RedissonClient.class), ctx.getBean(RedisLockUtil.class).getRedissonClient());
            // 4.91 实测：Redisson starter 的 redissonReactive / redissonRx 会被实例化（@Lazy 也会被依赖拉起），不能抛异常
            Assertions.assertNotNull(ctx.getBean(org.redisson.api.RedissonReactiveClient.class));
            Assertions.assertNotNull(ctx.getBean(org.redisson.api.RedissonRxClient.class));
            RedisCacheUtil util = ctx.getBean(RedisCacheUtil.class);
            util.set("apc:info_code_config_new:C:I", "cfg");
            Assertions.assertEquals("cfg", util.get("apc:info_code_config_new:C:I"));
        });
    }

    @Test
    void sharedModeConnectsToRedisLazily() {
        runner.withPropertyValues("switch.opt.localRedis=true").run(ctx -> {
            Assertions.assertNull(ctx.getStartupFailure());
            OptLocalRedisConnectionFactory f = ctx.getBean(OptLocalRedisConnectionFactory.class);
            Assertions.assertTrue(f.sharedRouting());
            RedisCacheUtil util = ctx.getBean(RedisCacheUtil.class);
            // 私有键不碰 Redis
            util.set("apc:class_name", "x");
            Assertions.assertEquals("x", util.get("apc:class_name"));
            // 共享键才连接 Redis（这里端口不存在，所以报连接错误）
            Assertions.assertThrows(RuntimeException.class, () -> util.set("apc:automatic:o:i", 1));
        });
    }

    @Test
    void refusesToStartOnCloudService() {
        runner.withPropertyValues("switch.opt.localRedis=true", "spring.application.name=meos-control-server")
                .run(ctx -> Assertions.assertNotNull(ctx.getStartupFailure()));
    }

    @Test
    void switchOffRegistersNothing() {
        new ApplicationContextRunner().withUserConfiguration(OptLocalRedisConfiguration.class).run(ctx -> {
            Assertions.assertNull(ctx.getStartupFailure());
            Assertions.assertTrue(ctx.getBeansOfType(OptLocalRedisConnectionFactory.class).isEmpty());
            Assertions.assertTrue(ctx.getBeansOfType(OptLocalLocks.class).isEmpty());
        });
    }
}
