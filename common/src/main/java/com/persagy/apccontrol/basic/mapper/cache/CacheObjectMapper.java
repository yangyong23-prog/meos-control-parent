package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObject;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 缓存对象 Mapper。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface CacheObjectMapper extends BaseMapper<CacheObject> {

    /**
     * 查询缓存对象。
     *
     * @param projectId   项目 ID
     * @param param       查询参数
     * @param withColumns 查询字段；为空时查询全部字段
     * @return 缓存对象列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObject> list(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") CacheObjectQueryDaoParam param,
            @Nullable @Param("withColumns") String... withColumns);

    /**
     * 按对象 ID 批量新增或更新缓存对象。
     *
     * @param objects 缓存对象集合
     * @since v7.260701.0
     */
    void upsertBatch(@NonNull @Param("objects") Collection<CacheObject> objects);

    /**
     * 删除当前项目指定对象类编码的完整快照。
     *
     * @param projectId 项目 ID
     * @param classCodes 对象类编码集合
     * @return 删除行数
     * @since v7.260802.0
     */
    int deleteByClassCodes(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("classCodes") Collection<String> classCodes);

}
