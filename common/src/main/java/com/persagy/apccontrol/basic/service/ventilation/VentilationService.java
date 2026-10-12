package com.persagy.apccontrol.basic.service.ventilation;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.ventilation.VentilationModelViewParam;
import com.persagy.apccontrol.basic.model.vo.ventilation.VentilationModelViewVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;

import java.util.*;

public interface VentilationService extends BizComponent<VentilationService> {

    /**
     * 所有设备类别代码
     */
    Collection<DtpRwdClassCodeEnum> ALL_EQUIPMENT_CLASS_CODES = Arrays.asList(
            DtpRwdClassCodeEnum.FFSCAF,
            DtpRwdClassCodeEnum.ACVTSF,
            DtpRwdClassCodeEnum.FFSCMF,
            DtpRwdClassCodeEnum.CFCSWN,
            DtpRwdClassCodeEnum.CFCSEW
    );

    Collection<RunningMonitorSecondTypeEnum> ALL_VENTILATION_TYPES = Arrays.asList(
            RunningMonitorSecondTypeEnum.DOME_VENTILATION,
            RunningMonitorSecondTypeEnum.LOGISTICS_VENTILATION,
            RunningMonitorSecondTypeEnum.TOILET_VENTILATION,
            RunningMonitorSecondTypeEnum.PARKING_VENTILATION
    );

    /**
     * 查询通风模型视图
     *
     * @param param param
     * @return {@link VentilationModelViewVO }
     */
    VentilationModelViewVO queryVentilationModelView(VentilationModelViewParam param);


    Map<String, Set<String>> mapClaasCode2objectIds(String projectId, String instanceId, DemandInstanceTypeEnum instanceType);

    /**
     * 通风异常事件空间叠加二级风控目标
     *
     * @param param 通风入参
     * @return 异常事件列表
     */
    List<ExceptionEventBaseInfoVO> getAbnormalDataList(VentilationModelViewParam param);
    
    
}
