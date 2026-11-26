package com.persagy.apccontrol.basic.opt;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * 本地 Redis（switch.opt.localRedis）：进程内锁，替代 Redisson 分布式锁。
 *
 * <p>边缘盒子单节点单副本，分布式锁退化为 JVM 内的锁即可。语义按 Redisson RLock 对齐：
 * <ul>
 *   <li>按线程可重入，非持有线程 unlock 抛 {@link IllegalMonitorStateException}；</li>
 *   <li>leaseTime &gt; 0 时到期自动释放（其他线程可直接抢占）；leaseTime &lt;= 0 时一直持有到 unlock
 *       （Redisson 由看门狗续期，效果相同）；</li>
 *   <li>tryLock 等待期间可被中断，抛 {@link InterruptedException}；lock() 不响应中断（与 Redisson 一致）。</li>
 * </ul>
 *
 * <p>{@link #redissonClient()} 返回一个只实现锁相关方法的 RedissonClient 代理，业务里
 * {@code redissonClient.getLock(...)} 的调用点不用改；其他 Redisson 对象（RMap 等）当前代码未使用，调用会抛异常。
 *
 * <p>锁对象按名称常驻内存不回收：锁名都是“业务类型 + 项目”一类的有限集合。
 */
public final class OptLocalLocks {

    private final Map<String, State> states = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public OptLocalLocks(LongSupplier clock) {
        this.clock = clock;
    }

    /** 一把锁的状态，所有字段由 State 自身的监视器保护 */
    private static final class State {
        private long owner = -1;
        private int holds;
        /** 租约到期时间（毫秒），0 表示不限 */
        private long leaseUntil;
    }

    private State state(String name) {
        return states.computeIfAbsent(name, n -> new State());
    }

    /** 当前是否空闲（顺带回收已过期的租约），调用方须持有 s 的监视器 */
    private boolean freeLocked(State s) {
        if (s.owner != -1 && s.leaseUntil > 0 && s.leaseUntil <= clock.getAsLong()) {
            s.owner = -1;
            s.holds = 0;
            s.leaseUntil = 0;
        }
        return s.owner == -1;
    }

    private void acquireLocked(State s, long thread, long leaseMillis) {
        if (s.owner == thread) {
            s.holds++;
        } else {
            s.owner = thread;
            s.holds = 1;
        }
        s.leaseUntil = leaseMillis > 0 ? clock.getAsLong() + leaseMillis : 0;
    }

    /**
     * 尝试加锁。
     *
     * @param waitMillis  最长等待毫秒；&lt; 0 表示一直等
     * @param leaseMillis 租约毫秒；&lt;= 0 表示持有到 unlock
     * @param interruptible 等待时是否响应中断
     */
    boolean tryLock(String name, long waitMillis, long leaseMillis, boolean interruptible) throws InterruptedException {
        State s = state(name);
        long thread = Thread.currentThread().getId();
        long deadline = waitMillis < 0 ? Long.MAX_VALUE : clock.getAsLong() + waitMillis;
        boolean interrupted = false;
        try {
            synchronized (s) {
                while (true) {
                    if (s.owner == thread || freeLocked(s)) {
                        acquireLocked(s, thread, leaseMillis);
                        return true;
                    }
                    long now = clock.getAsLong();
                    if (now >= deadline) {
                        return false;
                    }
                    long sleep = deadline - now;
                    if (s.leaseUntil > 0) {
                        sleep = Math.min(sleep, Math.max(s.leaseUntil - now, 1));
                    }
                    try {
                        s.wait(Math.min(sleep, 1000));
                    } catch (InterruptedException e) {
                        if (interruptible) {
                            throw e;
                        }
                        interrupted = true;
                    }
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void unlock(String name) {
        State s = state(name);
        long thread = Thread.currentThread().getId();
        synchronized (s) {
            freeLocked(s);
            if (s.owner != thread) {
                throw new IllegalMonitorStateException(
                        "attempt to unlock lock, not locked by current thread by node id: local thread-id: "
                                + thread + " lock: " + name);
            }
            if (--s.holds <= 0) {
                s.owner = -1;
                s.holds = 0;
                s.leaseUntil = 0;
                s.notifyAll();
            }
        }
    }

    boolean forceUnlock(String name) {
        State s = state(name);
        synchronized (s) {
            boolean wasLocked = !freeLocked(s);
            s.owner = -1;
            s.holds = 0;
            s.leaseUntil = 0;
            s.notifyAll();
            return wasLocked;
        }
    }

    boolean isLocked(String name) {
        State s = state(name);
        synchronized (s) {
            return !freeLocked(s);
        }
    }

    boolean isHeldByThread(String name, long thread) {
        State s = state(name);
        synchronized (s) {
            return !freeLocked(s) && s.owner == thread;
        }
    }

    int holdCount(String name) {
        State s = state(name);
        synchronized (s) {
            return freeLocked(s) || s.owner != Thread.currentThread().getId() ? 0 : s.holds;
        }
    }

    long remainTimeToLive(String name) {
        State s = state(name);
        synchronized (s) {
            if (freeLocked(s)) {
                return -2;
            }
            return s.leaseUntil == 0 ? -1 : Math.max(s.leaseUntil - clock.getAsLong(), 0);
        }
    }

    // ------------------------------------------------------------------------------------------- Redisson 适配

    /** 返回只实现锁相关方法的 RedissonClient 代理 */
    public RedissonClient redissonClient() {
        return (RedissonClient) Proxy.newProxyInstance(OptLocalLocks.class.getClassLoader(),
                new Class<?>[]{RedissonClient.class}, new ClientHandler());
    }

    /** 名为 name 的 RLock 代理 */
    public RLock lock(String name) {
        return (RLock) Proxy.newProxyInstance(OptLocalLocks.class.getClassLoader(),
                new Class<?>[]{RLock.class}, new LockHandler(name));
    }

    private final class ClientHandler implements InvocationHandler {
        private volatile boolean shutdown;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getLock":
                case "getFairLock":
                case "getSpinLock":
                    return lock((String) args[0]);
                case "shutdown":
                    shutdown = true;
                    return null;
                case "isShutdown":
                case "isShuttingDown":
                    return shutdown;
                case "getId":
                    return "opt-local";
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return "OptLocalRedissonClient";
                default:
                    throw new UnsupportedOperationException(
                            "本地 Redis（switch.opt.localRedis）的 RedissonClient 只支持锁，未实现: " + method.getName());
            }
        }
    }

    private final class LockHandler implements InvocationHandler {
        private final String name;

        private LockHandler(String name) {
            this.name = name;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object[] a = args == null ? new Object[0] : args;
            switch (method.getName()) {
                case "getName":
                    return name;
                case "lock":
                    tryLock(name, -1, a.length == 2 ? millis(a[0], a[1]) : -1, false);
                    return null;
                case "lockInterruptibly":
                    tryLock(name, -1, a.length == 2 ? millis(a[0], a[1]) : -1, true);
                    return null;
                case "tryLock":
                    if (a.length == 0) {
                        return tryLock(name, 0, -1, true);
                    }
                    if (a.length == 2) {
                        return tryLock(name, millis(a[0], a[1]), -1, true);
                    }
                    if (a.length == 3) {
                        return tryLock(name, millis(a[0], a[2]), millis(a[1], a[2]), true);
                    }
                    break;
                case "unlock":
                    unlock(name);
                    return null;
                case "forceUnlock":
                    return forceUnlock(name);
                case "isLocked":
                    return isLocked(name);
                case "isHeldByCurrentThread":
                    return isHeldByThread(name, Thread.currentThread().getId());
                case "isHeldByThread":
                    return isHeldByThread(name, (Long) a[0]);
                case "getHoldCount":
                    return holdCount(name);
                case "remainTimeToLive":
                    return remainTimeToLive(name);
                case "equals":
                    return proxy == a[0];
                case "hashCode":
                    return name.hashCode();
                case "toString":
                    return "OptLocalLock[" + name + "]";
                default:
                    break;
            }
            throw new UnsupportedOperationException(
                    "本地 Redis（switch.opt.localRedis）的锁未实现: " + method.getName() + "/" + a.length);
        }

        private long millis(Object amount, Object unit) {
            long v = ((Number) amount).longValue();
            if (v < 0) {
                return -1;
            }
            return unit == null ? v : ((TimeUnit) unit).toMillis(v);
        }
    }
}
