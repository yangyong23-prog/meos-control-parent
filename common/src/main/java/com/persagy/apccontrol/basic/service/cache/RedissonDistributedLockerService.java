package com.persagy.apccontrol.basic.service.cache;

import org.jspecify.annotations.NonNull;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author zhangyanchang
 * @version 1.0
 * @description: redisson 业务接口
 * @date 2021/8/19 15:28
 */
public interface RedissonDistributedLockerService {

    RLock lock(String lockKey);

    RLock lock(String lockKey, int timeout);

    RLock lock(String lockKey, TimeUnit unit, int timeout);

    boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime);

    /**
     * 在指定锁内执行动作。
     *
     * <p>当前线程已持有该锁时直接执行动作（避免锁中套锁）；等待锁超时抛业务异常明确失败；
     * 锁在动作返回后释放，晚于动作内事务提交，保证并发写入串行化。</p>
     *
     * @param lockKey     锁 key
     * @param waitSeconds 等待获取锁的秒数
     * @param action      锁内动作
     * @since v7.260802.0
     */
    void executeWithLock(@NonNull String lockKey, long waitSeconds, @NonNull Runnable action);

    /**
     * 在指定锁内执行动作并返回动作结果。
     *
     * <p>等待、重入与释放语义与 {@link #executeWithLock(String, long, Runnable)} 一致，
     * 用于锁内动作需要向调用方返回结果（如替换是否提交成功）的场景。</p>
     *
     * @param lockKey     锁 key
     * @param waitSeconds 等待获取锁的秒数
     * @param action      锁内动作
     * @param <T>         动作结果类型
     * @return 锁内动作结果
     * @since v7.260802.0
     */
    <T> T executeWithLock(
            @NonNull String lockKey, long waitSeconds, @NonNull Supplier<T> action);

    /**
     * 在指定锁内执行动作；仅锁获取超时按给定退避时间重试，动作本身只执行一次。
     *
     * @param lockKey 锁 key
     * @param waitSeconds 单次等待秒数
     * @param action 锁内动作
     * @param retryDelaysMillis 超时后的退避毫秒数，长度决定重试次数
     * @param <T> 动作结果类型
     * @return 动作结果
     * @since v7.260802.0
     */
    <T> T executeWithLock(@NonNull String lockKey, long waitSeconds,
            @NonNull Supplier<T> action, long @NonNull ... retryDelaysMillis);

    void unlock(String lockKey);

    void unlock(RLock lock);

    /**
     * 检查锁是否被锁定
     *
     * @param lockKey 锁 key
     * @return true-已锁定，false-未锁定
     */
    boolean isLocked(String lockKey);
}
