package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectKeyRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 缓存键与对象关联 Mapper。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface CacheObjectKeyRelMapper extends BaseMapper<CacheObjectKeyRel> {

    /**
     * 按项目和缓存键批量查询对象关联。
     *
     * @param projectId 项目 ID
     * @param cacheKeys 缓存键集合
     * @return 缓存键与对象关联
     * @since v7.260701.0
     */
    @NonNull List<CacheObjectKeyRel> listByCacheKeys(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("cacheKeys") Collection<String> cacheKeys);

    /**
     * 批量新增缓存键与对象关联。
     *
     * @param relations 关联集合
     * @since v7.260701.0
     */
    void insertBatch(@NonNull @Param("relations") Collection<CacheObjectKeyRel> relations);

    /**
     * 删除单个项目缓存键的全部对象关联。
     *
     * @param projectId 项目 ID
     * @param cacheKey  缓存键
     * @return 删除行数
     * @since v7.260701.0
     */
    int deleteByCacheKey(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("cacheKey") String cacheKey);

    /**
     * 批量删除当前项目指定缓存键的对象关联。
     *
     * @param projectId 项目 ID
     * @param cacheKeys 缓存键集合
     * @return 删除行数
     * @since v7.260701.0
     */
    int deleteByCacheKeys(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("cacheKeys") Collection<String> cacheKeys);
}
