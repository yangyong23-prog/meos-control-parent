
package com.persagy.apccontrol.basic.model.vo.outside.energy;

import com.persagy.apccontrol.basic.model.vo.duty.BasicsCardVO;
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
public class ElevatorStatisticsVO extends BasicsCardVO {



    /**整个数据信息点统计总数*/
    private List<InfoCodeAllCount> infoCodeAllCounts;

    private List<ClaasCodeFuncTypeStatistics>  claasCodes;




}