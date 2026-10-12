package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigResult;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigResultRepositoryQueryParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * 需求配置预计算结果Mapper
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Mapper
@Repository
public interface DemandConfigResultMapper extends BaseMapper<DemandConfigResult> {

    /**
     * 查询需求配置预计算结果
     *
     * @param projectId   项目ID
     * @param param       查询参数
     * @param withColumns 查询字段
     * @return 需求配置预计算结果
     * @since v7.260701.0
     */
    @NonNull List<DemandConfigResult> list(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") DemandConfigResultRepositoryQueryParam param,
            @Nullable @Param("withColumns") String... withColumns);

    /**
     * 分页查询需求配置预计算结果ID
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @param limit     查询数量上限
     * @return 需求配置预计算结果ID
     * @since v7.260701.0
     */
    @NonNull List<Long> listIds(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") DemandConfigResultRepositoryQueryParam param,
            @Param("limit") int limit);

    /**
     * 批量新增需求配置预计算结果
     *
     * @param results 预计算结果集合
     * @since v7.260701.0
     */
    void insertBatch(@NonNull @Param("results") Collection<DemandConfigResult> results);

}
