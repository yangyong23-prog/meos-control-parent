package com.persagy.apccontrol.basic.model.vo.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmControlVo {

    /**
     * 自动数量
     */
    private int autoStatus;
    /**
     * 本地数量
     */
    private int localStatus;
    /**
     * 手动的数量
     */
    private int manualStatus;
    /**
     * 离线数量
     */
    private int offLine;
    /**
     * 控反不一致数量
     */
    private int orderFailAlarm;
}
