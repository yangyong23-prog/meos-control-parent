
package com.persagy.apccontrol.basic.model.vo.outside.energy;

import com.persagy.apccontrol.basic.model.vo.duty.BasicsCardVO;
import lombok.Data;

import java.util.List;

/**
 * @Author 薛涛
 * @Description 
 * @Date 15:58 2024/9/3
 * @Param 
 * @return 
 **/
@Data
public class SubstationStatistics extends BasicsCardVO {



    /**离线*/
    private Integer offlineCount = 0;

    /**异常事件*/
    private Integer exceptionEventCount = 0;

    private List<SubstationObjectRwdStatistics>  objects;

}