package com.persagy.apccontrol.basic.service.dashboard;


import com.persagy.apccontrol.basic.model.param.dashboard.DashboardBatchControlQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.DemandSpaceByGroupFloorQueryParam;
import com.persagy.apccontrol.basic.model.vo.dashboard.DashboardBatchControlVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;

import javax.validation.Valid;
import java.util.List;

/**
 * Dashboard服务
 */
public interface DashboardGridChartService {


    /**
     * 查询空间信息
     *
     * @param param 请求参数
     * @return 空间列表
     */
    public List<? extends DtpRwdSpaceVO> querySpace(DemandSpaceByGroupFloorQueryParam param);

    List<? extends DtpRwdSpaceVO> querySpaceComputerRoomSecurity();

    /**
     * 查询设备信息
     *
     * @param param 请求参数
     * @return 空间列表
     */
    public List<? extends DtpRwdEquipmentVO> queryEquipment(DemandSpaceByGroupFloorQueryParam param);

    /**
     * 批量控制
     *
     * @param param 批量控制参数
     * @return 批量控制结果
     */
    List<DashboardBatchControlVO> listBatchControl(@Valid DashboardBatchControlQueryParam param);

}
