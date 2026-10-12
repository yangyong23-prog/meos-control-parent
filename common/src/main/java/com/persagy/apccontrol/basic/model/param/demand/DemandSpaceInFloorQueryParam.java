package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 需求管理-楼层下空间查询参数
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-楼层下空间查询参数")
public class DemandSpaceInFloorQueryParam {

    @Schema(description = "需求模版类型")
    @NotNull(message = "{key_67d92d2b08624257a2b1303cf198a074}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "楼层ID")
    @NotBlank(message = "{key_4a7e14edef4d45f5a3c128903799b601}")
    private String floorId;

    @Schema(description = "查询字段, 为空仅查询默认字段")
    private Set<String> withColumns;

}
