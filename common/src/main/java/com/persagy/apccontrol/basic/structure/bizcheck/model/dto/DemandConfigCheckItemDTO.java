package com.persagy.apccontrol.basic.structure.bizcheck.model.dto;

import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 空调运行标准检查项DTO
 *
 * @author JiYinchuan
 * @since 7.260301.0
 */
@Data
public class DemandConfigCheckItemDTO {

    @Schema(description = "运行模板检查状态")
    private BizCheckStatusEnum templateStatus;

    @Schema(description = "当日运行的空调模板名称")
    private String templateName;

    @Schema(description = "温度门限值检查状态")
    private BizCheckStatusEnum temperatureThresholdStatus;

}
