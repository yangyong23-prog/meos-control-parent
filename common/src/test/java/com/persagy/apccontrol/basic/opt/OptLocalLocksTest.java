package com.persagy.apccontrol.basic.opt;

import com.persagy.apccontrol.basic.service.cache.impl.RedissonDistributedLockerServiceImpl;
import com.persagy.framework.redis.lock.LockType;
import com.persagy.framework.redis.lock.RedisLockUtilImpl;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地 Redis（switch.opt.localRedis）的进程内锁：按 Redisson RLock 语义验证。
 */
public class OptLocalLocksTest {

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final OptLocalLocks locks = new OptLocalLocks(now::get);
    private final RedissonClient client = locks.redissonClient();

    private static boolean otherThreadTryLock(RLock lock) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return lock.tryLock(0, 10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }).get(5, TimeUnit.SECONDS);
    }

    @Test
    void reentrantAndExclusiveAcrossThreads() throws Exception {
        RLock lock = client.getLock("apc:lock:a");
        Assertions.assertTrue(lock.tryLock(0, -1, TimeUnit.SECONDS));
        Assertions.assertTrue(lock.tryLock());
        Assertions.assertEquals(2, lock.getHoldCount());
        Assertions.assertTrue(lock.isHeldByCurrentThread());
        Assertions.assertFalse(otherThreadTryLock(client.getLock("apc:lock:a")));
        lock.unlock();
        Assertions.assertTrue(lock.isLocked());
        lock.unlock();
        Assertions.assertFalse(lock.isLocked());
        Assertions.assertTrue(otherThreadTryLock(client.getLock("apc:lock:a")));
    }

    @Test
    void leaseExpiresAndUnlockByNonOwnerFails() throws Exception {
        RLock lock = client.getLock("apc:lock:lease");
        Assertions.assertTrue(lock.tryLock(0, 5, TimeUnit.SECONDS));
        Assertions.assertFalse(otherThreadTryLock(lock));
        now.addAndGet(5_000);
        Assertions.assertFalse(lock.isLocked());
        Assertions.assertTrue(otherThreadTryLock(lock));
        // 锁现在归另一个线程（其租约 10 秒），当前线程 unlock 应报错
        Assertions.assertThrows(IllegalMonitorStateException.class, lock::unlock);
        Assertions.assertFalse(lock.isHeldByCurrentThread());
    }

    @Test
    void waiterAcquiresAfterRelease() throws Exception {
        OptLocalLocks real = new OptLocalLocks(System::currentTimeMillis);
        RLock lock = real.redissonClient().getLock("apc:lock:wait");
        lock.lock();
        CountDownLatch started = new CountDownLatch(1);
        CompletableFuture<Boolean> waiter = CompletableFuture.supplyAsync(() -> {
            started.countDown();
            try {
                return real.redissonClient().getLock("apc:lock:wait").tryLock(5, 1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
        started.await();
        Thread.sleep(50);
        lock.unlock();
        Assertions.assertTrue(waiter.get(5, TimeUnit.SECONDS));
    }

    @Test
    void tryLockTimesOutAndIsInterruptible() throws Exception {
        OptLocalLocks real = new OptLocalLocks(System::currentTimeMillis);
        RLock lock = real.lock("apc:lock:timeout");
        lock.lock();
        long t0 = System.nanoTime();
        Boolean got = CompletableFuture.supplyAsync(() -> {
            try {
                return real.lock("apc:lock:timeout").tryLock(200, -1, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }).get(5, TimeUnit.SECONDS);
        Assertions.assertFalse(got);
        Assertions.assertTrue(System.nanoTime() - t0 >= TimeUnit.MILLISECONDS.toNanos(150));

        AtomicBoolean interrupted = new AtomicBoolean();
        Thread th = new Thread(() -> {
            try {
                real.lock("apc:lock:timeout").tryLock(10, -1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        th.start();
        Thread.sleep(50);
        th.interrupt();
        th.join(5_000);
        Assertions.assertTrue(interrupted.get());
        lock.unlock();
    }

    @Test
    void existingLockerServiceAndFrameworkLockUtilWorkUnchanged() throws Exception {
        RedissonDistributedLockerServiceImpl locker = new RedissonDistributedLockerServiceImpl();
        FieldUtils.writeField(locker, "redissonClient", client, true);
        AtomicBoolean ran = new AtomicBoolean();
        locker.executeWithLock("apc:lock:svc", 1L, () -> ran.set(true));
        Assertions.assertTrue(ran.get());
        Assertions.assertTrue(locker.tryLock("apc:lock:svc2", TimeUnit.SECONDS, 0, 60));
        Assertions.assertTrue(locker.isLocked("apc:lock:svc2"));
        locker.unlock("apc:lock:svc2");
        Assertions.assertFalse(locker.isLocked("apc:lock:svc2"));

        RedisLockUtilImpl util = new RedisLockUtilImpl(client);
        Assertions.assertTrue(util.tryLock("paas:lock:x", LockType.FAIR, 0, 10, TimeUnit.SECONDS));
        util.unLock("paas:lock:x", LockType.FAIR);
        Assertions.assertFalse(client.getLock("paas:lock:x").isLocked());
    }

    @Test
    void unsupportedRedissonObjectsFailLoudly() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> client.getMap("m"));
    }
}
