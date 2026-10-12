package com.persagy.apccontrol.service;

import com.persagy.apccontrol.model.RobotFloor;

import java.util.List;

/**
 * 机器人
 *
 * @author yuecaipu
 * @date 2024/7/24 16:35
 * @since v5.240402.00
 */
public interface RobotService {
    List<RobotFloor> listOfFloor(String projectId);
}
