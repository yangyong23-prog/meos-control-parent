package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.outside.energy.StatisticsTypeEnum;
import com.persagy.apccontrol.basic.model.vo.outside.energy.StatisticsVO;

public interface TerminalStatisticsService extends LogComponent {
    StatisticsVO getTerminalStatistics(StatisticsTypeEnum type);
}
