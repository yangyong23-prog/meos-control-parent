package com.persagy.apccontrol.basic.structure.performance;

import org.jspecify.annotations.NonNull;

/**
 * 计时器上下午
 *
 * @author JiYinchuan
 * @since 7.250302.0
 */
public class StopWatchContext extends StopWatch {

    /**
     * 构造器
     *
     * @param id 标识符
     */
    public StopWatchContext(String id) {
        super((id));
    }

    /**
     * 创建计时任务（秒表）
     *
     * @return StopWatch
     * @since 7.250302.0
     */
    public static StopWatchContext create() {
        return new StopWatchContext("");
    }

    /**
     * 创建计时任务（秒表）
     *
     * @param id 用于标识秒表的唯一ID
     * @return StopWatch
     * @since 7.250302.0
     */
    public static StopWatchContext create(@NonNull String id) {
        return new StopWatchContext(id);
    }

    @Override
    public void start(String taskName) throws IllegalStateException {
        throw new IllegalStateException("Can't start context StopWatch");
    }

    @Override
    public void stop() throws IllegalStateException {
        throw new IllegalStateException("Can't stop context StopWatch");
    }
}