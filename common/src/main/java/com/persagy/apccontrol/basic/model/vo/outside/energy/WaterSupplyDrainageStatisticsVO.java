
package com.persagy.apccontrol.basic.model.vo.outside.energy;

import lombok.Data;

import java.util.List;

/**
 * @Author 薛涛
 * @Description
 * @Date 15:59 2024/9/3
 * @Param 
 * @return 
 **/
@Data
public class WaterSupplyDrainageStatisticsVO {



    /*** 离线数量*/
    private Integer  offlineCount;
    /*** （高等级的）异常事件数量*/
    private Integer  exceptionEventCount;
    /*** 控反不一致数量*/
    private Integer  inconsistentControlCount;

    private List<ClaasCodeStatistics>  claasCodes;




}