package com.persagy.apccontrol.common.opt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * B0：@Scheduled 调度线程池。
 *
 * <p>现状所有 @Scheduled 任务（InnerSchdule、照明 5 分钟任务、SSE 1 分钟 cron 等）共用 1 个线程串行执行，
 * 一个任务卡住会拖停其它任务。
 *
 * <p>配置 {@code opt.scheduling.pool-size}（启动时读取，需重启生效）：未配置或 ≤1 时不做任何事，
 * 保持现有调度器；&gt;1 时使用该大小的独立调度线程池。Nacos 建议值 4。
 */
@Slf4j
@Configuration
public class OptSchedulingConfigurer implements SchedulingConfigurer, DisposableBean {

    @Value("${opt.scheduling.pool-size:1}")
    private int poolSize;

    private ThreadPoolTaskScheduler scheduler;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        if (poolSize <= 1) {
            return;
        }
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix("opt-scheduling-");
        scheduler.setErrorHandler(t -> log.error("[opt] 定时任务异常", t));
        scheduler.setWaitForTasksToCompleteOnShutdown(false);
        scheduler.initialize();
        registrar.setTaskScheduler(scheduler);
        log.info("[opt] @Scheduled 使用独立调度线程池，大小 {}", poolSize);
    }

    @Override
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
