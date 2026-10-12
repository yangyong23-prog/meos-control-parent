package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyDateCfg;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyDateCfgQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 策略日期表Mapper
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Mapper
public interface StrategyDateCfgMapper extends BaseMapper<StrategyDateCfg> {

    // ------------------------- 统计相关 -------------------------

    /**
     * 查询数量
     *
     * @param param 查询参数
     * @return 数量
     * @since v5.6.0
     */
    int count(@NonNull @Param("param") StrategyDateCfgQueryDaoParam param);

    /**
     * 统计已配置的日期
     *
     * @param strategyIds 策略ID
     * @return 日期列表
     * @since v5.6.0
     */
    List<LocalDate> statisticDateInValid(@NonNull @Param("strategyIds") Collection<Long> strategyIds);

    // ------------------------- 查询相关 -------------------------

    /**
     * 查询实体ID列表
     *
     * @param param 查询参数
     * @return 实体ID列表
     * @since v5.7.0
     */
    List<String> listIds(@NonNull @Valid @Param("param") StrategyDateCfgQueryDaoParam param);

    /**
     * 查询实体列表
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.6.0
     */
    List<StrategyDateCfg> list(@NonNull @Param("param") StrategyDateCfgQueryDaoParam param);

    /**
     * 查询未被同步实体
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.7.0
     */
    List<StrategyDateCfg> listNotSynced(@NonNull @Param("param") StrategyDateCfgQueryDaoParam param);

    // ------------------------- 删除相关 -------------------------

    /**
     * 根据策略ID删除
     *
     * @param strategyIds 策略ID
     * @return 删除条数
     * @since v5.6.0
     */
    int delByStrategyId(@NonNull @Param("strategyIds") Collection<Long> strategyIds);

}