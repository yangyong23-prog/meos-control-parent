package com.persagy.apccontrol.basic.model.vo.strategy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 信息码数据时间和精度类型
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/02/12 17:20
 * @since 7.250201.0
 */
@Data
public class ChillersNumberVO {

    @Schema(description = "大冷机台数")
    private Integer largeChillersNumber;

    @Schema(description = "小冷机台数")
    private Integer smallChillersNumber;

}
