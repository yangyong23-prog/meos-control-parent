package com.persagy.apccontrol.basic.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 控制目标计算规则
 * @author: yancheng
 * @date: 2023/4/4
 * @version: V1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Target {
    /**
     * 不参与控制目标计算的对象
     */
    private List<String> removeObject;
    /**
     * 控制目标计算规则
     */
    private TargetCalRule targetCalRule;
}
