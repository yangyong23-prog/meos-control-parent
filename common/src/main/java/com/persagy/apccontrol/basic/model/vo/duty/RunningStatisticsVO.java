package com.persagy.apccontrol.basic.model.vo.duty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 大小冷机运行数量统计
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/11/6 15:47
 * @since v5.241101.0
 */
@Data
public class RunningStatisticsVO {

    @Schema(description = "大机台数")
    private Long chillerLNum;

    @Schema(description = "小机台数")
    private Long chillerSNum;

    @Schema(description = "锅炉运行台数")
    private Long boilerNum;

    @Schema(description = "供热水泵运行台数")
    private Long heatSupplyPumpNum;

    @Schema(description = "锅炉燃烧状态")
    private List<BoilerCombustionStateVO> boilerCombustionStates;

}
