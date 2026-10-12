package com.persagy.apccontrol.service;

import com.persagy.apccontrol.controller.strategy.param.ChillerFixedStrategyQueryParam;
import com.persagy.apccontrol.controller.strategy.vo.ChillerStrategyPlanVO;
import org.springframework.lang.NonNull;

import java.util.List;

public interface SaasStrategyService {

    /**
     * 查询冷机固定时间点策略
     *
     * @param param 查询参数
     * @return 冷机固定时间点策略
     */
    List<ChillerStrategyPlanVO> queryByParam(@NonNull ChillerFixedStrategyQueryParam param);

}
