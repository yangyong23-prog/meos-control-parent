package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateQueryDaoParam;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateUpdateDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 需求模版Mapper
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Mapper
@Repository
public interface DemandTemplateMapper extends BaseMapper<DemandTemplate> {

    /**
     * 查询需求模版
     *
     * @param projectId   项目ID
     * @param param       查询参数
     * @param withColumns 查询字段
     * @return 需求模版
     * @since v6.0.0
     */
    List<DemandTemplate> list(
            @NonNull @Param("projectId") String projectId,
            @NonNull @Param("param") DemandTemplateQueryDaoParam param,
            @Nullable @Param("withColumns") String... withColumns);

    /**
     * 更新需求模版
     *
     * @param param 更新参数
     * @return 影响行数
     * @since 5.240402.0
     */
    int edit(@NonNull @Param("param") DemandTemplateUpdateDaoParam param);

}