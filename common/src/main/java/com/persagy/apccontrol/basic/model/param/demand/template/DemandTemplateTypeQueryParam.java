package com.persagy.apccontrol.basic.model.param.demand.template;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 需求管理-需求日历模版批量查询参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求管理-需求日历模版类型查询参数")
@NoArgsConstructor
@AllArgsConstructor
public class DemandTemplateTypeQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "需求类型, 为空时查询所有")
    private DemandTypeEnum demandType;

}
