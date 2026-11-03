package com.persagy.apccontrol.basic.config;

import com.alibaba.ttl.TtlRunnable;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.opt.OptRejectedExecutionHandler;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 *
 * @author JiYinchuan
 * @see TaskExecutionProperties
 * @since v5.1.0
 */
@EnableAsync
@Configuration
public class AsyncTaskConfigurer extends AsyncConfigurerSupport {

    private static final String LOG_TAG = "异步任务";

    /**
     * 默认 CallerRuns；opt 分支 B0：switch.opt.asyncDiscardOnFull=true 时改为丢弃并计数
     */
    public static final RejectedExecutionHandler REJECTED_EXECUTION_HANDLER =
            new OptRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    @Autowired
    private TaskExecutionProperties properties;
    @Autowired
    private ObjectProvider<TaskExecutorCustomizer> taskExecutorCustomizers;
    @Autowired
    private ObjectProvider<TaskDecorator> taskDecorator;
    
    @Bean
    public TaskDecorator taskDecorator() {
        return runnable -> {
            String tenant = ContextUtil.getTenant();
            String groupCode = ContextUtil.getGroupCode();
            return Objects.requireNonNull(TtlRunnable.get(() -> {
                ContextUtil.setTenant(tenant);
                ContextUtil.setGroupCode(groupCode);
                try {
                    runnable.run();
                } finally {
                    ContextUtil.remove();
                }
            }));
        };
    }

    @Bean
    public TaskExecutorCustomizer taskExecutorCustomizer() {
        return taskExecutor -> taskExecutor.setRejectedExecutionHandler(REJECTED_EXECUTION_HANDLER);
    }

    @Bean({
            TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME,
            AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME
    })
    @Primary
    @Override
    public Executor getAsyncExecutor() {
        TaskExecutionProperties.Pool pool = properties.getPool();
        TaskExecutorBuilder builder = new TaskExecutorBuilder();
        builder = builder.queueCapacity(pool.getQueueCapacity());
        builder = builder.corePoolSize(pool.getCoreSize());
        builder = builder.maxPoolSize(pool.getMaxSize());
        builder = builder.allowCoreThreadTimeOut(pool.isAllowCoreThreadTimeout());
        builder = builder.keepAlive(pool.getKeepAlive());
        TaskExecutionProperties.Shutdown shutdown = properties.getShutdown();
        builder = builder.awaitTermination(shutdown.isAwaitTermination());
        builder = builder.awaitTerminationPeriod(shutdown.getAwaitTerminationPeriod());
        builder = builder.threadNamePrefix(properties.getThreadNamePrefix());
        builder = builder.customizers(taskExecutorCustomizers.orderedStream()::iterator);
        builder = builder.taskDecorator(taskDecorator.getIfUnique());
        return builder.build();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            Logger logger = LoggerFactory.getLogger(method.getClass());
            LogExecutor.instance().error(logger, LOG_TAG, "异步任务异常", ex, Arrays.asList(
                    Pair.of("method", method.getName()),
                    Pair.of("params", params)
            ));
        };
    }
}
