package com.persagy.apccontrol.basic.resource;

import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.entity.strategy.StrategyPlanCfg;
import com.persagy.apccontrol.basic.resource.param.strategy.StrategyPlanCfgQueryDaoParam;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 策略计划表服务
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
public interface StrategyPlanCfgService extends IService<StrategyPlanCfg> {

    // ------------------------- 统计相关 -------------------------

    /**
     * 查询数量
     *
     * @param param 查询参数
     * @return 数量
     * @since v5.6.0
     */
    int count(@NonNull @Valid StrategyPlanCfgQueryDaoParam param);

    // ------------------------- 查询相关 -------------------------

    /**
     * 查询实体
     *
     * @param param 查询参数
     * @return 实体
     * @since v5.6.0
     */
    StrategyPlanCfg get(@NonNull StrategyPlanCfgQueryDaoParam param);

    /**
     * 获取临时策略计划
     *
     * @param strategyId       策略ID
     * @param controlFieldName 控制字段
     * @return 临时策略计划
     * @since v5.6.0
     */
    StrategyPlanCfg getTempPlan(@NonNull Long strategyId, @NonNull String controlFieldName);

    /**
     * 查询ID列表
     *
     * @param param 查询参数
     * @return ID列表
     * @since v5.6.0
     */
    List<Long> listIds(@NonNull StrategyPlanCfgQueryDaoParam param);

    /**
     * 查询实体列表
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.6.0
     */
    List<StrategyPlanCfg> list(@NonNull StrategyPlanCfgQueryDaoParam param);

    /**
     * 查询当前时间临时计划
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.6.0
     */
    List<StrategyPlanCfg> listTempPlanAtNow(@NonNull StrategyPlanCfgQueryDaoParam param);

    /**
     * 查询未被同步实体
     *
     * @param param 查询参数
     * @return 实体列表
     * @since v5.7.0
     */
    List<StrategyPlanCfg> listNotSynced(@NonNull StrategyPlanCfgQueryDaoParam param);

}
