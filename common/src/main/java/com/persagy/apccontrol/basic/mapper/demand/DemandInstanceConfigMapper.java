package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceConfig;
import com.persagy.apccontrol.basic.resource.demand.param.DemandInstanceConfigQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 需求配置实例Mapper
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Repository
@Mapper
public interface DemandInstanceConfigMapper extends BaseMapper<DemandInstanceConfig> {

    /**
     * 查询需求实例配置数量
     *
     * @param projectId 项目ID
     * @param param     查询参数
     * @return 需求实例配置数量
     * @since 7.250301.0
     */
    int count(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") DemandInstanceConfigQueryDaoParam param);

    /**
     * 查询需求实例配置
     *
     * @param projectId   项目ID
     * @param param       查询参数
     * @param withColumns 查询字段, 为空时查询所有
     * @return 需求实例配置
     * @since v6.0.0
     */
    List<DemandInstanceConfig> list(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") DemandInstanceConfigQueryDaoParam param,
            @Nullable @Param("withColumns") String... withColumns);

}