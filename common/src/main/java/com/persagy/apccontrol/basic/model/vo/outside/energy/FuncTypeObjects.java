
package com.persagy.apccontrol.basic.model.vo.outside.energy;

import lombok.Data;

import java.util.List;

/**
 * @Author 薛涛
 * @Description 
 * @Date 16:09 2024/9/3
 * @Param 
 * @return 
 **/
@Data
public class FuncTypeObjects {

    private List<InfoCodeStatistics> infoCodes;

    private String funcType;

    private String funcTypeName;


}