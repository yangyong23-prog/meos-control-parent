
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
public class InfoCodeAllCount {

    private String infoCode;

    private List<DataSourceStatistic> dataSource;


}