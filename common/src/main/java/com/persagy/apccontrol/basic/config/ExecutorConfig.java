package com.persagy.apccontrol.basic.config;

import com.alibaba.ttl.TtlRunnable;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.persagy.apccontrol.basic.config.properties.ExecutorProperties;
import com.persagy.apccontrol.basic.utils.DailyStringCounter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync(proxyTargetClass = true)
@EnableConfigurationProperties({ExecutorProperties.class})
public class ExecutorConfig {

    private static final String SSE_NAMEPREFIX = "sse-event-publisher-";

    @Autowired
    private ExecutorProperties executorProperties;
    @Autowired
    private Executor executor;

    @Bean("asyncSSEExecutor")
    public ThreadPoolTaskExecutor asyncCurrentExecutor() {
        ExecutorProperties.Config sse = executorProperties.getSse();
        log.info("初始化 asyncSSEExecutor: corePoolSize:{}, maxPoolSize: {}, queueCapacity: {}", sse.getCorePoolSize(), sse.getMaxPoolSize(), sse.getQueueCapacity());
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(sse.getCorePoolSize());
        // 设置最大线程数
        executor.setMaxPoolSize(sse.getMaxPoolSize());
        // 设置队列容量
        executor.setQueueCapacity(sse.getQueueCapacity());
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(sse.getKeepAliveSeconds());
        executor.setAllowCoreThreadTimeOut(true);
        // 设置默认线程名称
        executor.setThreadNamePrefix(SSE_NAMEPREFIX);
        // 设置拒绝策略 丢弃老的数据 避免阻塞
        executor.setRejectedExecutionHandler(new CustomRejectedExecutionHandler());
        executor.setTaskDecorator(runnable -> {
            if (runnable instanceof TtlRunnable) {
                return runnable;
            }
            Runnable decorated = TtlRunnable.get(runnable);
            return Objects.requireNonNull(decorated);
        });
        return executor;
    }

    /**
     * 获取用ttl包装后的执行器
     *
     * @return {@link Executor }
     */
    @Bean("ttlExecutor")
    public Executor getTTlExecutor() {
        return TtlExecutors.getTtlExecutor(executor);
    }

    // 自定义拒绝策略类
    private static class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 调用自定义方法
            handleRejectedTask(r, executor);
            // 执行原有的 DiscardOldestPolicy 逻辑
            new ThreadPoolExecutor.DiscardOldestPolicy().rejectedExecution(r, executor);
        }

        private void handleRejectedTask(Runnable r, ThreadPoolExecutor executor) {
            log.error("sse数据推送,任务被拒绝，当前线程池状态：核心线程数={}，队列大小={}，当前任务：{}", executor.getCorePoolSize(), executor.getQueue().size(), r);
            DailyStringCounter.incrementCount("sse推送丢弃总数");
        }
    }
}
