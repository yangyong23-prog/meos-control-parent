package com.persagy.apccontrol.basic.structure.cacherwd.repository;

import com.persagy.apccontrol.basic.model.base.domain.Repository;
import com.persagy.apccontrol.basic.model.entity.cache.CacheIotRule;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 缓存-IOT规则仓储
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
public interface CacheIotRuleRepository extends Repository {

    /**
     * 当前部署端读取的规则类型。
     *
     * <p>云端返回 {@code PERSISTENCE}，边缘端返回 {@code TWIN}；读取新规则主表时沿用该语义，
     * 不在共享消费者中猜测部署端。</p>
     *
     * @return 当前部署端规则类型
     * @since v7.260701.0
     */
    @NonNull CacheIotRuleTypeEnum cacheIotRuleType();

    /**
     * 根据key查询对象ID和存在规则的信息点
     *
     * @param key 自定义Key
     * @return 对象ID -> 存在规则的信息点编码集合
     */
    Map<String, Set<String>> mapObjectIdAndInfoCodes(@NonNull String key, @Nullable Set<String> objectIds);

    /**
     * 添加缓存
     *
     * @param cacheIotRules 缓存列表
     * @since 7.260302.0
     */
    void add(@NonNull Collection<CacheIotRule> cacheIotRules);

    /**
     * 根据keys删除缓存
     *
     * @param keys 自定义Key集合
     * @since 7.260302.0
     */
    void removeByKeys(@NonNull Collection<String> keys);

}
