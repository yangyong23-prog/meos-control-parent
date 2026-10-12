package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectRel;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelReplaceScope;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectRelQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 缓存对象关系 Mapper。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface CacheObjectRelMapper extends BaseMapper<CacheObjectRel> {

    /**
     * 按关系 from 对象 ID 查询关系。
     *
     * @param projectId   项目 ID
     * @param param       查询参数
     * @param withColumns 查询字段；为空时查询全部字段
     * @return from 对象命中的关系列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObjectRel> listByFrom(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") CacheObjectRelQueryDaoParam param,
            @Nullable @Param("withColumns") String... withColumns);

    /**
     * 按关系 to 对象 ID 查询关系。
     *
     * @param projectId   项目 ID
     * @param param       查询参数
     * @param withColumns 查询字段；为空时查询全部字段
     * @return to 对象命中的原始方向关系列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObjectRel> listByTo(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") CacheObjectRelQueryDaoParam param,
            @Nullable @Param("withColumns") String... withColumns);

    /**
     * 批量新增缓存对象关系。
     *
     * @param relations 对象关系集合
     * @since v7.260701.0
     */
    void insertBatch(@NonNull @Param("relations") Collection<CacheObjectRel> relations);

    /**
     * 按主体对象和关系类型批量删除正向关系。
     *
     * @param projectId 项目 ID
     * @param objectIds 主体对象 ID
     * @param graphCode 图谱编码
     * @param relCode 关系编码
     * @return 删除行数
     * @since v7.260701.0
     */
    int deleteForward(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("objectIds") Collection<String> objectIds,
            @NonNull @Param("graphCode") String graphCode,
            @NonNull @Param("relCode") String relCode);

    /**
     * 按目标对象和关系类型批量删除反向命中关系。
     *
     * @param projectId 项目 ID
     * @param relObjectIds 目标对象 ID
     * @param graphCode 图谱编码
     * @param relCode 关系编码
     * @return 删除行数
     * @since v7.260701.0
     */
    int deleteReverse(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("relObjectIds") Collection<String> relObjectIds,
            @NonNull @Param("graphCode") String graphCode,
            @NonNull @Param("relCode") String relCode);

    /**
     * 批量删除关系范围。
     *
     * @param projectId 项目 ID
     * @param scopes 关系范围
     * @return 删除行数
     * @since v7.260701.0
     */
    int deleteScopes(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("scopes") Collection<CacheObjectRelReplaceScope> scopes);

}
