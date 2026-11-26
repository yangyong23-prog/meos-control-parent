package com.persagy.apccontrol.basic.opt;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 本地 Redis（switch.opt.localRedis）：替代 Lettuce 的 {@link RedisConnectionFactory}。
 *
 * <p>所有连接共用一个 {@link OptLocalRedisStore}；可选地持有一个真实 Redis 连接工厂，只用于与 dtp-meos 共享的键
 * （见 {@link OptLocalRedisRouter}）。真实连接按需建立：没有命令落到共享键时不会连 Redis。
 */
public class OptLocalRedisConnectionFactory implements RedisConnectionFactory, DisposableBean {

    private final OptLocalRedisStore store;
    private final OptLocalRedisRouter router;
    private final RedisConnectionFactory realFactory;
    private final ScheduledExecutorService sweeper;

    /**
     * @param router      共享键路由；null 表示全部本地，不连接任何 Redis
     * @param realFactory 真实 Redis 连接工厂；router 为 null 时忽略
     * @param sweepSeconds 过期键清理周期（秒），&lt;= 0 不启动清理线程（仅测试用）
     */
    public OptLocalRedisConnectionFactory(OptLocalRedisStore store, OptLocalRedisRouter router,
                                          RedisConnectionFactory realFactory, long sweepSeconds) {
        this.store = store;
        this.router = router;
        this.realFactory = router == null ? null : realFactory;
        if (router != null && realFactory == null) {
            throw new IllegalArgumentException("共享键路由需要真实 Redis 连接工厂");
        }
        if (sweepSeconds > 0) {
            this.sweeper = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "opt-local-redis-sweeper");
                t.setDaemon(true);
                return t;
            });
            this.sweeper.scheduleWithFixedDelay(store::sweepExpired, sweepSeconds, sweepSeconds, TimeUnit.SECONDS);
        } else {
            this.sweeper = null;
        }
    }

    public OptLocalRedisStore store() {
        return store;
    }

    public boolean sharedRouting() {
        return router != null;
    }

    @Override
    public RedisConnection getConnection() {
        return OptLocalRedisConnectionHandler.newConnection(store, router, realFactory);
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        throw new InvalidDataAccessApiUsageException("本地 Redis 不支持集群连接");
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return true;
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        throw new InvalidDataAccessApiUsageException("本地 Redis 不支持哨兵连接");
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return realFactory == null ? null : realFactory.translateExceptionIfPossible(ex);
    }

    @Override
    public void destroy() throws Exception {
        if (sweeper != null) {
            sweeper.shutdownNow();
        }
        if (realFactory instanceof DisposableBean) {
            ((DisposableBean) realFactory).destroy();
        }
    }
}
