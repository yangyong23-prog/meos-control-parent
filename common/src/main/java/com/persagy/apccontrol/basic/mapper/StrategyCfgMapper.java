package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyCfg;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyCfgQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 策略配置表Mapper
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Mapper
public interface StrategyCfgMapper extends BaseMapper<StrategyCfg> {

    // ------------------------- 查询相关 -------------------------

    /**
     * 查询实体
     *
     * @param param 查询参数
     * @return 实体
     * @since v5.6.0
     */
    StrategyCfg get(@NonNull @Param("param") StrategyCfgQueryDaoParam param);

    /**
     * 查询ID列表
     *
     * @param param 查询参数
     * @return ID列表
     * @since v5.6.0
     */
    List<Long> listIds(@NonNull @Param("param") StrategyCfgQueryDaoParam param);

    /**
     * 查询实体列表
     *
     * @param param 查参数
     * @return 实体列表
     * @since v5.6.0
     */
    List<StrategyCfg> list(@NonNull @Param("param") StrategyCfgQueryDaoParam param);

    /**
     * 查询未被同步实体
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.7.0
     */
    List<StrategyCfg> listNotSynced(@NonNull @Param("param") StrategyCfgQueryDaoParam param);

}