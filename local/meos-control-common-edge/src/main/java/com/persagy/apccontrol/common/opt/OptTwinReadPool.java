package com.persagy.apccontrol.common.opt;

import java.util.concurrent.ExecutorService;

/**
 * B10：实时值回源 Twin 分片并行用的线程池持有者。
 * 用独立类型注册为 Bean，避免新增 Executor/ExecutorService 类型的 Bean 影响现有按类型注入。
 */
public final class OptTwinReadPool {

    private final ExecutorService executor;

    public OptTwinReadPool(ExecutorService executor) {
        this.executor = executor;
    }

    public ExecutorService executor() {
        return executor;
    }

    public void shutdown() {
        executor.shutdown();
    }
}
