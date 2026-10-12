package com.persagy.apccontrol.basic.model.calculate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalTime;
import java.util.TreeMap;

/**
 * 价值计算--综合结果
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/7 18:43
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CalculateResultCondition {

    // ------------------------- 价值计算对结果 -------------------
    @Schema(description = "当天价值计算数据依赖--温度、CO2")
    private TreeMap<LocalTime, CalculateResultConditionDetail> conditionDetailTreeMap;

}
