package com.persagy.apccontrol.basic.service;

import com.alibaba.fastjson.JSONArray;
import com.persagy.apccontrol.basic.model.enumtype.terminal.AbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.AbnormalStateParam;
import com.persagy.apccontrol.basic.model.param.dashboard.DashboardQueryParam;
import com.persagy.apccontrol.basic.model.param.dashboard.PeriodDataBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.CurveVO;
import com.persagy.apccontrol.basic.model.vo.PeriodDataBatchVO;

import java.util.List;
import java.util.Map;

/**
 * 室内温湿度服务
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public interface DashBoardService {

    JSONArray getSystemEfficiency(DashboardQueryParam param) throws Exception;

    List<CurveVO> getChillerLoad(DashboardQueryParam param) throws Exception;

    List<CurveVO> getChillingTemp(DashboardQueryParam param) throws Exception;

    List<CurveVO> getFreezingTempDiff(DashboardQueryParam param) throws Exception;

    List<CurveVO> getFreezingTemp(DashboardQueryParam param) throws Exception;

    List<CurveVO> getFreezingSecondary(DashboardQueryParam param) throws Exception;


    /**
     * 批量查询分精度数据
     *
     * @param param
     * @return
     */
    List<PeriodDataBatchVO> batchQueryPeriodData(PeriodDataBatchQueryParam param);

    /**
     * 查询空间异常事件、设备的离线、控返不一致、异常事件
     *
     * @param param 设备id加空间id两个数组
     * @return <对象id,异常状态数组>
     */
    Map<String, List<AbnormalStatusTypeEnum>> listAbnormalState(AbnormalStateParam param);

}
