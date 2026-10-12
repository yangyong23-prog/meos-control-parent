package com.persagy.apccontrol.basic.model.base;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Getter;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.StringJoiner;

/**
 * 缓存锁Key
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface CacheLockKey {

    /**
     * 锁Key统一前缀
     */
    String LOCK_KEY_PREFIX = "LOCK_CACHE_";

    /**
     * DTX发布
     *
     * @author JiYinchuan
     * @since 7.250602.0
     */
    interface DtxPublish {

        /**
         * 照明Key
         *
         * @return 照明Key
         * @since 7.250602.0
         */
        static Key lighting() {
            return new Key(Duration.ofMinutes(10), "lighting", ContextUtil.getProjectId());
        }

        /**
         * 缓存Key。
         *
         * <p>DTX 发布、规则发布和需求实例配置变更等事件触发路径使用的锁，
         * 与初始化任务、调试接口共用的 {@link Trigger#cache()} 锁相互独立：
         * 事件触发不抢占手动初始化任务，手动初始化也不抢占事件触发。</p>
         *
         * @return 缓存Key
         * @since 7.260302.0
         */
        static Key cache() {
            return new Key(Duration.ofMinutes(30), "cache", ContextUtil.getProjectId());
        }

        /**
         * 需求配置结果刷新Key
         *
         * @param projectId 项目ID
         * @return 需求配置结果刷新Key
         * @since v7.260701.0
         */
        static Key demandConfigResult(@NonNull String projectId) {
            return new Key(Duration.ofMinutes(30), "demandConfigResult", projectId);
        }
    }

    /**
     * 缓存触发。
     *
     * <p>初始化缓存、需求配置结果定时任务等缓存触发源共用同一把锁，按项目隔离串行化。
     * xxl-job 初始化任务与调试接口通过 {@code BaseEvent#executeWithLock} 使用本锁，
     * 具备抢占中断语义（新触发到来时中断正在执行的旧触发）；项目 ID 由线程上下文提供。</p>
     *
     * @author JiYinchuan
     * @since v7.260802.0
     */
    interface Trigger {

        /**
         * 缓存触发Key。
         *
         * @return 缓存触发Key
         * @since v7.260802.0
         */
        static Key cache() {
            return new Key(Duration.ofMinutes(30), "trigger", ContextUtil.getProjectId());
        }
    }

    /**
     * 缓存重建
     *
     * @author JiYinchuan
     * @since v7.260802.0
     */
    interface CacheRebuild {

        /**
         * 按运行监测类型的模板缓存写入Key。
         *
         * <p>模板缓存重建（“查询旧键 → 删除 → 插入”）按 {@link RunningMonitorTypeEnum}
         * 粒度串行化，适用于已知运行监测类型的 {@code rebuildByParam} 和 {@code add} 入口。</p>
         * <p>单次锁等待上限为 30 分钟，覆盖百万级对象写入的本地替换耗时；锁成功后的
         * 持有时间不由该值限制，而由 Redisson watchdog 负责续期。远程查询与对象准备
         * 必须在锁外完成（锁内只允许本地替换），重建是每轮轮询任务，等不到锁或并发
         * 变更时放弃本轮，由持锁者或下一轮自愈。</p>
         *
         * @param type 运行监测类型
         * @return 模板缓存写入Key
         * @since v7.260802.0
         */
        static Key byType(@NonNull RunningMonitorTypeEnum type) {
            return new Key(Duration.ofMinutes(30), "cacheRebuild",
                    ContextUtil.getProjectId(), type.name());
        }
    }

    /**
     * 缓存对象模型本地保存。
     *
     * <p>同一项目的共享对象行写入必须使用本锁串行化。锁顺序固定为业务范围锁、
     * 对象模型保存锁、数据库事务。</p>
     *
     * @author JiYinchuan
     * @since v7.260802.0
     */
    interface CacheObjectModelWrite {

        /**
         * 当前项目对象模型保存 Key。
         *
         * <p>单次锁等待上限为 2 分钟，锁成功后的持有时间不由该值限制，而由 Redisson
         * watchdog 负责续期，避免长事务在固定租期到期时失去保护。</p>
         *
         * @return 对象模型保存 Key
         * @since v7.260802.0
         */
        static Key cache() {
            return new Key(Duration.ofMinutes(2), "objectModelWrite",
                    ContextUtil.getProjectId());
        }
    }

    /**
     * 键类
     *
     * @author JiYinchuan
     * @since 7.250602.0
     */
    @Getter
    class Key {

        private final String key;

        private final Duration expire;

        public Key(@NonNull Duration expire, @NonNull String... keys) {
            StringJoiner keyJoiner = new StringJoiner(CacheKeyPrefix.SEPARATOR, LOCK_KEY_PREFIX, "");
            for (String key : keys) {
                keyJoiner.add(key);
            }
            this.key = keyJoiner.toString();
            this.expire = expire;
        }
    }
}
