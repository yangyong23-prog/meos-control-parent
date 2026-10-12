package com.persagy.apccontrol.basic.resource.cache;

import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectKeyRel;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 缓存键与对象关联资源服务。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public interface CacheObjectKeyRelService {

    /**
     * 按缓存键批量查询当前项目的对象关联。
     *
     * @param cacheKeys 缓存键集合
     * @return 缓存键与对象关联
     * @since v7.260701.0
     */
    @NonNull List<CacheObjectKeyRel> listByCacheKeys(@NonNull Collection<String> cacheKeys);

    /**
     * 为当前项目单个缓存键批量新增对象关联。
     *
     * @param cacheKey  缓存键
     * @param objectIds 对象 ID 集合
     * @since v7.260701.0
     */
    void insertObjectIds(@NonNull String cacheKey, @NonNull Collection<String> objectIds);

    /**
     * 删除当前项目单个缓存键的全部对象关联。
     *
     * @param cacheKey 缓存键
     * @since v7.260701.0
     */
    void removeByCacheKey(@NonNull String cacheKey);

    /**
     * 批量删除当前项目指定缓存键的对象关联。
     *
     * @param cacheKeys 缓存键集合
     * @since v7.260701.0
     */
    void removeByCacheKeys(@NonNull Collection<String> cacheKeys);
}
