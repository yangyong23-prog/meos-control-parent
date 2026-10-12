package com.persagy.apccontrol.basic.model.vo.security;

import com.persagy.validate.utility.annotation.NumberToScaleString;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 安防-DashboardVO
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@Schema(title = "安防-DashboardVO")
public class SecurityDashboardVO {

    @Schema(description = "需求一致性")
    @NumberToScaleString(scale = 10)
    private BigDecimal demandConsistency;

    @Schema(description = "需求空间总数")
    private Map<BaseEnum<?>, Integer> totalDemandSpaceCount;

    @Schema(description = "建筑列表")
    private List<SecurityDashboardBuildingVO> buildings;

}
