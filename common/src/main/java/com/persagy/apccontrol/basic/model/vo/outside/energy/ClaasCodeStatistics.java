
package com.persagy.apccontrol.basic.model.vo.outside.energy;

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
public class ClaasCodeStatistics {

    private String classCode;

    private String classCodeName;

    private List<InfoCodeStatistics> infoCodes;



}