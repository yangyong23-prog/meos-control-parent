package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheObjectModelView;
import com.persagy.apccontrol.basic.resource.cache.param.CacheObjectModelViewQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 缓存对象模型视图 Mapper。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface CacheObjectModelViewMapper extends BaseMapper<CacheObjectModelView> {

    /**
     * 查询缓存对象模型视图。
     *
     * @param projectId   项目 ID
     * @param param       查询参数
     * @param withColumns 查询字段；为空时查询模型视图转换所需的默认业务字段
     * @return 模型视图列表
     * @since v7.260701.0
     */
    @NonNull List<CacheObjectModelView> list(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") CacheObjectModelViewQueryDaoParam param,
            @Nullable @Param("withColumns") String... withColumns);

    /**
     * 按对象 ID 批量新增或更新模型视图。
     *
     * @param modelViews 模型视图集合
     * @since v7.260701.0
     */
    void upsertBatch(@NonNull @Param("modelViews") Collection<CacheObjectModelView> modelViews);

    /**
     * 删除当前项目指定对象类编码的完整模型视图快照。
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
