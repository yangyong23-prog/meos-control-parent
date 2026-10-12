package com.persagy.apccontrol.basic.model.vo.demand.openingcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 需求管理-营业日历-全局规则最后更新时间VO
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "需求管理-营业日历-全局规则最后更新时间VO")
public class DemandGlobalRuleLastUpdateTimeVO {

    @Schema(description = "最后一次更新时间")
    private LocalDateTime dateTime;

}
