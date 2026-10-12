package com.persagy.apccontrol.basic.model.param.demand.openingcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * 需求管理-营业日历全局规则查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Accessors(chain = true)
@Schema(title = "需求管理-营业日历全局规则查询参数")
public class DemandGlobalRuleQueryParam {

    @Schema(description = "实例ID")
    @NotBlank(message = "{key_e8dc2fefc0e244ff95cc77a3352c1826}")
    private String instanceId;

}
