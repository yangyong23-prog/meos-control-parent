package com.persagy.apccontrol.basic.service.cache.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author zhangyanchang
 * @version 1.0
 * @title: RedissonDistributedLocker
 * @description: redisson 业务接口实现
 * @date: 2021/8/19 15:19
 */
@Component
public class RedissonDistributedLockerServiceImpl
        implements RedissonDistributedLockerService, LogComponent {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public String logTag() {
        return "分布式锁服务";
    }

    @Override
    public RLock lock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        return lock;
    }

    @Override
    public RLock lock(String lockKey, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, TimeUnit.SECONDS);
        return lock;
    }

    @Override
    public RLock lock(String lockKey, TimeUnit unit, int timeout) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(timeout, unit);
        return lock;
    }

    @Override
    public boolean tryLock(String lockKey, TimeUnit unit, int waitTime, int leaseTime) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (Exception e) {
            this.logProjectInstance().warn("尝试获取锁失败",
                    Lists.newArrayList(Pair.of("lockKey", lockKey)));
            return false;
        }
    }

    @Override
    public void executeWithLock(@NonNull String lockKey, long waitSeconds, @NonNull Runnable action) {
        this.executeWithLock(lockKey, waitSeconds, () -> {
            action.run();
            return null;
        });
    }

    @Override
    public <T> T executeWithLock(
            @NonNull String lockKey, long waitSeconds, @NonNull Supplier<T> action) {
        return this.executeWithLock(lockKey, waitSeconds, action, new long[0]);
    }

    @Override
    public <T> T executeWithLock(@NonNull String lockKey, long waitSeconds,
            @NonNull Supplier<T> action, long @NonNull ... retryDelaysMillis) {
        RLock lock = redissonClient.getLock(lockKey);
        for (int attempt = 0; ; attempt++) {
            boolean locked = false;
            boolean interrupted = false;
            try {
                if (lock.isHeldByCurrentThread()) {
                    return action.get();
                }
                locked = lock.tryLock(waitSeconds, TimeUnit.SECONDS);
                if (locked) {
                    return action.get();
                }
                if (attempt >= retryDelaysMillis.length) {
                    this.logProjectInstance().warn("获取分布式锁重试耗尽",
                            Lists.newArrayList(Pair.of("lockKey", lockKey),
                                    Pair.of("attempts", attempt + 1)));
                    throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                            "获取分布式锁超时: " + lockKey);
                }
                this.logProjectInstance().warn("获取分布式锁超时，等待重试",
                        Lists.newArrayList(Pair.of("lockKey", lockKey),
                                Pair.of("attempt", attempt + 1),
                                Pair.of("retryDelayMillis", retryDelaysMillis[attempt])));
                Thread.sleep(retryDelaysMillis[attempt]);
            } catch (InterruptedException e) {
                // 日志调用会消费线程中断标志，这里只记录状态，标志在 finally 中统一恢复。
                interrupted = true;
                this.logProjectInstance().warn("获取分布式锁中断",
                        Lists.newArrayList(Pair.of("lockKey", lockKey)));
                throw new BusinessException(ErrorCodeConstants.SYSTEM_ERROR_B0001.getCode(),
                        "获取分布式锁中断: " + lockKey, e);
            } finally {
                // 中断标志存在时 Redisson 同步命令（含 isHeldByCurrentThread 与 unlock）
                // 可能立即失败，导致持锁线程退出后锁残留（僵尸锁）：必须在任何锁命令前
                // 清除。中断意图不会因此丢失——等锁/睡眠中断由 catch(InterruptedException)
                // 经局部变量记录并在此恢复（日志调用会消费中断标志，不能依赖探测值判断）；
                // 持锁动作异常路径不恢复（意图已随异常传递，且避免污染线程池后续任务）。
                Thread.interrupted();
                try {
                    if (locked && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                } catch (Exception e) {
                    this.logProjectInstance().error("释放锁失败，锁可能残留", e,
                            Lists.newArrayList(Pair.of("lockKey", lockKey)));
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            lock.unlock();
        } catch (Exception e) {
            this.logProjectInstance().warn("释放锁失败",
                    Lists.newArrayList(Pair.of("lockKey", lockKey)));
        }
    }

    @Override
    public void unlock(RLock lock) {
        lock.unlock();
    }

    @Override
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

}
