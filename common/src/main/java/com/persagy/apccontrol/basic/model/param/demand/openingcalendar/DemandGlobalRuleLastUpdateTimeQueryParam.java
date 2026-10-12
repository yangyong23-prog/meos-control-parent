package com.persagy.apccontrol.basic.model.param.demand.openingcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 需求管理-营业日历全局规则最后更新时间查询参数
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-营业日历全局规则最后更新时间查询参数")
public class DemandGlobalRuleLastUpdateTimeQueryParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

}
