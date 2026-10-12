package com.persagy.apccontrol.service;

import com.persagy.apccontrol.common.model.param.desktop.DesktopEquipmentDetailsQueryParam;
import com.persagy.apccontrol.common.model.vo.desktop.DesktopEquipmentDetailsVO;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.List;

/**
 * 桌面端服务
 *
 * @author JiYinchuan
 * @since 24Q1 T3S2-5.18.0
 */
public interface DesktopService {

    /**
     * 查询设备详情
     *
     * @param param 查询参数
     * @return 设备详情
     * @since 24Q1 T3S2-5.18.0
     */
    List<DesktopEquipmentDetailsVO> listEquipmentDetails(@NonNull @Valid DesktopEquipmentDetailsQueryParam param);

}
