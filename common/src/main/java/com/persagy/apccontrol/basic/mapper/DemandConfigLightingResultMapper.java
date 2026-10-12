package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import com.persagy.apccontrol.basic.resource.demand.param.lighting.scene.DemandConfigLightingResultQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 需求管理-照明需求-配置结果Mapper
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Mapper
@Repository
public interface DemandConfigLightingResultMapper extends BaseMapper<DemandConfigLightingResult> {

    /**
     * 查询照明配置结果
     *
     * @param param 查询参数
     * @return 照明配置结果
     * @since 24Q1 T3S1-5.17.0
     */
    DemandConfigLightingResult get(@NonNull @Param("param") DemandConfigLightingResultQueryDaoParam param);

    /**
     * 查询照明配置结果
     *
     * @param param 查询参数
     * @return 照明配置结果
     * @since 24Q1 T3S1-5.17.0
     */
    List<DemandConfigLightingResult> list(@NonNull @Param("param") DemandConfigLightingResultQueryDaoParam param);

    /**
     * 查询照明配置结果数量
     *
     * @param param 查询参数
     * @return 照明配置结果数量
     * @since 24Q1 T3S1-5.17.0
     */
    int cfgCount(@NonNull @Param("param") DemandConfigLightingResultQueryDaoParam param);
    
    //批量保存
    void batchSave(@NonNull List<DemandConfigLightingResult> param);

}