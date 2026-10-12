package com.persagy.apccontrol.basic.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 室内温度计算规则
 * @author: yancheng
 * @date: 2023/4/4
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndoorTempRule {

    /**
     * 控制目标计算规则
     */
    private Target target;
}
