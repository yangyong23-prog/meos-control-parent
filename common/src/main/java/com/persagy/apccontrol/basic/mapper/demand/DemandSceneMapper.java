package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandScene;
import com.persagy.apccontrol.basic.resource.demand.param.DemandSceneQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 需求场景Mapper
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Mapper
@Repository
public interface DemandSceneMapper extends BaseMapper<DemandScene> {

    /**
     * 查询需求场景
     *
     * @param projectId   项目ID
     * @param param       查询参数
     * @param withColumns 查询字段
     * @return 需求场景
     * @since 5.241102.0
     */
    List<DemandScene> list(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") DemandSceneQueryDaoParam param,
            @Nullable @Param("withColumns") String... withColumns);

}