package com.persagy.apccontrol.basic.service.passengerflow;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.vo.passengerflow.ClientServerHomeStatistics;
import com.persagy.apccontrol.basic.model.vo.passengerflow.PassengerFlowHomeStatistics;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.security.SecurityService;

import java.util.List;

/**
 * 客流
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/21 14:51
 * @since v5.240802.0
 */
public interface PassengerFlowService extends BizComponent<SecurityService> {

    String ACCD_IN_PEOPLE_FLOW_STRING = InfoCodeEnum.ACCD_IN_PEOPLE_FLOW.value();
    String ACCD_OUT_PEOPLE_FLOW_STRING = InfoCodeEnum.ACCD_OUT_PEOPLE_FLOW.value();
    String ACCM_IN_PEOPLE_FLOW_STRING = InfoCodeEnum.ACCM_IN_PEOPLE_FLOW.value();
    String ACCM_OUT_PEOPLE_FLOW_STRING = InfoCodeEnum.ACCM_OUT_PEOPLE_FLOW.value();
    List<String> GATE_GROUP_INFO_CODE_LIST = Lists.newArrayList(ACCD_IN_PEOPLE_FLOW_STRING, ACCD_OUT_PEOPLE_FLOW_STRING);
    List<String> CUMULATIVE_PERSON_TIME_INFO_CODE_LIST = Lists.newArrayList(ACCM_IN_PEOPLE_FLOW_STRING, ACCM_OUT_PEOPLE_FLOW_STRING);


    /**
     * 客流首页上方统计
     *
     * @return 统计后的结果集
     */
    List<PassengerFlowHomeStatistics> getPassagewayHeadCount();

    /**
     * cs首页客流人数统计
     *
     * @return 30内的日维度数据
     */
    ClientServerHomeStatistics homeStatistics();

}
