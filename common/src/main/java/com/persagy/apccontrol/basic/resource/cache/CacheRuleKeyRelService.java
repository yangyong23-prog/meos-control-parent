package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.model.entity.cache.CacheRuleKeyRel;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 缓存键与规则写入归属关联资源服务。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheRuleKeyRelService {

    /**
     * 查询单个缓存键引用的规则 ID。
     *
     * @param cacheKey 缓存键
     * @return 规则 ID 列表
     * @since v7.260701.0
     */
    @NonNull List<Long> listRuleIdsByCacheKey(@NonNull String cacheKey);

    /**
     * 批量查询缓存键引用的规则 ID。
     *
     * @param cacheKeys 缓存键集合
     * @return 去重后的规则 ID 列表
     * @since v7.260701.0
     */
    @NonNull List<Long> listRuleIdsByCacheKeys(@NonNull Collection<String> cacheKeys);

    /**
     * 原子替换当前项目单个缓存键的规则关联。
     *
     * @param cacheKey  缓存键
     * @param relations 完整目标关联集合；空集合表示清空当前缓存键
     * @since v7.260701.0
     */
    void replaceByCacheKey(
            @NonNull String cacheKey,
            @NonNull Collection<CacheRuleKeyRel> relations);

    /**
     * 批量删除当前项目指定缓存键的全部规则关联。
     *
     * @param cacheKeys 缓存键集合
     * @since v7.260701.0
     */
    void removeByCacheKeys(@NonNull Collection<String> cacheKeys);
}
