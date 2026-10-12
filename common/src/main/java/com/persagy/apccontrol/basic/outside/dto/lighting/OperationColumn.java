package com.persagy.apccontrol.basic.outside.dto.lighting;

import lombok.Data;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/5/20 17:00
 * @Version 1.0
 */
@Data
public class OperationColumn {
    /** 手自动状态*/
    private RunningModeDTO manualAutoSet;

    /** 设备开关设定 0关闭  1开启*/
    private Object equipSwitchSet;

}
