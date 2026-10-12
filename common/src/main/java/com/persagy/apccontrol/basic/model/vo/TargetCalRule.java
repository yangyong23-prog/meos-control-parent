package com.persagy.apccontrol.basic.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.analysis.function.Floor;

/**
 * @Description: 控制目标计算规则
 * @author: yancheng
 * @date: 2023/4/17
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TargetCalRule {
    /**
     * 各个建筑之间的控制目标计算规则
     */
    private AllBuilding allBuilding;
    /**
     * 同建筑内层与层之间的控制目标计算规则
     */
    private Building building;
    /**
     * 其他楼层的控制目标计算规则
     */
    private Floor floor;
}
