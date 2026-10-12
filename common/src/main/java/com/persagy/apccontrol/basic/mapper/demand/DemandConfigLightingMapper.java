package com.persagy.apccontrol.basic.mapper.demand;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.DemandConfigLightingQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 需求管理-照明需求-配置Mapper
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Mapper
@Repository
public interface DemandConfigLightingMapper extends BaseMapper<DemandConfigLighting> {

    /**
     * 查询照明需求配置
     *
     * @param param 查询参数
     * @return 照明需求配置
     * @since 24Q1 T3S1-5.17.0
     */
    DemandConfigLighting get(@NonNull @Param("param") DemandConfigLightingQueryDaoParam param);

    /**
     * 查询照明需求配置
     *
     * @param param 查询参数
     * @return 照明需求配置
     * @since 24Q1 T3S1-5.17.0
     */
    List<DemandConfigLighting> list(@NonNull @Param("param") DemandConfigLightingQueryDaoParam param);

}