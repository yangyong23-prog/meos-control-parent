package com.persagy.apccontrol.basic.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 同建筑内层与层之间的控制目标计算规则
 * @author: yancheng
 * @date: 2023/4/4
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Building {

    /**
     * 其他建筑的控制目标计算规则
     */
    private String otherBuilding = "max";
}
