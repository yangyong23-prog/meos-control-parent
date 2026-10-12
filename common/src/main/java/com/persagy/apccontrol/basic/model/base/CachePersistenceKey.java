package com.persagy.apccontrol.basic.model.base;

import lombok.Getter;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.StringJoiner;

/**
 * 持久化缓存Key
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
public interface CachePersistenceKey {

    /**
     * 持久化Key统一前缀
     */
    String PERSISTENCE_KEY_PREFIX = "PERSISTENCE_CACHE_";

    /**
     * 需求管理
     *
     * @author JiYinchuan
     * @since v6.0.0
     */
    interface Demand {

        /**
         * 全局规则最后更新时间Key
         *
         * @param projectId 项目ID
         * @return 全局规则最后更新时间Key
         * @since v6.0.0
         */
        static Key globalRuleLastUpdateTime(@NonNull String projectId) {
            return new Key(Duration.ZERO, "demand_global_rule", "last_update_time", projectId);
        }

        /**
         * 照明需求反馈调节下发记录
         *
         * @param projectId 项目ID
         * @return 照明需求反馈调节下发记录
         * @since 7.250802.0
         */
        static Key lightingFeedbackAdjustRecord(@NonNull String projectId) {
            return new Key(Duration.ofMinutes(9), "lighting_feedback_adjust_record", projectId);
        }

        /**
         * 照明需求回路级反馈调节下发记录
         *
         * @param projectId 项目ID
         * @return 照明需求回路级反馈调节下发记录
         * @since 7.260801.0
         */
        static Key lightingCircuitFeedbackAdjustRecord(@NonNull String projectId) {
            return new Key(Duration.ofMinutes(9), "lighting_circuit_feedback_adjust_record", projectId);
        }
    }

    /**
     * 键类
     *
     * @author JiYinchuan
     * @since v6.0.0
     */
    @Getter
    class Key {

        private final String key;

        private final Duration expire;

        public Key(@NonNull Duration expire, @NonNull String... keys) {
            StringJoiner keyJoiner = new StringJoiner(CacheKeyPrefix.SEPARATOR, PERSISTENCE_KEY_PREFIX, "");
            for (String key : keys) {
                keyJoiner.add(key);
            }
            this.key = keyJoiner.toString();
            this.expire = expire;
        }
    }
}
