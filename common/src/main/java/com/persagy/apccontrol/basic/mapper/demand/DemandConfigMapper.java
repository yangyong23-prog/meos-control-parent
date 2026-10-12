package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.structure.demand.repository.param.DemandConfigByInstanceRepositoryBatchQueryParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 需求配置Mapper
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Mapper
@Repository
public interface DemandConfigMapper extends BaseMapper<DemandConfig> {

    /**
     * 存在自定义配置实例的数量
     *
     * @param projectId  项目ID
     * @param date       查询日期
     * @param templateId 模版ID
     * @return 存在自定义配置实例的数量
     * @since 5.241201.0
     */
    int countExistsCustomizedInstance(
            @NonNull @Param("projectId") String projectId,
            @Nullable @Param("date") LocalDate date,
            @NonNull @Param("templateId") Long templateId);

    /**
     * 根据实例查询配置
     *
     * @param projectId   项目ID
     * @param param       查询参数
     * @param withColumns 查询字段
     * @return 需求配置
     * @since 5.241102.0
     */
    List<DemandConfig> listByInstances(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") DemandConfigByInstanceRepositoryBatchQueryParam param,
            @Nullable @Param("withColumns") String... withColumns);

}