package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.model.RobotFloor;
import com.persagy.apccontrol.service.RobotService;
import com.persagy.framework.common.entity.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 机器人
 *
 * @author yuecaipu
 * @date 2024/7/24 16:33
 * @since v5.240402.00
 */
@RestController
@RequestMapping("/robot")
public class RobotController {

    @Resource
    RobotService robotService;

    @GetMapping("/listOfFloor")
    public R<List<RobotFloor>> listOfFloor(@RequestParam String projectId) {
        return R.success(robotService.listOfFloor(projectId));
    }

}
