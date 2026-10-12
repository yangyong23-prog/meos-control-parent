package com.persagy.apccontrol.service;

import com.persagy.apccontrol.controller.vo.TerminalEquipmentVO;

import java.util.List;

/**
 * 末端端设备服务
 *
 * @author MAF
 * @date 2023/12/12
 */
public interface TerminalEquipmentService {

    /**
     * 获取末端设备统计数
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    List<TerminalEquipmentVO> getTerminalStatistics(String groupCode, String projectId, String objectId);

}
