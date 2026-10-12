package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 网格图空间信息-查询参数
 *
 * @author yuyao
 */
@Data
@Accessors(chain = true)
@Schema(title = "网格图需求空间信息按照楼层分组-查询参数")
public class DemandSpaceByGroupFloorQueryParam {

    @Schema(description = "系统类型", required = true)
    @NotNull(message = "{key_fd5b7b3d9ead4d7ca7fb03ba5bd37127}")
    private RunningMonitorTypeEnum runningMonitorType;

    @Schema(description = "系统二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "筛选条件", required = true)
    @NotNull(message = "{key_03cbf640855d41d88b5f5b09aad9aa59}")
    private DemandInstanceValueObject instance;

    @Schema(description = "查询字段, 为空时查询默认字段")
    private String[] withColumns;

}
