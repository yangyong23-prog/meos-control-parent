package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheTemplateObjectDetail;
import com.persagy.apccontrol.basic.resource.cache.param.CacheTemplateObjectDetailQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 缓存模板对象取值明细 Mapper。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface CacheTemplateObjectDetailMapper extends BaseMapper<CacheTemplateObjectDetail> {

    /**
     * 按当前项目和完整业务键批量查询模板对象取值明细。
     *
     * @param projectId   项目 ID
     * @param param       查询参数
     * @param withColumns 查询字段；为空时查询全部字段
     * @return 模板对象取值明细列表
     * @since v7.260701.0
     */
    @NonNull List<CacheTemplateObjectDetail> listByBusinessKeys(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") CacheTemplateObjectDetailQueryDaoParam param,
            @Nullable @Param("withColumns") String... withColumns);

    /**
     * 批量新增缓存模板对象取值明细。
     *
     * @param details 模板对象取值明细集合
     * @since v7.260701.0
     */
    void insertBatch(@NonNull @Param("details") Collection<CacheTemplateObjectDetail> details);

    /**
     * 删除当前项目单个模板键范围内的全部取值明细。
     *
     * @param projectId  项目 ID
     * @param templateKey 模板键
     * @return 删除行数
     * @since v7.260701.0
     */
    int deleteByTemplateKey(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("templateKey") String templateKey);
}
