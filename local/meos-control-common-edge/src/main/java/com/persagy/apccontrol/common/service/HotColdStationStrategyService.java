package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.model.param.strategy.chiller.HotColdStationRunningStrategyQueryParam;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningStrategyVO;
import com.persagy.apccontrol.basic.model.vo.strategy.chiller.HotColdStationUnitRunningPlanVO;

/**
 * @author yuyao
 */
public interface HotColdStationStrategyService {


    /**
     * 查询实时需求运行计划-页面冷热站控制面板
     *
     * @param param
     * @return
     */
    RunningStrategyVO<HotColdStationUnitRunningPlanVO> listChillerRunningStrategy(HotColdStationRunningStrategyQueryParam param);

}
