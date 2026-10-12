package com.persagy.apccontrol.basic.service.weakpower;

import com.persagy.apccontrol.basic.model.param.weakpower.WeakPowerTreeEquipmentsQueryParam;
import com.persagy.apccontrol.basic.model.vo.weakpower.WeakPowerSystemVO;
import com.persagy.apccontrol.basic.model.vo.weakpower.WeakPowerTreeEquipmentsVO;

import java.util.List;

/**
 * 弱电服务
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
public interface WeakPowerService {

    /**
     * 查询弱电设备树
     *
     * @param param 查询参数
     * @return 对象树
     */
    WeakPowerTreeEquipmentsVO treeEquipments(WeakPowerTreeEquipmentsQueryParam param);


    /**
     * 查询系统列表
     *
     * @return 系统列表
     * @since 7.250301.0
     */
    List<WeakPowerSystemVO> listSystems();

}
