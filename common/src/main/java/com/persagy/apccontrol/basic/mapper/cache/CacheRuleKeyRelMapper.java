package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheRuleKeyRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 缓存键与规则写入归属关联 Mapper。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface CacheRuleKeyRelMapper extends BaseMapper<CacheRuleKeyRel> {

    /**
     * 查询单个缓存键引用的规则 ID。
     *
     * @param projectId 项目 ID
     * @param cacheKey  缓存键
     * @return 规则 ID 列表
     * @since v7.260701.0
     */
    @NonNull List<Long> listRuleIdsByCacheKey(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("cacheKey") String cacheKey);

    /**
     * 批量查询缓存键引用的规则 ID。
     *
     * @param projectId 项目 ID
     * @param cacheKeys 缓存键集合
     * @return 去重后的规则 ID 列表
     * @since v7.260701.0
     */
    @NonNull List<Long> listRuleIdsByCacheKeys(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("cacheKeys") Collection<String> cacheKeys);

    /**
     * 批量新增规则关联。
     *
     * @param relations 规则关联集合
     * @since v7.260701.0
     */
    void insertBatch(@NonNull @Param("relations") Collection<CacheRuleKeyRel> relations);

    /**
     * 删除当前项目单个缓存键的全部规则关联。
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
     * 批量删除当前项目指定缓存键的全部规则关联。
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
