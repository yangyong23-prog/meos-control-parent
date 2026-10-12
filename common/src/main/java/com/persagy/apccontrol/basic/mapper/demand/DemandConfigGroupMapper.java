package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigGroupRepositoryBatchQueryParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 需求管理-照明需求-配置结果Mapper
 *
 * @author JiYinchuan
 * @since 7.250202.0
 */
@Mapper
@Repository
public interface DemandConfigGroupMapper extends BaseMapper<DemandConfigGroup> {

    /**
     * 查询配置组
     *
     * @param projectId   项目ID
     * @param param       查询参数
     * @param withColumns 查询字段
     * @return 配置组
     * @since 7.250202.0
     */
    List<DemandConfigGroup> list(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") DemandConfigGroupRepositoryBatchQueryParam param,
            @Nullable @Param("withColumns") String... withColumns);

}