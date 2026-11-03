package com.persagy.apccontrol.common.opt;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * B2：实时值缓存的 Bean。
 *
 * <p>契约序列化用独立的 ObjectMapper（不受应用全局 Jackson 配置影响，字段名固定）。
 * {@code opt.realtime.stale-seconds}（默认 120）：超过该时长没有新写入的缓存值视为陈旧，必须回源；
 * {@code opt.realtime.negative-seconds}（默认 60）：回源仍无有效值的点的负缓存时长。均为启动时读取。
 */
@Configuration
public class OptRealtimeConfiguration {

    @Bean
    public OptRealtimeCacheCore optRealtimeCacheCore(RedisCacheUtil redisCacheUtil,
                                                     @Value("${opt.realtime.stale-seconds:120}") long staleSeconds,
                                                     @Value("${opt.realtime.negative-seconds:60}") long negativeSeconds) {
        return new OptRealtimeCacheCore(new OptRedisRealtimeStore(redisCacheUtil), new ObjectMapper(),
                System::currentTimeMillis, staleSeconds, negativeSeconds);
    }

    /**
     * B10：实时值回源 Twin 的分片并行线程池（{@code opt.realtime.twin-parallelism}，默认 4）。
     * 独立小池 + CallerRuns：不占用业务公共池，满时由调用线程执行，不会因等待自身池而死锁。TTL 包装以传递上下文。
     */
    @Bean(destroyMethod = "shutdown")
    public OptTwinReadPool optTwinReadPool(@Value("${opt.realtime.twin-parallelism:4}") int parallelism) {
        int n = Math.max(parallelism, 1);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(n, n, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(n * 4),
                r -> {
                    Thread t = new Thread(r, "opt-twin-read");
                    t.setDaemon(true);
                    return t;
                }, new ThreadPoolExecutor.CallerRunsPolicy());
        pool.allowCoreThreadTimeOut(true);
        return new OptTwinReadPool(TtlExecutors.getTtlExecutorService(pool));
    }
}
