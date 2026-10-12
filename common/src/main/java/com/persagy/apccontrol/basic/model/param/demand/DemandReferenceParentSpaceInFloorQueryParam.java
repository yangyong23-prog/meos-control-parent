package com.persagy.apccontrol.basic.model.param.demand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.Set;

/**
 * 需求管理-楼层下引用上级的需求空间查询参数
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "需求管理-楼层下引用上级的需求空间查询参数")
public class DemandReferenceParentSpaceInFloorQueryParam extends BaseDemandTemplateParam {

    @Schema(description = "楼层ID")
    @NotBlank(message = "{key_4a7e14edef4d45f5a3c128903799b601}")
    private String floorId;

    @Schema(description = "查询字段, 为空仅查询默认字段")
    private Set<String> withColumns;

}
