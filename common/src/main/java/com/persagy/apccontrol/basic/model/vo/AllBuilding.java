package com.persagy.apccontrol.basic.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 各个建筑之间的控制目标计算规则
 * @author: yancheng
 * @date: 2023/4/4
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllBuilding {

    /**
     * 各个建筑控制目标计算方式
     */
    private String overAll = "mean";
}
