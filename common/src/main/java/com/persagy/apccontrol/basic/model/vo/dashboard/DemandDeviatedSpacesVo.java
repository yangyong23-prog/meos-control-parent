package com.persagy.apccontrol.basic.model.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : wangchenghong
 * @date : 2024/12/3 16:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Dashboard照明-需求偏离空间")
public class DemandDeviatedSpacesVo {

    @Schema(description = "空间ID")
    private String id;

    @Schema(description = "空间名称")
    private String localName;

    @Schema(description = "多开总数")
    private Integer totalMoreOpenCount;

    @Schema(description = "少开总数")
    private Integer totalLessOpenCount;

    @Schema(description = "实际开启总数")
    private Integer totalRealOpenCount;

    @Schema(description = "需求开启总数")
    private Integer totalDemandOpenCount;

    @Schema(description = "需求干预总数")
    private Integer totalDemandInterventionCount;

    @Schema(description = "手动控制总数")
    private Integer totalManualCount;

}
