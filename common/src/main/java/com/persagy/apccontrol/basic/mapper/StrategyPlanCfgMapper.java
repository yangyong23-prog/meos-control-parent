package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyPlanCfg;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyPlanCfgQueryDaoParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 策略计划表Mapper
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Mapper
public interface StrategyPlanCfgMapper extends BaseMapper<StrategyPlanCfg> {

    // ------------------------- 统计相关 -------------------------

    /**
     * 查询数量
     *
     * @param param 查询参数
     * @return 数量
     * @since v5.6.0
     */
    int count(@NonNull @Valid @Param("param") StrategyPlanCfgQueryDaoParam param);

    // ------------------------- 查询相关 -------------------------

    /**
     * 查询实体
     *
     * @param param 查询参数
     * @return 实体
     * @since v5.6.0
     */
    StrategyPlanCfg get(@NonNull @Param("param") StrategyPlanCfgQueryDaoParam param);

    /**
     * 获取临时策略计划
     *
     * @param strategyId       策略ID
     * @param controlFieldName 控制字段
     * @return 临时策略计划
     * @since v5.6.0
     */
    StrategyPlanCfg getTempPlan(
            @NonNull @Param("strategyId") Long strategyId,
            @NonNull @Param("controlFieldName") String controlFieldName);

    /**
     * 查询ID列表
     *
     * @param param 查询参数
     * @return ID列表
     * @since v5.6.0
     */
    List<Long> listIds(@NonNull @Param("param") StrategyPlanCfgQueryDaoParam param);

    /**
     * 查询实体列表
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.6.0
     */
    List<StrategyPlanCfg> list(@NonNull @Param("param") StrategyPlanCfgQueryDaoParam param);

    /**
     * 查询当前时间临时计划
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.6.0
     */
    List<StrategyPlanCfg> listTempPlanAtNow(@NonNull @Param("param") StrategyPlanCfgQueryDaoParam param);

    /**
     * 查询未被同步实体
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.7.0
     */
    List<StrategyPlanCfg> listNotSynced(@NonNull @Param("param") StrategyPlanCfgQueryDaoParam param);

}