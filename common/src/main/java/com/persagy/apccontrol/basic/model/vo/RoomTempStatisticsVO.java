package com.persagy.apccontrol.basic.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/10 3:37 PM
 */
@Data
public class RoomTempStatisticsVO {

    @Schema(name = "范围")
    private String range;

    @Schema(name = "数量")
    private Integer count;

}
