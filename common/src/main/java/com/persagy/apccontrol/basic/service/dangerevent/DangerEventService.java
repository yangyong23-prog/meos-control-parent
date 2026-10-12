package com.persagy.apccontrol.basic.service.dangerevent;

import com.persagy.apccontrol.basic.model.param.dangerevent.QueryByObjectParam;
import com.persagy.apccontrol.basic.model.vo.dangerevent.DangerEventVO;

import javax.validation.Valid;
import java.util.List;

/**
 * @author wxf
 * @description 风险事件服务
 * @date 19:04 2025/2/12
**/
public interface DangerEventService {

    /**
     * @author wxf
     * @description 查询设备关联的风险事件
     * @date 11:31 2025/2/13
     * @param objectIds
     * @return java.util.List<com.persagy.apccontrol.basic.model.vo.dangerevent.DangerEventObjectVO>
    **/
    List<DangerEventVO> listByObject(List<String> objectIds);

    /**
     * @author wxf
     * @description 查询设备风险事件
     * @date 16:47 2025/2/17
     * @param dangerEventIds
     * @return java.util.List<com.persagy.apccontrol.basic.model.vo.dangerevent.DangerEventVO>
    **/
    List<DangerEventVO> listByDangerEvent(List<String> dangerEventIds);

    /**
     * @author wxf
     * @description 全量拉取风险事件
     * @date 15:21 2025/2/14
    **/
    void pullAllDangerEvent();


    /**
     * 查询风险事件对象
     *
     * @param param 参数
     * @return {@code List<DangerEventVO> }
     * @since 7.250902.0
     */
    List<DangerEventVO> listByDangerObject(@Valid QueryByObjectParam param);
}
