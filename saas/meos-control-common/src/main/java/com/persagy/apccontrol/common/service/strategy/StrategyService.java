package com.persagy.apccontrol.common.service.strategy;

import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.param.strategy.chiller.ChillerRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.param.strategy.lastfreezepumpgroup.LastFreezePumpGroupRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.ChillerUnitRunningPlanVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.LastFreezePumpGroupRunningPlanVO;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 策略干预服务
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
public interface StrategyService {

    // ------------------------- 冷机策略 -------------------------

    /**
     * 查询冷机运行策略列表
     *
     * @param param 查询参数
     * @return 冷机运行策略列表
     * @since v5.6.0
     */
    RunningStrategyVO<ChillerUnitRunningPlanVO> listChillerRunningStrategy(@NonNull ChillerRunningStrategyQueryParam param);


    // ------------------------- 末级冷冻泵组 -------------------------

    /**
     * 查询末级泵组运行策略列表
     *
     * @param param 查询参数
     * @return 末级泵组运行策略列表
     * @since v5.6.0
     */
    List<RunningStrategyVO<LastFreezePumpGroupRunningPlanVO>> listLastFreezePumpGroupRunningStrategy(
            @NonNull LastFreezePumpGroupRunningStrategyQueryParam param);

    /**
     * 查询冷站运行状态
     *
     * @param acccSystemId 系统id
     * @return 在线且运行
     */
    RunStatusEnum queryColdStationRunningStatus(String acccSystemId);

}
