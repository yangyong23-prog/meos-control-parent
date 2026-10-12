package com.persagy.apccontrol.basic.model.vo.duty;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 空间温度
 * @author xiaofengwang
 */
@Data
public class RunAnalysisSpaceVO {

    @Schema(description = "空间id")
    private String spaceId;

    @Schema(description = "空间名")
    private String spaceName;

    @Schema(description = "温度信息点")
    private BigDecimal tdb;
}
