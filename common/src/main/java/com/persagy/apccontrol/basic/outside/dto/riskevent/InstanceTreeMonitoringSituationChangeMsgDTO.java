package com.persagy.apccontrol.basic.outside.dto.riskevent;

import com.persagy.apccontrol.basic.outside.enums.MonitoringSituationsEnum;
import lombok.Data;

import java.util.List;

@Data
public class InstanceTreeMonitoringSituationChangeMsgDTO {

    /** 实例树id */
    private String instanceTreeId;
    /** 旧监控情况标识 */
    private List<MonitoringSituationsEnum> oldMonitoringSituations;
    /** 新监控情况标识 */
    private List<MonitoringSituationsEnum> newMonitoringSituations;

    /** 包装一层数据，好方便SSE推送，此字段风险组不推送 */
    private List<InstanceTreeMonitoringSituationChangeMsgDTO> data;
}
