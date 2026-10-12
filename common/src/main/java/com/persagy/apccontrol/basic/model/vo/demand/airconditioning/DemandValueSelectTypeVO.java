package com.persagy.apccontrol.basic.model.vo.demand.airconditioning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 需求管理-价值选择类型VO
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "需求管理-价值选择类型VO")
public class DemandValueSelectTypeVO {

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "是否被设置")
    private Boolean set;

}
