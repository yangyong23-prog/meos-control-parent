package com.persagy.apccontrol.basic.opt;

import com.persagy.framework.redis.lock.RedisLockUtil;
import com.persagy.framework.redis.lock.RedisLockUtilImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 本地 Redis（{@code switch.opt.localRedis=true}）：边缘盒子上 meos-control 不再依赖 Redis 保存私有数据。
 *
 * <p>与其他 switch.opt.* 不同，这个开关在<b>启动时</b>决定 Bean，修改后需要重启服务。
 * 关闭（默认）时这个配置类整体不生效，Lettuce / Redisson 与改造前完全一致。
 *
 * <p>打开后：
 * <ul>
 *   <li>{@link OptLocalRedisConnectionFactory} 取代 Lettuce 连接工厂（@Primary），RedisTemplate、RedisCacheUtil、
 *       RedisCacheTemplate、{@code @Cacheable} 的 RedisCacheManager 都落到进程内存；</li>
 *   <li>{@link OptLocalLocks} 提供的 RedissonClient 取代 Redisson（@Primary），锁退化为 JVM 内锁；
 *       框架 {@code @RedisLock} 的 RedisLockUtil 也改用它；</li>
 *   <li>{@code opt.local-redis.shared}（默认 true）：与 dtp-meos 共享的键（{@link OptLocalRedisRouter}）仍读写真实 Redis，
 *       连接参数沿用 {@code spring.redis.*}，按需连接；设为 false 时完全不连接 Redis
 *       （仅在 dtp-meos 不再读写这些键之后使用）；</li>
 *   <li>{@code opt.local-redis.shared-prefixes}：覆盖共享前缀，逗号分隔；</li>
 *   <li>{@code opt.local-redis.sweep-seconds}（默认 30）：过期键清理周期。</li>
 * </ul>
 *
 * <p>只允许在边缘服务（spring.application.name 以 -edge 结尾）上打开：云端多副本需要真正的分布式锁和共享缓存。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = OptSwitch.PREFIX + OptSwitch.LOCAL_REDIS, havingValue = "true")
public class OptLocalRedisConfiguration {

    private static final String LOG_TAG = "[opt-local-redis]";

    @Bean
    @Primary
    public OptLocalRedisConnectionFactory optLocalRedisConnectionFactory(Environment env) {
        String app = env.getProperty("spring.application.name", "");
        if (!app.endsWith("-edge")) {
            throw new IllegalStateException(LOG_TAG + " switch.opt.localRedis 只能在边缘服务上打开，当前服务: " + app);
        }
        boolean shared = env.getProperty("opt.local-redis.shared", Boolean.class, true);
        long sweepSeconds = env.getProperty("opt.local-redis.sweep-seconds", Long.class, 30L);
        OptLocalRedisStore store = new OptLocalRedisStore(System::currentTimeMillis);
        if (!shared) {
            log.warn("{} 已启用：全部数据保存在进程内存，不连接 Redis", LOG_TAG);
            return new OptLocalRedisConnectionFactory(store, null, null, sweepSeconds);
        }
        String configured = env.getProperty("opt.local-redis.shared-prefixes");
        List<String> prefixes = StringUtils.hasText(configured)
                ? Arrays.asList(configured.split(","))
                : OptLocalRedisRouter.DEFAULT_SHARED_PREFIXES;
        OptLocalRedisRouter router = new OptLocalRedisRouter(prefixes);
        LettuceConnectionFactory real = realFactory(env);
        log.warn("{} 已启用：私有数据保存在进程内存；共享键 {} 仍读写 Redis {}:{}/{}", LOG_TAG, router.prefixes(),
                real.getHostName(), real.getPort(), real.getDatabase());
        return new OptLocalRedisConnectionFactory(store, router, real, sweepSeconds);
    }

    /** 按 spring.redis.* 建一个只给共享键用的 Lettuce 连接工厂（单机、共享原生连接、不建连接池） */
    private static LettuceConnectionFactory realFactory(Environment env) {
        RedisProperties p = Binder.get(env).bind("spring.redis", RedisProperties.class).orElseGet(RedisProperties::new);
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(p.getHost(), p.getPort());
        standalone.setDatabase(p.getDatabase());
        if (StringUtils.hasText(p.getUsername())) {
            standalone.setUsername(p.getUsername());
        }
        if (StringUtils.hasText(p.getPassword())) {
            standalone.setPassword(RedisPassword.of(p.getPassword()));
        }
        LettuceClientConfiguration.LettuceClientConfigurationBuilder client = LettuceClientConfiguration.builder();
        Duration timeout = p.getTimeout();
        if (timeout != null) {
            client.commandTimeout(timeout);
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, client.build());
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    @Primary
    public OptLocalLocks optLocalLocks() {
        return new OptLocalLocks(System::currentTimeMillis);
    }

    @Bean(destroyMethod = "shutdown")
    @Primary
    public RedissonClient optLocalRedissonClient(OptLocalLocks locks) {
        log.warn("{} 已启用：Redisson 锁替换为进程内锁", LOG_TAG);
        return locks.redissonClient();
    }

    /**
     * 框架 {@code @RedisLock} 注解用的锁工具：框架在 paas.lock.enabled=true 时会自建 Redisson 客户端并在启动时连接 Redis，
     * 这里提供同名类型的 Bean（框架侧是 @ConditionalOnMissingBean）让它改用进程内锁。
     */
    @Bean
    @Primary
    public RedisLockUtil optLocalRedisLockUtil(RedissonClient optLocalRedissonClient) {
        return new RedisLockUtilImpl(optLocalRedissonClient);
    }
}
