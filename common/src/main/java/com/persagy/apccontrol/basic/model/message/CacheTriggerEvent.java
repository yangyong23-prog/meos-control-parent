package com.persagy.apccontrol.basic.model.message;

import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 缓存触发事件。
 *
 * <p>供 xxl-job 初始化任务和调试接口使用的轻量 {@link BaseEvent} 实现，
 * 通过 {@link #executeWithLock(LogComponent, CacheLockKey.Key, Supplier)}
 * 实现抢占中断语义。与 {@link BaseEvent} 默认行为不同，本事件在业务操作抛出
 * {@link RuntimeException} 时会在锁执行结束后原样重抛，保证初始化任务失败
 * 能被任务框架正确标记，而不是被静默吞掉。</p>
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class CacheTriggerEvent extends BaseEvent {

    /**
     * 构造缓存触发事件。
     *
     * <p>集团编码和项目 ID 由 {@code com.persagy.framework.common.context.ContextUtil}
     * 线程上下文提供，初始化任务和调试接口在构造前已初始化上下文。</p>
     *
     * @since v7.260802.0
     */
    public CacheTriggerEvent() {
        super("");
    }

    /**
     * 在缓存触发锁内执行动作，业务异常原样重抛。
     *
     * <p>锁内业务操作抛出 {@link RuntimeException} 时，先由 {@link BaseEvent} 记录
     * 抢占中断与执行日志，本方法再在锁释放后原样抛出首个异常，避免 xxl-job 或
     * 调试接口把失败误判为成功。</p>
     *
     * @param <T>          返回值类型
     * @param logComponent 日志组件
     * @param lockKey      锁 Key
     * @param supplier     业务操作
     * @return 业务操作返回值，业务未抛异常且未被抢占中断时为操作结果
     * @since v7.260802.0
     */
    public <T> T executeWithLock(
            LogComponent logComponent,
            CacheLockKey.Key lockKey,
            Supplier<T> supplier) {
        AtomicReference<RuntimeException> failure = new AtomicReference<>();
        T result = super.executeWithLock(logComponent, lockKey, () -> {
            try {
                return supplier.get();
            } catch (RuntimeException exception) {
                failure.compareAndSet(null, exception);
                throw exception;
            }
        });
        RuntimeException exception = failure.get();
        if (exception != null) {
            throw exception;
        }
        return result;
    }

    /**
     * 在缓存触发锁内执行动作（无返回值），业务异常原样重抛。
     *
     * @param logComponent 日志组件
     * @param lockKey      锁 Key
     * @param action       业务操作
     * @since v7.260802.0
     */
    public void executeWithLock(
            LogComponent logComponent,
            CacheLockKey.Key lockKey,
            Runnable action) {
        this.executeWithLock(logComponent, lockKey, () -> {
            action.run();
            return null;
        });
    }
}
