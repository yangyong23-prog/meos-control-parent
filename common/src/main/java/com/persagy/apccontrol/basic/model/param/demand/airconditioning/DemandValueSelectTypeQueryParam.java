package com.persagy.apccontrol.basic.model.param.demand.airconditioning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 需求管理-价值选择类型查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-价值选择类型查询参数")
public class DemandValueSelectTypeQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

}
