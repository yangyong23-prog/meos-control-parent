package com.persagy.apccontrol.basic.model.message;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.utils.context.ContextUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基础事件
 *
 * <p>提供统一的分布式锁管理和线程中断处理机制，所有需要分布式锁保护的事件均应继承此类。</p>
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
public abstract class BaseEvent extends ApplicationEvent {

    /**
     * 执行线程映射
     *
     * <p>Key格式: {lockKey}，用于区分不同监听器的执行线程</p>
     */
    private static final Map<String, Thread> EXECUTE_THREAD_MAP = new ConcurrentHashMap<>();

    /**
     * Redisson客户端
     */
    private static final RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);

    /**
     * 构造器
     *
     * @param source 事件源对象
     * @since 7.260302.0
     */
    protected BaseEvent(Object source) {
        super(source);
    }

    /**
     * 获取集团编码
     *
     * @return 集团编码
     * @since 7.260302.0
     */
    public String getGroupCode() {
        return ContextUtil.getGroupCode();
    }

    /**
     * 获取项目ID
     *
     * @return 项目ID
     * @since 7.260302.0
     */
    public String getProjectId() {
        return ContextUtil.getProjectId();
    }

    /**
     * 执行变更操作
     *
     * <p>提供统一的分布式锁管理和线程中断处理机制，确保同一项目的同一类型操作在同一时刻只有一个线程在执行。</p>
     *
     * <h3>执行流程：</h3>
     * <ol>
     *   <li>初始化上下文（集团编码、项目ID）</li>
     *   <li>检查是否有同类型线程正在执行，若有则中断该线程</li>
     *   <li>尝试获取分布式锁（等待时间与锁过期时间相同）</li>
     *   <li>获取锁成功后检查中断状态，若已中断则放弃执行</li>
     *   <li>执行业务操作</li>
     *   <li>释放锁并清理线程映射</li>
     * </ol>
     *
     * <h3>线程安全性：</h3>
     * <ul>
     *   <li>使用{@link ConcurrentHashMap}存储执行线程映射，保证并发安全</li>
     *   <li>使用Redisson分布式锁，保证跨实例的互斥访问</li>
     *   <li>不同监听器使用不同的锁Key，互不影响</li>
     * </ul>
     *
     * @param <T>          返回值类型
     * @param logComponent 日志组件，用于记录执行过程中的日志
     * @param lockKey      分布式锁Key，包含锁的key和过期时间
     * @param action       要执行的业务操作
     * @return 业务操作返回值，执行失败或被中断时返回null
     * @since 7.260302.0
     */
    public <T> T executeWithLock(
            LogComponent logComponent,
            CacheLockKey.Key lockKey,
            Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey.getKey());
        T result = null;
        String threadMapKey = null;
        try {
            threadMapKey = lockKey.getKey();
            ContextUtils.initGroupCodeProjectId(this.getGroupCode(), this.getProjectId());
            // 有线程正在执行时进行标记中断
            Thread executedThread = EXECUTE_THREAD_MAP.get(threadMapKey);
            if (Objects.nonNull(executedThread)) {
                logComponent.logProjectInstance().warn("当前已存在正在执行的线程", Arrays.asList(
                        Pair.of("threadId", executedThread.getId()),
                        Pair.of("threadMapKey", threadMapKey),
                        Pair.of("event", this)
                ));
                executedThread.interrupt();
            }
            if (lock.tryLock(lockKey.getExpire().getSeconds(), lockKey.getExpire().getSeconds(), TimeUnit.SECONDS)) {
                if (Thread.currentThread().isInterrupted()) {
                    logComponent.logProjectInstance().warn("获取锁成功但线程已被中断，放弃执行",
                            Pair.of("event", this));
                    Thread.currentThread().interrupt();
                    lock.unlock();
                    return null;
                }
                logComponent.logProjectInstance().info("获取锁成功, 开始执行",
                        Pair.of("event", this));
                EXECUTE_THREAD_MAP.put(threadMapKey, Thread.currentThread());
                result = action.get();
            } else {
                logComponent.logProjectInstance().error("获取锁失败", Arrays.asList(
                        Pair.of("event", this),
                        Pair.of("executedThreadId", Objects.nonNull(executedThread)
                                ? executedThread.getId() : null)
                ));
            }
        } catch (InterruptedException e) {
            logComponent.logProjectInstance().warn("获取锁中断", e, Pair.of("event", this));
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logComponent.logProjectInstance().error("执行失败", e, Arrays.asList(
                    Pair.of("event", this),
                    Pair.of("errorMessage", e.getLocalizedMessage())
            ));
        } finally {
            if (Objects.nonNull(threadMapKey)) {
                EXECUTE_THREAD_MAP.remove(threadMapKey);
            }
            if (Thread.interrupted()) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
                Thread.currentThread().interrupt();
            } else if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return result;
    }

    /**
     * 执行变更操作（无返回值）
     *
     * <p>功能与{@link #executeWithLock(LogComponent, CacheLockKey.Key, Supplier)}相同，
     * 区别在于业务操作无返回值。</p>
     *
     * @param logComponent 日志组件，用于记录执行过程中的日志
     * @param lockKey      分布式锁Key，包含锁的key和过期时间
     * @param action       要执行的业务操作，无返回值
     * @see #executeWithLock(LogComponent, CacheLockKey.Key, Supplier)
     * @since 7.260302.0
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
