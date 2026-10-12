package com.persagy.apccontrol.basic.model.vo.kitchen;


import lombok.Data;

@Data
public class KitChenRangeHoodEquipStatisticsVo {


    private AlarmControlVo alarmControl;
    /**
     * 运行状态
     */
    private RunStatusVo runStatus;
    private int total;

}
