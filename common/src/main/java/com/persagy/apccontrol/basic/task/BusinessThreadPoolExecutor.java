package com.persagy.apccontrol.basic.task;

import com.alibaba.ttl.TtlRunnable;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/12 17:22
 */
public class BusinessThreadPoolExecutor {

    private static ThreadPoolTaskExecutor TTL_EXEXUTOR = null;

    /**
     * 并行查询
     *
     * @param callback 执行回调
     * @return 并行查询结果
     * @param <T> 并行查询结果类型
     * @since 7.250201.0
     */
    public static <T> T parallelExecute(@NonNull Supplier<T> callback) {
        try {
            LocalContextStorage.parallelExecute();
            return callback.get();
        } finally {
            LocalContextStorage.clearParallelExecute();
        }
    }

    public static void executeCommon(Runnable runnable) {
        if (TTL_EXEXUTOR == null) {
            ttlExecutor();
        }
        TTL_EXEXUTOR.execute(runnable);
    }

    public static <T> Future<T> submitCommon(Callable<T> task) {
        if (TTL_EXEXUTOR == null) {
            ttlExecutor();
        }
        return TTL_EXEXUTOR.submit(task);
    }


    public static AsyncTaskExecutor ttlExecutor() {
        if (TTL_EXEXUTOR == null) {
            synchronized (BusinessThreadPoolExecutor.class) {
                if (TTL_EXEXUTOR == null) {

                    TTL_EXEXUTOR = new ThreadPoolTaskExecutor();
                    TTL_EXEXUTOR.setMaxPoolSize(64);
                    TTL_EXEXUTOR.setCorePoolSize(32);
                    TTL_EXEXUTOR.setQueueCapacity(0);
                    TTL_EXEXUTOR.setThreadNamePrefix("ttl-thread-pool-");
                    TTL_EXEXUTOR.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                    TTL_EXEXUTOR.setTaskDecorator(runnable -> {
                        Runnable decorated = TtlRunnable.get(runnable);
                        return Objects.requireNonNull(decorated);
                    });
                    TTL_EXEXUTOR.initialize();
                }
            }
        }

        return TTL_EXEXUTOR;
    }

    public static <T> List<T> getResult(List<CompletableFuture<T>> futures) {
        if (futures == null) {
            return null;
        }
        List<T> resultList = new ArrayList<>();
        for (CompletableFuture<T> future : futures) {
            try {
                T result = future == null ? null : future.get();
                if (result != null) {
                    resultList.add(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }

}
