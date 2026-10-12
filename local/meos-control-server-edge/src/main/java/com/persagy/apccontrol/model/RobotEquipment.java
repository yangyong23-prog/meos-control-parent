package com.persagy.apccontrol.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机器人和搬运箱对象
 *
 * @author yuecaipu
 * @date 2024/7/24 16:41
 * @since v5.240402.00
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RobotEquipment {

    private String id;

    private String name;

    /**
     * 设备编码，PDPDRB-机器人，PDPDBX-搬运箱
     */
    private String classCode;
    /**
     * 蓝牙信标计算位置
     */
    private String bluetoothSignCalLocation;
    /**
     * 绑定的搬运箱ID
     */
    private String cartId;
    /**
     * 搬运箱装载状态 1-已装载 2-已卸载 99-未知
     */
    private String cartLoadStatus;

    /**
     * 状态，2-运行中（机器人有）
     */
    private String taskStatus;
}
