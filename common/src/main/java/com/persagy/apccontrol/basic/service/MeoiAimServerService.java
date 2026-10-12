package com.persagy.apccontrol.basic.service;

import com.persagy.apccontrol.basic.model.dto.ObjectClassCodeDTO;
import com.persagy.apccontrol.basic.model.vo.CommonServerForwardVO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.CheckDeviceInfoDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventEquipDTO;

import java.util.List;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/17 14:43
 */
public interface MeoiAimServerService {

    String commonRoute(CommonServerForwardVO forwardVO);

    /**
     * 查询异常事件列表、巡检信息
     *
     * @param groupCode
     * @param projectId
     * @param objectClassCodeDTOS
     * @return
     */
    List<ExceptionEventEquipDTO> queryRiskEventList(String groupCode, String projectId, List<ObjectClassCodeDTO> objectClassCodeDTOS);

    /**
     * 查询iot信息全量入参
     *
     * @param groupCode 集团编码
     * @param projectId 项目id
     * @param infoDTO   查询iot数据信息全量入参
     * @return iothover信息全集
     */
    List<ExceptionEventEquipDTO> queryIotAndRiskEventList(String groupCode, String projectId, CheckDeviceInfoDTO infoDTO);

}