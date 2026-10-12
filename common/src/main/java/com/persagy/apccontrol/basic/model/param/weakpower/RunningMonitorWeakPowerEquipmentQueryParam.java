package com.persagy.apccontrol.basic.model.param.weakpower;

import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 运行监测-弱电设备查询参数
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RunningMonitorWeakPowerEquipmentQueryParam extends RunningMonitorEquipmentQueryParam {

    /**
     * 系统ID, 为空时查询二级类型下所有系统
     */
    private Set<String> systemIds;

}
