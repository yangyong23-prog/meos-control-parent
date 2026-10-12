package com.persagy.apccontrol.basic.service.duty.strategy;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.duty.HomePageParam;
import com.persagy.apccontrol.basic.model.vo.duty.BasicsCardVO;
import org.springframework.lang.NonNull;


/**
 * 值班首页统计
 */
public interface HomePageCardDataHandler<T extends BasicsCardVO> {


    @NonNull
    RunningMonitorTypeEnum getRunningMonitorType();

    /**
     * 执行统计
     */
    T execute(HomePageParam param);
}
