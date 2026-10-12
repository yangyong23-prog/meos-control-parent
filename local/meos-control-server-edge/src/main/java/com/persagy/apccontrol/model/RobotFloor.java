package com.persagy.apccontrol.model;

import lombok.Data;

import java.util.List;

/**
 * 描述
 *
 * @author yuecaipu
 * @date 2024/7/24 16:41
 * @since v5.240402.00
 */
@Data
public class RobotFloor {

    /**
     * 楼层ID
     */
    private String floorId;
    /**
     * 楼层name
     */
    private String floorName;

    private Integer floorSequenceId;

    /**
     * 机器人和搬运箱对象集合
     */
    private List<RobotEquipment> equipmentList;
}
