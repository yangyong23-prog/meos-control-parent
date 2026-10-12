package com.persagy.apccontrol.basic.service.elevator;

import com.persagy.apccontrol.basic.model.param.elevator.ElevatorStatisticsParam;
import com.persagy.apccontrol.basic.model.vo.elevator.ElevatorSummaryVO;

import java.util.Map;
import java.util.Set;

/**
 * 电梯处理类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/8/21 16:48
 */
public interface ElevatorService {

    /**
     * 统计直梯、扶梯
     *
     * @param param
     * @return
     */
    ElevatorSummaryVO statisticsRunningByClassCode(ElevatorStatisticsParam param) throws Exception;

    /**
     * 获取需要推送孪生数据的对象集合
     * key 为对象类
     * value 为对象实例id集合
     *
     * @param projectId    项目ID
     * @return {@link Map<String,Set<String>>}
     * @author yuecaipu
     * @date 2024/8/24 10:46
     * @since v5.240802
     */
    Map<String, Set<String>> mapClaasCode2objectIds(String projectId);
}
