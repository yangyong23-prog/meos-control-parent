package com.persagy.apccontrol.basic.model.param.midcalendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

/**
 * 工作历营业时间批量查询参数
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Data
public class MidCalendarWorkTimeBatchQueryParam {

    @Schema(description = "对象ID")
    @NotEmpty(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private Set<String> objectIds;

    @Schema(description = "开始日期, 闭区间")
    @NotNull(message = "{key_6dc6cc33082c41d189980dd415b06083}")
    private LocalDate beginDate;

    @Schema(description = "结束日期, 开区间")
    @NotNull(message = "{key_fe10d89294244a90a5a3d488db40d202}")
    private LocalDate endDate;

}
