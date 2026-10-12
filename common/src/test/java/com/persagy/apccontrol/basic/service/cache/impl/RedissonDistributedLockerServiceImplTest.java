package com.persagy.apccontrol.basic.service.cache.impl;

import com.persagy.framework.common.exception.BusinessException;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 分布式锁服务 executeWithLock 行为测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class RedissonDistributedLockerServiceImplTest {

    /**
     * 当前线程已持有锁时（外层链路持锁后再次进入）应直接执行动作, 不重复获取锁。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldRunDirectlyWhenLockHeldByCurrentThread() throws IllegalAccessException, InterruptedException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(true);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        AtomicBoolean executed = new AtomicBoolean(false);

        lockerService.executeWithLock("test-lock", 30L, () -> executed.set(true));

        Assertions.assertTrue(executed.get());
        Mockito.verify(lock, Mockito.never()).tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class));
        Mockito.verify(lock, Mockito.never()).unlock();
    }

    /**
     * 等待锁超时时应抛出业务异常且不执行动作。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldThrowWhenLockWaitTimeout() throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(false);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        AtomicBoolean executed = new AtomicBoolean(false);

        Assertions.assertThrows(BusinessException.class,
                () -> lockerService.executeWithLock("test-lock", 30L, () -> executed.set(true)));

        Assertions.assertFalse(executed.get());
        Mockito.verify(lock, Mockito.never()).unlock();
    }

    /**
     * 获取锁成功后应执行动作并在动作返回后释放锁。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldExecuteAndUnlockWhenLockAcquired() throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false, true);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(true);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        AtomicBoolean executed = new AtomicBoolean(false);

        lockerService.executeWithLock("test-lock", 30L, () -> executed.set(true));

        Assertions.assertTrue(executed.get());
        Mockito.verify(lock).tryLock(30L, TimeUnit.SECONDS);
        Mockito.verify(lock).unlock();
    }

    /**
     * 锁等待前两次超时、第三次成功时，动作只能执行一次。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldRetryLockTimeoutAndRunActionOnce() throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false, false, false, true);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                .thenReturn(false, false, true);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        AtomicInteger executions = new AtomicInteger();

        lockerService.executeWithLock("test-lock", 30L, executions::incrementAndGet, 0L, 0L);

        Assertions.assertEquals(1, executions.get());
        Mockito.verify(lock, Mockito.times(3))
                .tryLock(30L, TimeUnit.SECONDS);
        Mockito.verify(lock).unlock();
    }

    /**
     * 三次锁等待均超时时不执行动作。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldNotRunActionWhenLockRetriesExhausted()
            throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                .thenReturn(false);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        AtomicBoolean executed = new AtomicBoolean(false);

        Assertions.assertThrows(BusinessException.class,
                () -> lockerService.executeWithLock(
                        "test-lock", 30L, () -> {
                            executed.set(true);
                            return null;
                        }, 0L, 0L));

        Assertions.assertFalse(executed.get());
        Mockito.verify(lock, Mockito.times(3))
                .tryLock(30L, TimeUnit.SECONDS);
        Mockito.verify(lock, Mockito.never()).unlock();
    }

    /**
     * Redis 获取锁异常必须原样上抛，不能被锁超时重试吞掉。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldNotRetryRedisFailure() throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        IllegalStateException failure = new IllegalStateException("redis unavailable");
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                .thenThrow(failure);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        AtomicBoolean executed = new AtomicBoolean(false);

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class,
                () -> lockerService.executeWithLock(
                        "test-lock", 30L, () -> {
                            executed.set(true);
                            return null;
                        }, 0L, 0L));

        Assertions.assertSame(failure, actual);
        Assertions.assertFalse(executed.get());
        Mockito.verify(lock).tryLock(30L, TimeUnit.SECONDS);
        Mockito.verify(lock, Mockito.never()).unlock();
    }

    /**
     * 获取锁被中断时必须恢复中断标记并立即停止重试。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldRestoreInterruptAndStopRetry()
            throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        InterruptedException interruption = new InterruptedException("interrupted");
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                .thenThrow(interruption);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);

        try {
            BusinessException actual = Assertions.assertThrows(
                    BusinessException.class,
                    () -> lockerService.executeWithLock(
                            "test-lock", 30L, () -> null, 0L, 0L));

            Assertions.assertSame(interruption, actual.getCause());
            Assertions.assertTrue(Thread.currentThread().isInterrupted());
            Mockito.verify(lock).tryLock(30L, TimeUnit.SECONDS);
            Mockito.verify(lock, Mockito.never()).unlock();
        } finally {
            Thread.interrupted();
        }
    }

    /**
     * 锁内动作异常必须原样上抛并释放锁，不能重新执行动作。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldNotRetryActionFailure() throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false, true);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                .thenReturn(true);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        IllegalStateException failure = new IllegalStateException("database failed");
        AtomicInteger executions = new AtomicInteger();

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class,
                () -> lockerService.executeWithLock(
                        "test-lock", 30L, () -> {
                            executions.incrementAndGet();
                            throw failure;
                        }, 0L, 0L));

        Assertions.assertSame(failure, actual);
        Assertions.assertEquals(1, executions.get());
        Mockito.verify(lock).tryLock(30L, TimeUnit.SECONDS);
        Mockito.verify(lock).unlock();
    }

    /**
     * 持锁动作执行中被抢占 interrupt（僵尸锁事故场景）时，释放锁不能被中断标志阻断，
     * 且中断标志应被消费，避免残留污染线程池后续任务。
     *
     * @since v7.260901.0
     */
    @Test
    public void shouldReleaseLockWhenActionInterrupted()
            throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false, true);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                .thenReturn(true);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        IllegalStateException failure = new IllegalStateException("interrupt at sql");
        try {
            IllegalStateException actual = Assertions.assertThrows(
                    IllegalStateException.class,
                    () -> lockerService.executeWithLock(
                            "test-lock", 30L, () -> {
                                // 模拟被外部抢占 interrupt 后 HikariCP 在中断标志下抛 SQLException
                                Thread.currentThread().interrupt();
                                throw failure;
                            }, 0L, 0L));

            Assertions.assertSame(failure, actual);
            // 中断标志必须先清除再释放锁，锁不允许残留
            Mockito.verify(lock).unlock();
            // 动作异常场景中断意图已随异常传递，不残留标志污染后续任务
            Assertions.assertFalse(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    /**
     * 锁内动作直接抛 InterruptedException 时，应释放锁并将中断按“获取锁中断”语义上抛，
     * 中断标志恢复，保证上层能感知抢占。
     *
     * @since v7.260901.0
     */
    @Test
    public void shouldReleaseLockAndRestoreInterruptWhenActionThrowsInterruptedException()
            throws InterruptedException, IllegalAccessException {
        RedissonClient redissonClient = Mockito.mock(RedissonClient.class);
        RLock lock = Mockito.mock(RLock.class);
        Mockito.when(lock.isHeldByCurrentThread()).thenReturn(false, true);
        Mockito.when(lock.tryLock(Mockito.anyLong(), Mockito.any(TimeUnit.class)))
                .thenReturn(true);
        Mockito.when(redissonClient.getLock(Mockito.anyString())).thenReturn(lock);
        RedissonDistributedLockerServiceImpl lockerService =
                new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(lockerService, "redissonClient", redissonClient, true);
        InterruptedException interruption = new InterruptedException("action interrupted");
        try {
            BusinessException actual = Assertions.assertThrows(
                    BusinessException.class,
                    () -> lockerService.executeWithLock(
                            "test-lock", 30L, () -> {
                                // 模拟持锁动作中线程被抢占：可中断点立即抛 InterruptedException
                                Thread.currentThread().interrupt();
                                this.throwAsUnchecked(interruption);
                                return null;
                            }, 0L, 0L));

            Assertions.assertSame(interruption, actual.getCause());
            // 锁必须释放，中断标志必须恢复（等锁/动作中断统一按局部变量恢复）
            Mockito.verify(lock).unlock();
            Assertions.assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    /**
     * 以非受检方式抛出异常，用于在 Supplier 中模拟可中断点抛出的受检异常。
     *
     * @param throwable 需要抛出的异常
     * @since v7.260901.0
     */
    @lombok.SneakyThrows
    private void throwAsUnchecked(Throwable throwable) {
        throw throwable;
    }
}
