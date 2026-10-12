package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyDetailResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyItemInfoListResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.EnergyTopItemInfoListResponse;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.EnergyTypeEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/26 11:33
　　 * @description: 查询能耗预算相关数据
*/
public interface EnergyBudgetServerService extends BaseFeignClientService {

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 11:34
    　　 * @description: 能耗分项日数据列表
    */
    List<EnergyDetailResponse> dayEnergyDetail(LocalDateTime startDate, LocalDateTime endDate,EnergyTypeEnum energyType);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/27 18:21
    　　 * @description: 分项跨天数据列表
    */
    List<EnergyDetailResponse> crossDayEnergyDetail(LocalDateTime startDate, LocalDateTime endDate, EnergyTypeEnum energyType);


    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/26 11:34
    　　 * @description: 能耗分项信息列表
    */
    List<EnergyItemInfoListResponse> itemInfoList(EnergyTypeEnum energyType);

    /**
     　　* @author weiyizhong
        * @Company yushu.biz
    　　 * @date 2026/1/27 18:16
    　　 * @description: 获取项目顶级分项名称和id
     */
    EnergyTopItemInfoListResponse queryTopItem(EnergyTypeEnum energyType);
}
