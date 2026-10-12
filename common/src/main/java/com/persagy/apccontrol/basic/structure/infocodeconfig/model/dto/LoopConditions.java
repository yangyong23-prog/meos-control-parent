package com.persagy.apccontrol.basic.structure.infocodeconfig.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 循环条件用于兼容多种时间类型不同的补齐条件
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/22 19:40
 * @since v5.240802.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoopConditions {

    /**
     * 循环开始值
     */
    private int start;

    /**
     * 循环结束值
     */
    private int end;

    /**
     * 步长
     */
    private int step;

}
